package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.wikilab.android.friendica01.R;

public class TimelineEventAdapter<mPopup> extends ArrayAdapter<TimelineEvent> {
	
	protected static class TimelineEventViewHolder {
		protected ImageView bubble_image = null;
		protected TextView bubble_title = null;
		protected Button bubble_moreinfo = null;
		protected TextView bubble_description = null;
		protected TextView bubble_subdescription = null;

		public TimelineEventViewHolder() {
		}
	}
	
	protected TimelineEventMapPopup mPopup = null;
	protected LinearLayout bubble = null;
	private ImageView touchedImage;
	protected TimelineEventViewHolder timEvViHo = null;
	
	public TimelineEventAdapter(Context context, List<TimelineEvent> objects,TimelineEventMapPopup mPopup) {
		this(context,objects);
		this.mPopup=mPopup;
	}

	public TimelineEventAdapter(Context context, List<TimelineEvent> objects) {
		super(context, R.layout.map_popup_element, objects);
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		bubble = (LinearLayout) parent.getParent();
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			timEvViHo = new TimelineEventViewHolder();
			convertView = inf.inflate(R.layout.map_popup_element, null);
			timEvViHo.bubble_title = (TextView) convertView.findViewById(R.id.bubble_title);
			timEvViHo.bubble_description = (TextView) convertView.findViewById(R.id.bubble_description);
			timEvViHo.bubble_subdescription = (TextView) convertView.findViewById(R.id.bubble_subdescription);
			timEvViHo.bubble_image = (ImageView) convertView.findViewById(R.id.bubble_image);
			timEvViHo.bubble_moreinfo = (Button) convertView.findViewById(R.id.bubble_moreinfo);

			convertView.setTag(timEvViHo);

		} else {
			timEvViHo = (TimelineEventViewHolder) convertView.getTag();
		}

		TimelineEvent item = getItem(position);

		try {
			timEvViHo.bubble_description.setText(item.getId() + ": " + item.getText());
			timEvViHo.bubble_title.setText("Posted:");
			timEvViHo.bubble_subdescription.setText(item.getRelativeDate(getContext()));
			timEvViHo.bubble_image.setImageDrawable(item.getImage());
			timEvViHo.bubble_image.setTag(R.id.caption, item.getId());
			timEvViHo.bubble_image.setTag(R.id.subject, item.getImageURI());
			timEvViHo.bubble_image.setTag(R.id.active, item.getType());
			timEvViHo.bubble_image.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					
					return false; //event not consumed, pass on
				}
			});
			timEvViHo.bubble_image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {					
					touchedImage=((ImageView)v);
					if(touchedImage.getTag(R.id.active)==TimelineEvent.TYPE_IMAGE){
						String originalURI=(String) touchedImage.getTag(R.id.subject);
						Drawable image=touchedImage.getDrawable();
						mPopup.setFullsizeImage(image,originalURI);
					}
				}
			});

		} catch (Exception e) {
			timEvViHo.bubble_description.setText("xxInvalid Dataset!");
			e.printStackTrace();
		}
		return convertView;
	}
}