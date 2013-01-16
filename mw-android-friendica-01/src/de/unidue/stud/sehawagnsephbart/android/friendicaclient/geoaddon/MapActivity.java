// Created by plusminus on 00:23:14 - 03.10.2008
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
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

}