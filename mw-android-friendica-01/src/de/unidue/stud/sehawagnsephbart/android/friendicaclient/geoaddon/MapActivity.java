package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import de.wikilab.android.friendica01.R;

public class MapActivity extends Activity implements MapEventsReceiver {

	public MapView mOsmv = null;
	private LocationEventsOverlay locationEventsOverlay = null;
	public ResourceProxy mResourceProxy = null;
	private MapController mMapController = null;
	private PathOverlay myPath = null;
	protected Road mRoad = null;
	protected PathOverlay roadOverlay = null;

	public ArrayList<GeoPoint> coordinates = new ArrayList<GeoPoint>();
	private MyLocationOverlay mLocationOverlay;
	private ScaleBarOverlay mScaleBarOverlay;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResourceProxy = new ResourceProxyImpl(getApplicationContext());

		final RelativeLayout rl = new RelativeLayout(this);

		this.mOsmv = new MapView(this, 1024);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

		this.mOsmv.setBuiltInZoomControls(true);
		this.mOsmv.setMultiTouchControls(true);

		this.mOsmv.setTileSource(TileSourceFactory.MAPNIK);
		mMapController = this.mOsmv.getController();
		mMapController.setZoom(16);

		this.mScaleBarOverlay = new ScaleBarOverlay(this, mResourceProxy);
		this.mOsmv.getOverlays().add(mScaleBarOverlay);

		this.mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2 - getResources().getDisplayMetrics().xdpi / 2, 10);

		this.mLocationOverlay = new MyLocationOverlay(this, this.mOsmv, mResourceProxy);
		this.mLocationOverlay.enableCompass();

		this.mOsmv.getOverlays().add(this.mLocationOverlay);

		// TODO change to center on last event location
		mMapController.setCenter(new GeoPoint(51.4624925, 7.0169541));

		this.locationEventsOverlay = new LocationEventsOverlay(items, this, mResourceProxy);
		this.locationEventsOverlay.addTimelinePositions();
		this.locationEventsOverlay.setFocusItemsOnTap(true);
		this.mOsmv.getOverlays().add(this.locationEventsOverlay);

		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		this.mOsmv.getOverlays().add(overlay);

		/*
		 * breaks all markers MinimapOverlay miniMapOverlay = new
		 * MinimapOverlay(this, mOsmv.getTileRequestCompleteHandler());
		 * this.mOsmv.getOverlays().add(miniMapOverlay);
		 */

		this.setContentView(rl);
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
			if (this.mLocationOverlay.isMyLocationEnabled() == true) {
				this.mLocationOverlay.disableMyLocation();
				this.mLocationOverlay.disableFollowLocation();
				CharSequence text = "Location disabled";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			} else {
				this.mLocationOverlay.enableMyLocation();
				this.mLocationOverlay.enableFollowLocation();
				CharSequence text = "Location enabled";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	    		//Mapnik
	        case R.id.submenu1:
	            if (checked)
	            	
	            break;
	            //Bicycle map
	        case R.id.submenu2:
	            if (checked)
	              
	            break;
	            //public transport
	        case R.id.submenu3:
	        	if (checked)
	        		
	        		break;
	        	//Mapquest
	        case R.id.submenu4:
	        	if (checked)
	        		
	        		break;
	        	//Mapquest Aerial
	        case R.id.submenu5:
	        	if (checked)
	        		
	        		break;
	        	//Bing
	        case R.id.submenu6:
	        	if (checked)
	        		
	        		break;
	    }
	}
	
	public void goOn(ArrayList<TimelineEvent> timelineEvents) {
		// owner.mOsmv.getOverlays().add(getPathOverlay());

		getRoadAsync(timelineEvents);

		final ArrayList<TimelineEventItem> roadItems = generateMarkers(timelineEvents);
		ItemizedOverlayWithBubble<TimelineEventItem> roadNodes = new ItemizedOverlayWithBubble<TimelineEventItem>(this, roadItems, mOsmv,new TimelineEventMapPopup(R.layout.map_popup, mOsmv));
		mOsmv.getOverlays().add(roadNodes);
	}

	ArrayList<TimelineEventItem> generateMarkers(ArrayList<TimelineEvent> timelineEvents) {
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		ArrayList<TimelineEventItem> markers = new ArrayList<TimelineEventItem>();
		for (TimelineEvent timelineEvent : timelineEvents) {
			ArrayList<TimelineEvent> curTimelineEvents=new ArrayList<TimelineEvent>();
			curTimelineEvents.add(timelineEvent);
			curTimelineEvents.add(timelineEvent);

			TimelineEventItem nodeMarker = new TimelineEventItem(curTimelineEvents, this);
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
	//		nodeMarker.setTitle("Posted:");
	//		nodeMarker.setDescription(timelineEvent.getId() + ": " + timelineEvent.getText());
	//		nodeMarker.setSubDescription(timelineEvent.getDateTime());
	//		nodeMarker.setSubDescription(timelineEvent.getRelativeDate(this));

	//		nodeMarker.setImage(timelineEvent.getImage());

			markers.add(nodeMarker);
		}
		return markers;
	}

	public PathOverlay getPathOverlay() {
		if (myPath == null) {
			myPath = new PathOverlay(Color.parseColor("#99FF0000"), this);
		}
		return myPath;
	}

	public void getRoadAsync(ArrayList<TimelineEvent> timelineEvents) {
		mRoad = null;
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
		for (TimelineEvent timelineEvent : timelineEvents) {
			if (timelineEvent.getLocation() != null) {
				waypoints.add(timelineEvent.getLocation());
			}
		}
		new UpdateRoadTask().execute(waypoints);
	}

	void updateUIWithRoad(Road road) {
		if (road == null) {
			return;
		}
		if (road.mStatus == Road.STATUS_DEFAULT) {
			Toast.makeText(this.mOsmv.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		}
		roadOverlay = RoadManager.buildRoadOverlay(road, this.mOsmv.getContext());
		mOsmv.getOverlays().add(roadOverlay);
		this.mOsmv.invalidate();
	}

	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
		@SuppressWarnings("unchecked")
		protected Road doInBackground(Object... params) {
			RoadManager roadManager = new OSRMRoadManager();
			return roadManager.getRoad(((ArrayList<GeoPoint>) params[0]));
		}

		protected void onPostExecute(Road result) {
			mRoad = result;
			updateUIWithRoad(result);
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
}