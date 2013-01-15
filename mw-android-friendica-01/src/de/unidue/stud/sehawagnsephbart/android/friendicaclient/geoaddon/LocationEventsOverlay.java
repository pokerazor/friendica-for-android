package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.List;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.widget.Toast;

public class LocationEventsOverlay extends ItemizedOverlayWithFocus<OverlayItem> {	
	protected List<OverlayItem> itemList=null;
	
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
			} , mResourceProxy);
		 itemList=aList;
	}
}