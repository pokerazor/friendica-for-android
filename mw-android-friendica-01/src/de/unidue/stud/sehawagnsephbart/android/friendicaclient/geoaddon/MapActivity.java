// Created by plusminus on 00:23:14 - 03.10.2008
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import de.wikilab.android.friendica01.R;

public class MapActivity extends Activity {

	public MapView mOsmv = null;
	private LocationEventsOverlay locationEventsOverlay = null;
	public ResourceProxy mResourceProxy = null;
	private MapController mMapController = null;
	private PathOverlay myPath = null;
	protected Road mRoad = null;
	protected PathOverlay roadOverlay = null;

	public ArrayList<GeoPoint> coordinates = new ArrayList<GeoPoint>();

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResourceProxy = new ResourceProxyImpl(getApplicationContext());

		final RelativeLayout rl = new RelativeLayout(this);

		this.mOsmv = new MapView(this, 1024);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		{

			ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

			this.locationEventsOverlay = new LocationEventsOverlay(items, this, mResourceProxy);
			this.locationEventsOverlay.addTimelinePositions();
			this.locationEventsOverlay.setFocusItemsOnTap(true);

			this.mOsmv.setBuiltInZoomControls(true);
			this.mOsmv.setMultiTouchControls(true);

			this.mOsmv.setTileSource(TileSourceFactory.MAPNIK);
			mMapController = this.mOsmv.getController();
			mMapController.setZoom(16);

			// TODO change to center on last event location
			mMapController.setCenter(new GeoPoint(51.4624925, 7.0169541));

			this.mOsmv.getOverlays().add(this.locationEventsOverlay);

		}

		this.setContentView(rl);
	}

	public void goOn(ArrayList<GeoPoint> coordinates) {
		// owner.mOsmv.getOverlays().add(getPathOverlay());

		getRoadAsync(coordinates);

		final ArrayList<ExtendedOverlayItem> roadItems = generateMarkers(coordinates);
		ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, mOsmv);
		mOsmv.getOverlays().add(roadNodes);
	}

	ArrayList<ExtendedOverlayItem> generateMarkers(ArrayList<GeoPoint> coordinates) {
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		ArrayList<ExtendedOverlayItem> markers = new ArrayList<ExtendedOverlayItem>();
		for (int i = 0; i < coordinates.size(); i++) {
			GeoPoint node = coordinates.get(i);
			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("", "", node, this);
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
			nodeMarker.setTitle("Posted:");
			nodeMarker.setDescription("Hier ist es total schön.");
			nodeMarker.setSubDescription("14. Mai, 15:36 Uhr");
			
			Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
			nodeMarker.setImage(icon);
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

	public void getRoadAsync(ArrayList<GeoPoint> waypoints) {
		mRoad = null;
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

}