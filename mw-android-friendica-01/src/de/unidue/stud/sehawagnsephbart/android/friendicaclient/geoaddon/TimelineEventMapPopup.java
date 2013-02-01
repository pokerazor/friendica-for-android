/**
 * 
 */
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import de.wikilab.android.friendica01.R;

/** @author Hanno - Felix Wagner */
public class TimelineEventMapPopup extends InfoWindow {

	ListView listview = null;

	/** @param layoutResId
	 * @param mapView */
	public TimelineEventMapPopup(int layoutResId, MapView mapView) {
		super(layoutResId, mapView);


		// default behavior: close it when clicking on the bubble:
		mView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (e.getAction() == MotionEvent.ACTION_UP)
					close();
				return true; // TODO: should consume the event - but it doesn't!
			}
		});
	}

	@Override
	public void onOpen(ExtendedOverlayItem item) {
		TimelineEventAdapter adapter = new TimelineEventAdapter(this.mMapView.getContext(), ((TimelineEventItem) item).getTimelineEvents());
		this.listview=(ListView) this.mView.findViewById(R.id.timelineEventsList);
		this.listview.setAdapter(adapter);
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub
	}
}
