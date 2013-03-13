package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
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
import android.view.MotionEvent;
import android.view.SubMenu;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;
import de.wikilab.android.friendica01.R;

public class TimelineEventMapActivity extends Activity implements MapEventsReceiver {
	protected Friendica friendicaAbstraction = null;
	
	protected final static Integer ROUTES_GROUP=42;

	protected MapView mapView = null;
	protected ResourceProxy mResourceProxy = null;
	protected MapController mMapController = null;

	protected MyLocationOverlay myLocationOverlay = null;
	protected ScaleBarOverlay scaleBarOverlay = null;

	protected ItemizedOverlayWithBubble<TimelineEventItem> timelineEventItemsOverlay = null;
	protected PathOverlay eventRoadRouteOverlay = null;
	protected ArrayList<PathOverlay> eventRoadRouteOverlays = new ArrayList<PathOverlay>();
	protected Road eventRoadRoute = null;

	protected ArrayList<GeoPoint> eventItemLocations = new ArrayList<GeoPoint>();
	protected ArrayList<TimelineEvent> timelineEvents = new ArrayList<TimelineEvent>();
	protected ArrayList<TimelineEventItem> timelineEventItems = null;

	protected ArrayList<JSONObject> myRoutes = null;

	protected Integer routingMode = 0;

	private Menu myMenu;

	public ArrayList<GeoPoint> getEventItemLocations() {
		return eventItemLocations;
	}

