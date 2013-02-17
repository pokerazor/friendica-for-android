package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.TwAjax;

public class TimelineEventMapActivity extends Activity implements MapEventsReceiver {
	private Friendica friendicaAbstraction = null;

	protected MapView mapView = null;
	protected ResourceProxy mResourceProxy = null;
	protected MapController mMapController = null;

	protected MyLocationOverlay myLocationOverlay = null;
	protected ScaleBarOverlay scaleBarOverlay = null;

	protected ItemizedOverlayWithBubble<TimelineEventItem> timelineEventItemsOverlay = null;
	protected PathOverlay eventRoadRouteOverlay = null;
	protected Road eventRoadRoute = null;

	protected ArrayList<GeoPoint> eventItemLocations = new ArrayList<GeoPoint>();
	protected ArrayList<TimelineEvent> timelineEvents = new ArrayList<TimelineEvent>();
	protected ArrayList<TimelineEventItem> timelineEventItems = null;

	public ArrayList<GeoPoint> getEventItemLocations() {
		return eventItemLocations;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_activity_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Context context = getApplicationContext();
		switch (item.getItemId()) {
		case R.id.map_mode:
			return true;
		case R.id.position:
			if (this.myLocationOverlay.isMyLocationEnabled() == true) {
				this.myLocationOverlay.disableMyLocation();
				this.myLocationOverlay.disableFollowLocation();
				CharSequence text = "Location disabled";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			} else {
				this.myLocationOverlay.enableMyLocation();
				this.myLocationOverlay.enableFollowLocation();
				CharSequence text = "Location enabled";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onRadioButtonClicked(View clickedItem) {
		Boolean checked = ((RadioButton) clickedItem).isChecked(); // Is the button now checked?

		switch (clickedItem.getId()) { 		// Check which radio button was clicked

		// Mapnik
		case R.id.submenu1:
			if (checked) {
			}

			break;
		// Bicycle map
		case R.id.submenu2:
			if (checked) {
			}
			break;
		// public transport
		case R.id.submenu3:
			if (checked) {
			}

			break;
		// Mapquest
		case R.id.submenu4:
			if (checked) {
			}

			break;
		// Mapquest Aerial
		case R.id.submenu5:
			if (checked) {
			}

			break;
		// Bing
		case R.id.submenu6:
			if (checked) {
			}

			break;
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		friendicaAbstraction = new Friendica(this);

		mResourceProxy = new ResourceProxyImpl(getApplicationContext());

		final RelativeLayout rl = new RelativeLayout(this);
		this.mapView = new MapView(this, 1024);
		rl.addView(this.mapView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setMultiTouchControls(true);
		this.mapView.setTileSource(TileSourceFactory.MAPNIK);
		this.mMapController = this.mapView.getController();
		this.mMapController.setZoom(16);

		this.scaleBarOverlay = new ScaleBarOverlay(this, mResourceProxy);
		this.scaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2 - getResources().getDisplayMetrics().xdpi / 2, 10);
		this.mapView.getOverlays().add(scaleBarOverlay);

		this.myLocationOverlay = new MyLocationOverlay(this, this.mapView, mResourceProxy);
		this.myLocationOverlay.enableCompass();
		this.mapView.getOverlays().add(this.myLocationOverlay);

		this.renderTimelineEventPositions();

		// TODO change to center on last event location
		mMapController.setCenter(new GeoPoint(51.4624925, 7.0169541));

		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		this.mapView.getOverlays().add(overlay);

		this.setContentView(rl);
	}

	public void renderTimelineEventPositions() {
		friendicaAbstraction.executeAjaxQuery("routes/activeevents", new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				ArrayList<JSONObject> routeEvents = result.getResult();
				for (JSONObject routeEvent : routeEvents) {
					TimelineEvent tEvent = new TimelineEvent(routeEvent);

					if (tEvent.getLocation() != null) {
						tEvent.processImage(TimelineEventMapActivity.this);
						timelineEvents.add(tEvent);
						eventItemLocations.add(tEvent.getLocation());
					}
				}
				new ComputeTimelineEventRoadRouteAsyncTask().execute(eventItemLocations);

				timelineEventItems = generateTimelineEventItems(timelineEvents);
				timelineEventItemsOverlay = new ItemizedOverlayWithBubble<TimelineEventItem>(TimelineEventMapActivity.this, timelineEventItems, mapView, new TimelineEventMapPopup(R.layout.map_popup, mapView));
				mapView.getOverlays().add(timelineEventItemsOverlay);
				
				BoundingBoxE6 boundingBox=BoundingBoxE6.fromGeoPoints(eventItemLocations);
				mMapController.setCenter(boundingBox.getCenter());
				mMapController.zoomToSpan(boundingBox);
				

			}
		});
	}

	ArrayList<TimelineEventItem> generateTimelineEventItems(ArrayList<TimelineEvent> timelineEvents) {
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		ArrayList<TimelineEventItem> allTimelineEventItems = new ArrayList<TimelineEventItem>();
		for (TimelineEvent timelineEvent : timelineEvents) {
			ArrayList<TimelineEvent> curTimelineEvents = new ArrayList<TimelineEvent>();
			curTimelineEvents.add(timelineEvent);
			curTimelineEvents.add(timelineEvent); // FIXME Duplicate only to show ListView

			TimelineEventItem timelineEventItem = new TimelineEventItem(curTimelineEvents, this);
			timelineEventItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			timelineEventItem.setMarker(marker);

			allTimelineEventItems.add(timelineEventItem);
		}
		return allTimelineEventItems;
	}

	void renderTimelineEventRoadRoute(Road road) {
		if (road == null) {
			return;
		}
		if (road.mStatus == Road.STATUS_DEFAULT) {
			Toast.makeText(this.mapView.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		}
		eventRoadRouteOverlay = RoadManager.buildRoadOverlay(road, this.mapView.getContext());
		mapView.getOverlays().add(eventRoadRouteOverlay);
		this.mapView.invalidate();
	}

	private class ComputeTimelineEventRoadRouteAsyncTask extends AsyncTask<Object, Void, Road> {
		@SuppressWarnings("unchecked")
		protected Road doInBackground(Object... timelineEventLocations) {
			eventRoadRoute = null;

			RoadManager roadManager = new OSRMRoadManager();
			return roadManager.getRoad(((ArrayList<GeoPoint>) timelineEventLocations[0]));
		}

		protected void onPostExecute(Road computedTimelineEventRoadRoute) {
			eventRoadRoute = computedTimelineEventRoadRoute;
			renderTimelineEventRoadRoute(computedTimelineEventRoadRoute);
		}
	}

	@Override
	public boolean longPressHelper(IGeoPoint eventLocation) {
		Toast.makeText(this, "Map long pressed at " + eventLocation.getLatitudeE6() + ", " + eventLocation.getLongitudeE6(), Toast.LENGTH_SHORT).show();
		return false;
	}

	@Override
	public boolean singleTapUpHelper(IGeoPoint eventLocation) {
		Toast.makeText(this, "Map single tapped at " + eventLocation.getLatitudeE6() + ", " + eventLocation.getLongitudeE6(), Toast.LENGTH_SHORT).show();
		return false;
	}
	
	public GeoPoint[] getEventBoundingBox(){
		GeoPoint[] boundingBox=new GeoPoint[2];
		GeoPoint firstElement=this.eventItemLocations.iterator().next();
		
		Integer minLat=firstElement.getLatitudeE6();
		Integer maxLat=firstElement.getLatitudeE6();
		Integer minLon=firstElement.getLongitudeE6();
		Integer maxLon=firstElement.getLongitudeE6();

		for (GeoPoint eventLocation : this.eventItemLocations) {
			System.out.println(eventLocation);
			if(eventLocation.getLatitudeE6()<minLat){
				minLat=eventLocation.getLatitudeE6();
			}
			if(eventLocation.getLatitudeE6()>maxLat){
				maxLat=eventLocation.getLatitudeE6();
			}
			if(eventLocation.getLongitudeE6()<minLon){
				minLon=eventLocation.getLongitudeE6();
			}
			if(eventLocation.getLongitudeE6()>maxLon){
				maxLon=eventLocation.getLongitudeE6();
			}
		}
		
		boundingBox[0]=new GeoPoint(minLat, minLon);
		boundingBox[1]=new GeoPoint(maxLat, maxLon);
		System.out.println(boundingBox[0]);
		System.out.println(boundingBox[1]);
		return boundingBox;
	}

}