// Created by plusminus on 00:23:14 - 03.10.2008
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
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

	private MapView mOsmv;
	private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
	public ResourceProxy mResourceProxy;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResourceProxy = new ResourceProxyImpl(getApplicationContext());

		final RelativeLayout rl = new RelativeLayout(this);

		this.mOsmv = new MapView(this, 256);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		{

			ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			items.add(new OverlayItem("Hannover", "Tiny SampleDescription", new GeoPoint(52370816, 9735936))); // Hannover

			this.mMyLocationOverlay= new LocationEventsOverlay(items, this, mResourceProxy);
			
			this.mMyLocationOverlay.setFocusItemsOnTap(true);
			this.mMyLocationOverlay.setFocusedItem(0);

			this.mOsmv.getOverlays().add(this.mMyLocationOverlay);
		}

		this.setContentView(rl);
	}
}