	public void loadList() {
		friendicaAbstraction.executeAjaxQuery("routes/list", new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				myRoutes = result.getResult();
				SubMenu routesmenu = myMenu.addSubMenu(R.string.menuitem_routes);
				for (JSONObject jsonObject : myRoutes) {
					try {
						routesmenu.add(ROUTES_GROUP,jsonObject.getInt("id"),Menu.NONE,jsonObject.getString("name"));

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_activity_menu, menu);
		myMenu = menu;
		loadList();
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Context context = getApplicationContext();
		Toast toast = null;
		switch (item.getItemId()) {
		case R.id.map_mode:
			return true;
		case R.id.position:
			if (this.myLocationOverlay.isMyLocationEnabled() == true) {
				this.myLocationOverlay.disableMyLocation();
				this.myLocationOverlay.disableFollowLocation();
				this.myLocationOverlay.disableCompass();


				mapView.getOverlayManager().remove(myLocationOverlay);
				mapView.getOverlays().remove(myLocationOverlay);

				toast = Toast.makeText(context, "Stop showing current location", Toast.LENGTH_SHORT);
				toast.show();
			} else {
				this.myLocationOverlay.enableMyLocation();
				this.myLocationOverlay.enableFollowLocation();
				this.myLocationOverlay.enableCompass();
				
				mapView.getOverlayManager().add(myLocationOverlay);
				this.mMapController.setZoom(16);

				toast = Toast.makeText(context, "Show current location", Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;
		case R.id.submenu1:
			if (item.isChecked())
				item.setChecked(false);
			else {
				item.setChecked(true);
				this.mapView.setTileSource(TileSourceFactory.MAPNIK);
			}
			return true;

		case R.id.submenu2:
			if (item.isChecked())
				item.setChecked(false);
			else {
				item.setChecked(true);
				this.mapView.setTileSource(TileSourceFactory.CYCLEMAP);
			}
			return true;

		case R.id.submenu3:
			if (item.isChecked())
				item.setChecked(false);
			else {
				item.setChecked(true);
				this.mapView.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
			}
			return true;

		case R.id.submenu4:
			if (item.isChecked())
				item.setChecked(false);
			else {
				item.setChecked(true);
				this.mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
			}
			return true;

		case R.id.submenu5:
			if (item.isChecked())
				item.setChecked(false);
			else {
				item.setChecked(true);
				this.mapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
			}
			return true;

		case R.id.submenu6:
			if (item.isChecked())
				item.setChecked(false);
			else {
				item.setChecked(true);
				routingMode = 0;
				renderTimelineEventRoadRoute();
			}
			return true;

		case R.id.submenu7:
			if (item.isChecked())
				item.setChecked(false);
			else {
				item.setChecked(true);
				routingMode = 1;
				renderTimelineEventRoadRoute();
			}
			return true;

		default:
			if(item.getGroupId()==ROUTES_GROUP){
				System.out.println("Item ID:" + item.getItemId());
				Integer routeId = item.getItemId();
				friendicaAbstraction.executeAjaxQuery("routes/setactive/"+routeId, new JsonFinishReaction<ArrayList<JSONObject>>() {
					@Override
					public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
						renderTimelineEventPositions();
					}
				});

			}
			return super.onOptionsItemSelected(item);
			
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
		this.mapView.getOverlayManager().add(scaleBarOverlay);

		this.myLocationOverlay = new MyLocationOverlay(this, this.mapView, mResourceProxy);
		this.myLocationOverlay.enableCompass();
		this.mapView.getOverlayManager().add(this.myLocationOverlay);

		this.renderTimelineEventPositions();

		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		this.mapView.getOverlayManager().add(overlay);

		this.setContentView(rl);
	}

	public void renderTimelineEventPositions() {
		timelineEvents.clear();
		eventItemLocations.clear();
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
				renderTimelineEventRoadRoute();

				timelineEventItems = generateTimelineEventItems(timelineEvents);
				mapView.getOverlayManager().remove(timelineEventItemsOverlay);
				timelineEventItemsOverlay = new ItemizedOverlayWithBubble<TimelineEventItem>(TimelineEventMapActivity.this, timelineEventItems, mapView, new TimelineEventMapPopup(R.layout.map_popup, mapView));
				mapView.getOverlayManager().add(timelineEventItemsOverlay);

				BoundingBoxE6 boundingBox = BoundingBoxE6.fromGeoPoints(eventItemLocations);
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
			curTimelineEvents.add(timelineEvent); // FIXME Duplicate only to
													// show ListView

			TimelineEventItem timelineEventItem = new TimelineEventItem(curTimelineEvents, this);
			timelineEventItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			timelineEventItem.setMarker(marker);

			allTimelineEventItems.add(timelineEventItem);
		}
		return allTimelineEventItems;
	}

	void renderTimelineEventRoadRoute() {
		for (PathOverlay curOverlay : eventRoadRouteOverlays) {
			mapView.getOverlayManager().remove(curOverlay);
		}
		eventRoadRouteOverlays.clear();

		new ComputeTimelineEventRoadRouteAsyncTask().execute(eventItemLocations);
	}

	void renderTimelineEventCompleteRoadRoute(Road road) {
		if (road == null) {
			return;
		}
		if (road.mStatus == Road.STATUS_DEFAULT) {
			Toast.makeText(this.mapView.getContext(), "Event road route cannot be calculated (water in the way?)", Toast.LENGTH_SHORT).show();
		}
		eventRoadRouteOverlay = RoadManager.buildRoadOverlay(road, this);

		mapView.getOverlayManager().add(eventRoadRouteOverlay);
		this.mapView.invalidate();
	}

	private class ComputeTimelineEventRoadRouteAsyncTask extends AsyncTask<ArrayList<GeoPoint>, Void, Road> {
		protected Road doInBackground(ArrayList<GeoPoint>... timelineEventLocations) {
			eventRoadRoute = null;

			RoadManager roadManager = new OSRMRoadManager();
			if (routingMode == 1) {
				roadManager = new MapQuestRoadManager();
				roadManager.addRequestOption("routeType=bicycle");
			}
			Road completeRoad = roadManager.getRoad(timelineEventLocations[0]);
			GeoPoint lastLocation = null;
			ArrayList<GeoPoint> curLeg = new ArrayList<GeoPoint>();

			for (GeoPoint curLocation : timelineEventLocations[0]) {
				curLeg.add(curLocation);
				if (curLeg.size() == 2) {
					eventRoadRoute = roadManager.getRoad(curLeg);
					eventRoadRouteOverlay = RoadManager.buildRoadOverlay(eventRoadRoute, TimelineEventMapActivity.this);
					eventRoadRouteOverlays.add(eventRoadRouteOverlay);

					mapView.getOverlayManager().add(eventRoadRouteOverlay);
					curLeg.remove(lastLocation);
				}
				lastLocation = curLocation;
			}

			return roadManager.getRoad(timelineEventLocations[0]);
		}

		protected void onPostExecute(Road computedTimelineEventRoadRoute) {
			eventRoadRoute = computedTimelineEventRoadRoute;
			// renderTimelineEventRoadRoute(computedTimelineEventRoadRoute);
		}
	}

	@Override
	public boolean longPressHelper(IGeoPoint eventLocation) {
		TimelineEventItem item = new TimelineEventItem((GeoPoint) eventLocation, this);

		new TimelineEventMapPopup(R.layout.map_popup, mapView,true).open(item, 0, 0);
		Toast.makeText(this, "Map long pressed at " + eventLocation.getLatitudeE6() + ", " + eventLocation.getLongitudeE6(), Toast.LENGTH_SHORT).show();
		return false;
	}

	@Override
	public boolean singleTapUpHelper(IGeoPoint eventLocation) {
		Toast.makeText(this, "Map single tapped at " + eventLocation.getLatitudeE6() + ", " + eventLocation.getLongitudeE6(), Toast.LENGTH_SHORT).show();
		return false;
	}

	public GeoPoint[] getEventBoundingBox() {
		GeoPoint[] boundingBox = new GeoPoint[2];
		GeoPoint firstElement = this.eventItemLocations.iterator().next();

		Integer minLat = firstElement.getLatitudeE6();
		Integer maxLat = firstElement.getLatitudeE6();
		Integer minLon = firstElement.getLongitudeE6();
		Integer maxLon = firstElement.getLongitudeE6();

		for (GeoPoint eventLocation : this.eventItemLocations) {
			System.out.println(eventLocation);
			if (eventLocation.getLatitudeE6() < minLat) {
				minLat = eventLocation.getLatitudeE6();
			}
			if (eventLocation.getLatitudeE6() > maxLat) {
				maxLat = eventLocation.getLatitudeE6();
			}
			if (eventLocation.getLongitudeE6() < minLon) {
				minLon = eventLocation.getLongitudeE6();
			}
			if (eventLocation.getLongitudeE6() > maxLon) {
				maxLon = eventLocation.getLongitudeE6();
			}
		}

		boundingBox[0] = new GeoPoint(minLat, minLon);
		boundingBox[1] = new GeoPoint(maxLat, maxLon);
		System.out.println(boundingBox[0]);
		System.out.println(boundingBox[1]);
		return boundingBox;
	}

}