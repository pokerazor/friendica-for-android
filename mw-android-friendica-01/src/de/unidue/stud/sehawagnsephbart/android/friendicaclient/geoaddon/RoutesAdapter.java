package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.GenericJsonAdapter;
import de.wikilab.android.friendica01.R;

public class RoutesAdapter extends GenericJsonAdapter {
	protected RouteViewHolder H = null;

	protected static class RouteViewHolder extends GenericJsonViewHolder {
		protected Integer routeId = null;
		protected CheckedTextView routeName = null;
		protected CheckBox routeActiveCheckbox = null;
		protected Boolean routeActive = false;

		public RouteViewHolder() {
		}
	}

	public RoutesAdapter(Context context) {
		super(context, null);
	}

	public RoutesAdapter(Context context, List<JSONObject> objects) {
		super(context, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			H = new RouteViewHolder();
			convertView = inf.inflate(getElementLayoutId(), null);
			
			H.routeName = (CheckedTextView) convertView.findViewById(android.R.id.text1);

//			H.routeName = (TextView) convertView.findViewById(R.id.rawjson);
//			H.routeActiveCheckbox = (CheckBox) convertView.findViewById(android.R.id.);
			/*
			H.routeActiveCheckbox.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					System.out.println(v.getTag());
					 ViewParent parentList = v.getParent().getParent();
					 System.out.println(parentList.getClass());
					
				}
			});
			*/

			convertView.setTag(H);
		} else {
			H = (RouteViewHolder) convertView.getTag();
		}

		JSONObject route = (JSONObject) getItem(position);

		try {
			H.routeId = route.getInt("id");
			H.routeName.setText(route.getString("name"));
			H.routeActive = route.getInt("active") == 1;
			H.routeName.setChecked(H.routeActive);
//			H.routeActiveCheckbox.setTag(H.routeId);
		} catch (Exception e) {
			H.routeName.setText("xxInvalid Dataset!");
		}

		return convertView;
	}
}