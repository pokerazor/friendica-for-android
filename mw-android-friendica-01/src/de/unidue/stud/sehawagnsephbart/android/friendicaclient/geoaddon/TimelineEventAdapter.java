package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
//		bubble.removeAllViews();
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
			timEvViHo.bubble_image.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					
					touchedImage=((ImageView)v);
					String originalURI=(String) touchedImage.getTag(R.id.subject);
					Drawable image=touchedImage.getDrawable();
					mPopup.setFullsizeImage(image,originalURI);
					
//					Toast blabla = Toast.makeText(getContext(), "image touched "+v.getTag(R.id.caption), Toast.LENGTH_SHORT);
//					blabla.show();
//					ViewParent vadda = v.getParent().getParent();
//					blabla = Toast.makeText(getContext(), "parent= "+parent, Toast.LENGTH_SHORT);
//					blabla.show();
//					blabla = Toast.makeText(getContext(), "vadda= "+vadda, Toast.LENGTH_SHORT);
//					blabla.show();
//					blabla = Toast.makeText(getContext(), "bubble= "+bubble, Toast.LENGTH_SHORT);
//					blabla.show();
					
//					View rootView = ((ListView) parent).getRootView();
//					blabla = Toast.makeText(getContext(), "rootView= "+rootView, Toast.LENGTH_SHORT);
//					blabla.show();
//					parent.removeAllViews();
//					parent.removeAllViewsInLayout();
//					parent.removeView(v);
//					((ViewGroup) v.getParent()).removeView(v);
//					ViewManager vMan=(ViewManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//					vMan.removeView(v);
//					vMan.addView(v, null);
//					bubble.getClass();
//					bubble.removeView(touchedImage);
//					bubble.findViewById(id);
//					bubble.se
//					((ImageView)v).setMinimumHeight(99999);
//					((ImageView)v).setMinimumWidth(99999);
//					((ImageView)v).setLayoutParams(params);
//					ScaleType scaleType=new ScaleType();
//					((ImageView)v).setScaleType();

//					int height = ((ImageView)v).getDrawable().getIntrinsicHeight();
//					blabla = Toast.makeText(getContext(), "IntrinsicHeight= "+height, Toast.LENGTH_SHORT);
//					blabla.show();
//					height = ((ImageView)v).getDrawable().getMinimumHeight();
//					blabla = Toast.makeText(getContext(), "getMinimumHeight= "+height, Toast.LENGTH_SHORT);
//					blabla.show();
//					height = ((ImageView)v).getHeight();
//					blabla = Toast.makeText(getContext(), "getHeight= "+height, Toast.LENGTH_SHORT);
//					blabla.show();
					

					
//					touchedImage.getLayoutParams().height=image.getIntrinsicHeight();
//					touchedImage.getLayoutParams().width=image.getIntrinsicWidth();
					
//					height = ((ImageView)v).getMeasuredHeight();
//					blabla = Toast.makeText(getContext(), "getMeasuredHeight= "+height, Toast.LENGTH_SHORT);
//					blabla.show();
//					
//					float heightfl = ((ImageView)v).getScaleX();
//					blabla = Toast.makeText(getContext(), "getScaleX= "+heightfl, Toast.LENGTH_SHORT);
//					blabla.show();
					
//					((ViewGroup) bubble).addView(v);
					
//					blabla = Toast.makeText(getContext(), "v.getId= "+v.getId(), Toast.LENGTH_SHORT);
//					blabla.show();


					
//					parent.
					return false; //event not consumed, pass on
				}
			});
			timEvViHo.bubble_image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					Toast blabla = Toast.makeText(getContext(), "image clicked "+v.getTag(R.id.caption), Toast.LENGTH_SHORT);
//					blabla.show();
//					System.out.println(v.getParent());
//					parent.removeAllViews();
//					parent.removeAllViewsInLayout();

				}
			});

// timEvViHo.bubble_moreinfo.setText(item.getId());
		} catch (Exception e) {
			timEvViHo.bubble_description.setText("xxInvalid Dataset!");
			e.printStackTrace();
		}

		return convertView;
	}
	/*
	public static View getMyBubble(View v){
		if(v instanceof TimelineEventMapPopup){
			
		}
	}
	*/
	
}