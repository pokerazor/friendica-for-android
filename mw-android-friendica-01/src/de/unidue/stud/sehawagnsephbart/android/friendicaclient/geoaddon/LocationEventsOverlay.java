package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.TwAjax;

import android.app.Activity;
import android.graphics.Color;
import android.widget.Toast;

public class LocationEventsOverlay extends ItemizedOverlayWithFocus<OverlayItem> {
	protected List<OverlayItem> itemList = null;
	private MapActivity owner = null;
	private PathOverlay myPath;

	public LocationEventsOverlay(List<OverlayItem> aList, final Activity owner, ResourceProxy mResourceProxy) {
		super(aList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
			@Override
			public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				Toast.makeText(owner, "Item '" + item.mTitle + "' (index=" + index + ") got single tapped up", Toast.LENGTH_LONG).show();
				return true;
			}

			@Override
			public boolean onItemLongPress(final int index, final OverlayItem item) {
				Toast.makeText(owner, "Item '" + item.mTitle + "' (index=" + index + ") got long pressed", Toast.LENGTH_LONG).show();
				return false;
			}
		}, mResourceProxy);
		itemList = aList;
		this.owner = (MapActivity) owner;
	}

	public void addItem(String title, String snippet, GeoPoint p) {
		OverlayItem newItem = new OverlayItem(title, snippet, p);
		itemList.add(newItem);
		populate();

	}

	public void addTestItem() {
		addItem("testpunkt", "testpunkt", new GeoPoint(51.4624925, 7.0169541));
		// addItem("testpunkt1", "testpunkt1", new GeoPoint(41.4624925,
		// 7.0169541));
		// addItem("testpunkt", "testpunkt", new GeoPoint(11.4624925,
		// 7.0169541));

	}

	public void addTimelinePositions() {
		final TwAjax t = new TwAjax(owner, true, true);

		t.getUrlContent(Max.getServer(owner) + "/api/statuses/home_timeline.json", new Runnable() {

			@Override
			public void run() {
				try {

					JSONArray j = (JSONArray) t.getJsonResult();

					ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>(j.length());
					GeoPoint gp = null;
					for (int i = 0; i < j.length(); i++) {
						JSONObject jj = j.getJSONObject(i);
						String coordinates = jj.getString("coordinates");
						if (!coordinates.equals("")) {
							String[] splitcoordinates = coordinates.split(" ");
							jsonObjectArray.add(jj);
							gp = new GeoPoint(Double.parseDouble(splitcoordinates[0]), Double.parseDouble(splitcoordinates[1]));

							addItem(jj.getString("id"), jj.getString("text"), (GeoPoint) gp.clone());
							System.out.println(Double.parseDouble(splitcoordinates[0]));
							LocationEventsOverlay.this.getPathOverlay().addPoint(gp);

						}
					}
					owner.mOsmv.getOverlays().add(getPathOverlay());

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		});

	}

	public PathOverlay getPathOverlay() {
		if (myPath == null) {
			myPath = new PathOverlay(Color.RED, owner);
		}
		return myPath;
	}
}