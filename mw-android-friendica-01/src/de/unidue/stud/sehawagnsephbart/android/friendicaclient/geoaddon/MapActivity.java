// Created by plusminus on 00:23:14 - 03.10.2008
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
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
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
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
		this.mLocationOverlay.enableMyLocation();
		this.mLocationOverlay.enableFollowLocation();

		this.mOsmv.getOverlays().add(this.mLocationOverlay);

		// TODO change to center on last event location
		mMapController.setCenter(new GeoPoint(51.4624925, 7.0169541));

		this.locationEventsOverlay = new LocationEventsOverlay(items, this, mResourceProxy);
		this.locationEventsOverlay.addTimelinePositions();
		this.locationEventsOverlay.setFocusItemsOnTap(true);
		this.mOsmv.getOverlays().add(this.locationEventsOverlay);
		
		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		this.mOsmv.getOverlays().add(overlay);

		/* breaks all markers
		MinimapOverlay miniMapOverlay = new MinimapOverlay(this, mOsmv.getTileRequestCompleteHandler());
		this.mOsmv.getOverlays().add(miniMapOverlay);
		*/

		this.setContentView(rl);
	}

	public void goOn(ArrayList<TimelineEvent> timelineEvents) {
		// owner.mOsmv.getOverlays().add(getPathOverlay());

		getRoadAsync(timelineEvents);

		final ArrayList<ExtendedOverlayItem> roadItems = generateMarkers(timelineEvents);
		ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, mOsmv);
		mOsmv.getOverlays().add(roadNodes);
	}

	ArrayList<ExtendedOverlayItem> generateMarkers(ArrayList<TimelineEvent> timelineEvents) {
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		ArrayList<ExtendedOverlayItem> markers = new ArrayList<ExtendedOverlayItem>();
		for (TimelineEvent timelineEvent : timelineEvents) {

			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("", "", timelineEvent.getLocation(), this);
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
			nodeMarker.setTitle("Posted:");
			nodeMarker.setDescription(timelineEvent.getId() + ": " + timelineEvent.getText());
			nodeMarker.setSubDescription(timelineEvent.getDateTime());
			nodeMarker.setSubDescription(timelineEvent.getRelativeDate(this));

			nodeMarker.setImage(timelineEvent.getImage());

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
			if(timelineEvent.getLocation() != null){
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
		Toast.makeText(this, "Map long pressed at "+eventLocation.getLatitudeE6()+", "+eventLocation.getLongitudeE6(), Toast.LENGTH_SHORT).show();
		return false;
	}

	@Override
	public boolean singleTapUpHelper(IGeoPoint eventLocation) {
		Toast.makeText(this, "Map single tapped at "+eventLocation.getLatitudeE6()+", "+eventLocation.getLongitudeE6(), Toast.LENGTH_SHORT).show();
		return false;
	}
}