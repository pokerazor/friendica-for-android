package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class TimelineEventItem extends ExtendedOverlayItem {
	private ArrayList<TimelineEvent> mTimelineEvents = null;

	public TimelineEventItem(GeoPoint aGeoPoint, Context context) {
		super("", "", aGeoPoint, context);
	}

	public TimelineEventItem(ArrayList<TimelineEvent> timelineEvents, TimelineEventMapActivity timelineEventMapActivity) {
		this(timelineEvents.get(0).getLocation(), timelineEventMapActivity);
		mTimelineEvents = timelineEvents;
	}

	public ArrayList<TimelineEvent> getTimelineEvents() {
		return mTimelineEvents;
	}

	public void setTimelineEvents(ArrayList<TimelineEvent> timelineEvents) {
		mTimelineEvents = timelineEvents;
	}

	/** From a HotspotPlace and drawable dimensions (width, height), return the hotspot position. Could be a public method of HotspotPlace or OverlayItem... */
	public Point getHotspot(HotspotPlace place, int w, int h) {
		Point hp = new Point();
		if (place == null)
			place = HotspotPlace.BOTTOM_CENTER; // use same default than in osmdroid.
		switch (place) {
		case NONE:
			hp.set(0, 0);
			break;
		case BOTTOM_CENTER:
			hp.set(w / 2, 0);
			break;
		case LOWER_LEFT_CORNER:
			hp.set(0, 0);
			break;
		case LOWER_RIGHT_CORNER:
			hp.set(w, 0);
			break;
		case CENTER:
			hp.set(w / 2, -h / 2);
			break;
		case LEFT_CENTER:
			hp.set(0, -h / 2);
			break;
		case RIGHT_CENTER:
			hp.set(w, -h / 2);
			break;
		case TOP_CENTER:
			hp.set(w / 2, -h);
			break;
		case UPPER_LEFT_CORNER:
			hp.set(0, -h);
			break;
		case UPPER_RIGHT_CORNER:
			hp.set(w, -h);
			break;
		}
		return hp;
	}

	/** Populates this bubble with all item info:
	 * <ul>
	 * title and description in any case,
	 * </ul>
	 * <ul>
	 * image and sub-description if any.
	 * </ul>
	 * and centers the map view on the item if panIntoView is true. <br> */
	public void showBubble(InfoWindow bubble, MapView mapView, boolean panIntoView) {
		// offset the bubble to be top-centered on the marker:
		Drawable marker = getMarker(0 /*OverlayItem.ITEM_STATE_FOCUSED_MASK*/);
		int markerWidth = 0, markerHeight = 0;
		if (marker != null) {
			markerWidth = marker.getIntrinsicWidth();
			markerHeight = marker.getIntrinsicHeight();
		} // else... we don't have the default marker size => don't user default markers!!!
		Point markerH = getHotspot(getMarkerHotspot(), markerWidth, markerHeight);
		Point bubbleH = getHotspot(HotspotPlace.TOP_CENTER, markerWidth, markerHeight);
		bubbleH.offset(-markerH.x, -markerH.y);

		bubble.open(this, bubbleH.x, bubbleH.y);
		if (panIntoView) {
			mapView.getController().animateTo(getPoint());
		}
	}
}
