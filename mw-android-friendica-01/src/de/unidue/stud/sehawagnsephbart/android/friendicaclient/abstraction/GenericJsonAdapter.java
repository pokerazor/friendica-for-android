package de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.wikilab.android.friendica01.R;

public class GenericJsonAdapter extends ArrayAdapter<JSONObject> {
	protected Integer elementLayoutId=R.layout.gen_jsonobject;
	protected GenericJsonViewHolder H=null;

	protected static class GenericJsonViewHolder {
		protected TextView rawjson=null;
		public GenericJsonViewHolder() {
		}
	}
	
	public void setElementLayoutId(Integer elementLayoutId){
		this.elementLayoutId=elementLayoutId;
	}
	public Integer getElementLayoutId(){
		return this.elementLayoutId;
	}
	
	public GenericJsonAdapter(Context context, List<JSONObject> objects) {
		super(context, R.layout.gen_jsonobject, objects);

	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	@Override
	public long getItemId(int position) {
		try {
			return ((JSONObject) getItem(position)).getLong("id");
		} catch (JSONException e) {
			return 0;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			H = new GenericJsonViewHolder();
			convertView = inf.inflate(getElementLayoutId(), null);
			H.rawjson = (TextView) convertView.findViewById(R.id.rawjson);
			convertView.setTag(H);
		} else {
			H = (GenericJsonViewHolder) convertView.getTag();
		}

		JSONObject post = (JSONObject) getItem(position);

		try {
			H.rawjson.setText(post.toString());
		} catch (Exception e) {
			H.rawjson.setText("xxInvalid Dataset!");
		}

		return convertView;
	}
}