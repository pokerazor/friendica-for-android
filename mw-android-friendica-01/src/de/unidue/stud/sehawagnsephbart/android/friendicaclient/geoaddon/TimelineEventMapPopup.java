/**
 * 
 */
package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.wikilab.android.friendica01.PostBarModule;
import de.wikilab.android.friendica01.R;

/** @author Hanno - Felix Wagner */
public class TimelineEventMapPopup extends InfoWindow {
	TimelineEventAdapter<TimelineEventMapPopup> adapter = null;

	protected ListView listview = null;
	protected ImageView fullsizeImage = null;
	protected LinearLayout postBarLayout = null;
	protected PostBarModule postBar = null;

	protected Boolean newPost = false;

	public TimelineEventMapPopup(int layoutResId, MapView mapView) {
		this(layoutResId, mapView, false);
	}

	/** @param layoutResId
	 * @param mapView */
	public TimelineEventMapPopup(int layoutResId, MapView mapView, Boolean newPost) {
		super(layoutResId, mapView);

		this.newPost = newPost;

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
		this.listview = (ListView) this.mView.findViewById(R.id.timelineEventsList);
		this.fullsizeImage = (ImageView) this.mView.findViewById(R.id.fullsizeImage);
		this.postBarLayout = (LinearLayout) this.mView.findViewById(R.id.post_bar);
		fullsizeImage.setVisibility(ListView.GONE);

		if (newPost) {
			postBar = new PostBarModule(this.mMapView.getContext());
			postBar.initialize(mView);
			postBar.setLocation(item.getPoint());

			listview.setVisibility(ListView.GONE);
			postBarLayout.setVisibility(ListView.VISIBLE);
		} else {
			ArrayList<TimelineEvent> timelineEvents = ((TimelineEventItem) item).getTimelineEvents();
			if (timelineEvents != null && !timelineEvents.isEmpty()) {
				this.adapter = new TimelineEventAdapter<TimelineEventMapPopup>(this.mMapView.getContext(), timelineEvents, this);
			}

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
					((ImageView) arg1.findViewById(R.id.bubble_image)).performClick();
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
			if (adapter != null) {
				this.listview.setAdapter(adapter);
			}

			listview.setVisibility(ListView.VISIBLE);
			postBarLayout.setVisibility(ListView.GONE);
		}

	}

	public void setFullsizeImage(Drawable fullsizeImageDrawable, String originalURI) {
		listview.setVisibility(ListView.GONE);
		postBarLayout.setVisibility(ListView.GONE);
		fullsizeImage.setVisibility(ListView.VISIBLE);
		fullsizeImage.setImageDrawable(fullsizeImageDrawable);
		if (originalURI != null && !originalURI.equals("")) {
			String scaledUri = Friendica.getScaledImageURI(originalURI, 0);
			Friendica.placeImageFromURI(scaledUri, fullsizeImage, mView.getContext(), "pi_");
		}
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub
	}
}
