// Created by plusminus on 00:23:14 - 03.10.2008
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class MapActivity extends Activity {

	public MapView mOsmv;
	private LocationEventsOverlay locationEventsOverlay;
	public ResourceProxy mResourceProxy;
	private MapController mMapController;
	private PathOverlay myPath;
	protected Road mRoad;
	protected PathOverlay roadOverlay;

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

			mMapController.setCenter(new GeoPoint(51.4624925, 7.0169541));

			this.mOsmv.getOverlays().add(this.locationEventsOverlay);

		}

		this.setContentView(rl);
	}

	public void goOn() {
		// owner.mOsmv.getOverlays().add(getPathOverlay());

		getRoadAsync(coordinates);

		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
		ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, mOsmv);
		mOsmv.getOverlays().add(roadNodes);
	}

	public PathOverlay getPathOverlay() {
		if (myPath == null) {
			myPath = new PathOverlay(Color.WHITE, this);
		}
		return myPath;
	}

	public void getRoadAsync(ArrayList<GeoPoint> waypoints) {
		mRoad = null;

		new UpdateRoadTask().execute(waypoints);
	}

	void updateUIWithRoad(Road road) {

		if (road == null)
			return;
		if (road.mStatus == Road.STATUS_DEFAULT)
			Toast.makeText(this.mOsmv.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		roadOverlay = RoadManager.buildRoadOverlay(road, this.mOsmv.getContext());

		mOsmv.getOverlays().add(roadOverlay);

		this.mOsmv.invalidate();

	}

	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {

		protected Road doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = ((ArrayList<GeoPoint>) params[0]);
			RoadManager roadManager = new OSRMRoadManager();

			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) {
			mRoad = result;
			updateUIWithRoad(result);
		}
	}

}