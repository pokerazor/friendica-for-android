package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.Toast;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.PostListAdapter;
import de.wikilab.android.friendica01.TwAjax;

public class LocationEventsOverlay extends ItemizedOverlayWithFocus<OverlayItem> {

	protected List<OverlayItem> itemList = null;
	private MapActivity owner = null;

	ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
	ArrayList<TimelineEvent> timelineEvents = new ArrayList<TimelineEvent>();

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
		ExtendedOverlayItem newItem = new ExtendedOverlayItem(title, snippet, p, owner);
		itemList.add(newItem);
		populate();
	}

	public TimelineEvent addItem(JSONObject jj) {
		TimelineEvent tEvent = new TimelineEvent(jj);
		try {			// TODO handle asynchronously
			ImageView target = new ImageView(owner);
			Friendica.getProfileImageFromPost(jj, target, owner);
//			Friendica.placeImageFromURI(url, target, owner, "itemImage");
			tEvent.loadImageIntoTarget(target,owner);
			tEvent.setImage(target.getDrawable());

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (tEvent.getLocation() != null) {
			timelineEvents.add(tEvent);
		}
		return tEvent;
	}

	public void addTimelinePositions() {
		final TwAjax t = new TwAjax(owner, true, true);
		t.getUrlContent(Max.getServer(owner) + "/api/routes/activeevents", new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();
					for (int i = 0; i < j.length(); i++) {
						JSONObject jj = j.getJSONObject(i);
						{
							TimelineEvent tEvent = addItem(jj);
							if (tEvent.getLocation() != null) {
								owner.coordinates.add(tEvent.getLocation());
								owner.getPathOverlay().addPoint(tEvent.getLocation());
								waypoints.add(tEvent.getLocation());
							}
						}
					}
					owner.goOn(timelineEvents);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ArrayList<GeoPoint> getLocationCoordinates() {
		return waypoints;
	}
}