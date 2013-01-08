package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import de.wikilab.android.friendica01.FragmentParentListener;
import de.wikilab.android.friendica01.R;

public class MapFragment extends MapFragmentActivity implements FragmentParentListener {
	private static final String TAG = "Friendica/MapFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		myView = inflater.inflate(R.layout.fragment_map, container, false);
		return myView;
	}

	protected void onNavigate(String target) {
		if (target != null && target.equals("map")) {
			((FragmentParentListener) getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_mywall), null);
		}
	}

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

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

	private static final int MENU_LAST_ID = MENU_ABOUT + 1; // Always set to
															// last unused id
	// ===========================================================
	// Fields
	// ===========================================================

	private SharedPreferences mPrefs;
	private MapView mOsmv;
	private MyLocationOverlay mLocationOverlay;
	private ResourceProxy mResourceProxy;
	private Context myContext;

	// ===========================================================
	// Constructors
	// ===========================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myContext = this.getActivity().getApplicationContext();

		mResourceProxy = new DefaultResourceProxyImpl(myContext);

		mPrefs = myContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		// only do static initialization if needed
		if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
			CloudmadeUtil.retrieveCloudmadeKey(myContext);
		}

		final RelativeLayout rl = new RelativeLayout(this.getActivity());
		this.mOsmv = new MapView(this.getActivity(), 256, mResourceProxy);
		this.mLocationOverlay = new MyLocationOverlay(myContext, this.mOsmv, mResourceProxy);
		this.mOsmv.setBuiltInZoomControls(true);
		this.mOsmv.setMultiTouchControls(true);
		this.mOsmv.getOverlays().add(this.mLocationOverlay);
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		this.getActivity().setContentView(rl);

		mOsmv.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
		mOsmv.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));

	}

	@Override
	public void onPause() {
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
	public void onResume() {
		super.onResume();
		final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE, TileSourceFactory.DEFAULT_TILE_SOURCE.name());
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
	public void onPrepareOptionsMenu(final Menu pMenu) {
		mOsmv.getOverlayManager().onPrepareOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);
		return;
	}

}