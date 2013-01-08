package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.PostListAdapter;
import de.wikilab.android.friendica01.PostListFragment;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.TwAjax;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class LocationEventsOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
	private Activity owner = null;
	protected ListView list = null;
	protected ListAdapter ad = null;

	public LocationEventsOverlay(Drawable pDefaultMarker,
			ResourceProxy pResourceProxy, Activity owner) {
		super(pDefaultMarker, pResourceProxy);
		this.owner = owner;
		// TODO Auto-generated constructor stub
	}

	public void addItem(String title, String snippet, GeoPoint p) {
		OverlayItem newItem = new OverlayItem(title, snippet, p);
		overlayItemList.add(newItem);
		populate();
	}

	public void addTestItem() {
		// addItem("testpunkt", "testpunkt", new GeoPoint(51.4624925,
		// 7.0169541));
		// addItem("testpunkt1", "testpunkt1", new GeoPoint(41.4624925,
		// 7.0169541));
		// addItem("testpunkt", "testpunkt", new GeoPoint(11.4624925,
		// 7.0169541));

	}

	public void addTimelinePositions() {
		final TwAjax t = new TwAjax(owner, true, true);

		t.getUrlContent(Max.getServer(owner)
				+ "/api/statuses/home_timeline.json", new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();

					ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>(
							j.length());

					for (int i = 0; i < j.length(); i++) {
						JSONObject jj = j.getJSONObject(i);
						String coordinates = jj.getString("coordinates");
						String[] splitcoordinates = coordinates.split(" ");
						jsonObjectArray.add(jj);

						addItem("testpunkt",
								"testpunkt",
								new GeoPoint(Double
										.parseDouble(splitcoordinates[0]), Double
										.parseDouble(splitcoordinates[1])));
						System.out.println(Double
										.parseDouble(splitcoordinates[0]));
					}

					/*
					 * ad = new PostListAdapter(owner, jsonObjectArray);
					 * 
					 * list.setAdapter(ad);
					 */

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		});

	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return overlayItemList.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return overlayItemList.size();
	}

}