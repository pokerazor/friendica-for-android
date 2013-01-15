package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class LocationEventsOverlayOld extends ItemizedOverlayWithFocus<OverlayItem> {

	private List<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
	
	private Activity owner = null;
	protected ListView list = null;
	protected ListAdapter ad = null;
	
	
	public LocationEventsOverlayOld(final Activity owner, List<OverlayItem> aList) {

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
			} , new ResourceProxyImpl(owner.getApplicationContext()));
		 this.owner=owner;
	  overlayItemList=aList;
	}
}
	/*
	LocationEventsOverlay(Drawable pDefaultMarker,
			ResourceProxy pResourceProxy, Activity owner) {
		super (pDefaultMarker, pResourceProxy);
		this.owner = owner;
		
		// TODO Auto-generated constructor stub
	}
*/
	/*
	public void addItem(String title, String snippet, GeoPoint p) {
		OverlayItem newItem = new OverlayItem(title, snippet, p);
		overlayItemList.add(newItem);
		populate();
	}

	public void addTestItem() {
//		 addItem("testpunkt", "testpunkt", new GeoPoint(51.4624925,
//		 7.0169541));
//		 addItem("testpunkt1", "testpunkt1", new GeoPoint(41.4624925,
//		 7.0169541));
//		 addItem("testpunkt", "testpunkt", new GeoPoint(11.4624925,
//		 7.0169541));

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
*/
					/*
					 * ad = new PostListAdapter(owner, jsonObjectArray);
					 * 
					 * list.setAdapter(ad);
					 */
/*
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
	*/