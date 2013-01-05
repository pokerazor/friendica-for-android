package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class MapActivity extends Activity {
	
	public static final String DEBUGTAG = "OPENSTREETMAP";

	public static final boolean DEBUGMODE = false;

	public static final int NOT_SET = Integer.MIN_VALUE;

	public static final String PREFS_NAME = "org.andnav.osm.prefs";
	public static final String PREFS_TILE_SOURCE = "tilesource";
	public static final String PREFS_SCROLL_X = "scrollX";
	public static final String PREFS_SCROLL_Y = "scrollY";
	public static final String PREFS_ZOOM_LEVEL = "zoomLevel";
	public static final String PREFS_SHOW_LOCATION = "showLocation";
	public static final String PREFS_SHOW_COMPASS = "showCompass";
	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MENU_SAMPLES = Menu.FIRST + 1;
	private static final int MENU_ABOUT = MENU_SAMPLES + 1;

	private static final int MENU_LAST_ID = MENU_ABOUT + 1; // Always set to last unused id
	// ===========================================================
	// Fields
	// ===========================================================

	private SharedPreferences mPrefs;
	private MapView mOsmv;
	private MyLocationOverlay mLocationOverlay;
	private ResourceProxy mResourceProxy;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		// only do static initialisation if needed
		if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
			CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());
		}

		final RelativeLayout rl = new RelativeLayout(this);
		this.mOsmv = new MapView(this, 256, mResourceProxy);
		this.mLocationOverlay = new MyLocationOverlay(this.getBaseContext(), this.mOsmv,
				mResourceProxy);
		this.mOsmv.setBuiltInZoomControls(true);
		this.mOsmv.setMultiTouchControls(true);
		this.mOsmv.getOverlays().add(this.mLocationOverlay);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		this.setContentView(rl);

		mOsmv.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
		mOsmv.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));


	
	}

	@Override
	protected void onPause() {
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(PREFS_TILE_SOURCE, mOsmv.getTileProvider().getTileSource().name());
		edit.putInt(PREFS_SCROLL_X, mOsmv.getScrollX());
		edit.putInt(PREFS_SCROLL_Y, mOsmv.getScrollY());
		edit.putInt(PREFS_ZOOM_LEVEL, mOsmv.getZoomLevel());
		edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());
		edit.putBoolean(PREFS_SHOW_COMPASS, mLocationOverlay.isCompassEnabled());
		edit.commit();

		this.mLocationOverlay.disableMyLocation();
		this.mLocationOverlay.disableCompass();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
				TileSourceFactory.DEFAULT_TILE_SOURCE.name());
		try {
			final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
			mOsmv.setTileSource(tileSource);
		} catch (final IllegalArgumentException ignore) {
		}
		if (mPrefs.getBoolean(PREFS_SHOW_LOCATION, false)) {
			this.mLocationOverlay.enableMyLocation();
		}
		if (mPrefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
			this.mLocationOverlay.enableCompass();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		// Put overlay items next
		mOsmv.getOverlayManager().onCreateOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);


		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		mOsmv.getOverlayManager().onPrepareOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		// Now process the menu item selection
		switch (item.getItemId()) {


		default:
			return mOsmv.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mOsmv);
		}
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent event) {
		return this.mOsmv.onTrackballEvent(event);
	}

}