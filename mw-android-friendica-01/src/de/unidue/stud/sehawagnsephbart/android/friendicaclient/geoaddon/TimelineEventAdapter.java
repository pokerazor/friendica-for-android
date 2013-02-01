package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import de.wikilab.android.friendica01.R;

public class TimelineEventAdapter extends ArrayAdapter<TimelineEvent> {
	protected TimelineEventViewHolder timEvViHo=null;

	protected static class TimelineEventViewHolder {
		protected ImageView bubble_image=null;
		protected TextView bubble_title=null;
		protected Button bubble_moreinfo=null;
		protected TextView bubble_description=null;
		protected TextView bubble_subdescription=null;

		public TimelineEventViewHolder() {
		}
	}
	
	public TimelineEventAdapter(Context context, List<TimelineEvent> objects) {
		super(context,R.layout.map_popup_element,objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
//			timEvViHo.bubble_moreinfo.setText(item.getId());
		} catch (Exception e) {
			timEvViHo.bubble_description.setText("xxInvalid Dataset!");
			e.printStackTrace();
		}
		
		return convertView;
	}
}