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

import android.app.Activity;
import android.widget.ImageView;
import android.widget.Toast;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.TwAjax;

public class TimelineEventsOverlay extends ItemizedOverlayWithFocus<OverlayItem> {

	private TimelineEventMapActivity timelineEventMapActivity = null;


//	protected List<OverlayItem> itemList = null;


	public TimelineEventsOverlay(List<OverlayItem> aList, final Activity owner, ResourceProxy mResourceProxy) {
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
//		itemList = aList;
		this.timelineEventMapActivity = (TimelineEventMapActivity) owner;
	}
/*
	public void addItem(String title, String snippet, GeoPoint p) {
		ExtendedOverlayItem newItem = new ExtendedOverlayItem(title, snippet, p, timelineEventMapActivity);
		itemList.add(newItem);
		populate();
	}
*/

}