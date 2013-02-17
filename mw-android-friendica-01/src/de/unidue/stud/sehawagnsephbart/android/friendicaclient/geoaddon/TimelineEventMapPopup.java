/**
 * 
 */
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.wikilab.android.friendica01.R;

/** @author Hanno - Felix Wagner */
public class TimelineEventMapPopup extends InfoWindow {
	TimelineEventAdapter adapter = null;

	ListView listview = null;
	ImageView fullsizeImage = null;

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
		this.adapter = new TimelineEventAdapter(this.mMapView.getContext(), ((TimelineEventItem) item).getTimelineEvents(),this);
		this.listview=(ListView) this.mView.findViewById(R.id.timelineEventsList);
		this.fullsizeImage=(ImageView) this.mView.findViewById(R.id.fullsizeImage);
		
		listview.setVisibility(ListView.VISIBLE);
		fullsizeImage.setVisibility(ListView.GONE);

		/*
		this.listview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast blabla = Toast.makeText(mView.getContext(), "TEMPlistview clicked "+v.getTag(0), Toast.LENGTH_SHORT);
				blabla.show();
			}
		});
		*/
		this.listview.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
/*				Toast blabla = Toast.makeText(mView.getContext(), "TEMPlistview touched "+v.getTag(0)+" "+event, Toast.LENGTH_SHORT);
				blabla.show();*/
				return false;
			}
		});
		this.listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				Toast blabla = Toast.makeText(mView.getContext(), "TEMPlistview itemclicked "+arg0+arg1+arg2+arg3, Toast.LENGTH_SHORT);
				Integer itemId=(Integer) arg1.findViewById(R.id.bubble_image).getTag(R.id.caption);
				((ImageView) arg1.findViewById(R.id.bubble_image)).performClick();
				Toast blabla = Toast.makeText(mView.getContext(), "itemclicked, id= "+itemId, Toast.LENGTH_SHORT);

				blabla.show();
			}
		});
		this.listview.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/*
				Toast blabla = Toast.makeText(mView.getContext(), "TEMPlistview itemselected "+arg0+arg1+arg2+arg3, Toast.LENGTH_SHORT);
				blabla.show();
				*/
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
/*				Toast blabla = Toast.makeText(mView.getContext(), "TEMPlistview noitemselected "+arg0, Toast.LENGTH_SHORT);
				blabla.show();*/
			}
		});
		this.listview.setAdapter(adapter);
	}

	public void setFullsizeImage(Drawable fullsizeImageDrawable, String originalURI){
		listview.setVisibility(ListView.GONE);
		fullsizeImage.setVisibility(ListView.VISIBLE);
		fullsizeImage.setImageDrawable(fullsizeImageDrawable);
		if(originalURI!=null && !originalURI.equals("")){
			String scaledUri=Friendica.getScaledImageURI(originalURI, 0);
			Friendica.placeImageFromURI(scaledUri, fullsizeImage, mView.getContext(), "pi_");
		}
	}
	
	@Override
	public void onClose() {
		// TODO Auto-generated method stub
	}
}
