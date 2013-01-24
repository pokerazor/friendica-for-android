package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;

public class TimelineEvent {

	public static final Integer TYPE_DEFAULT = 0;
	public static final Integer TYPE_STATUS = 1;
	public static final Integer TYPE_IMAGE = 2;
	public static final Integer TYPE_FRIENDSHIP = 3;

	protected Integer type;
	protected String text;
	protected String dateTime;
	protected Long createdAt;

	protected Drawable image;
	protected long date;


	public TimelineEvent(JSONObject jsonPost) {
		try {
			this.setId(Integer.parseInt(jsonPost.getString("id")));
			this.setText(jsonPost.getString("text"));
			this.setDateTime(jsonPost.getString("created_at"));
			
			this.setDate(java.util.Date.parse(getDateTime()));

			this.setType(TYPE_STATUS);
			String coordinates = jsonPost.getString("coordinates");

			if (!coordinates.equals("")) {
				String[] splitcoordinates = coordinates.split(" ");
				GeoPoint gp = new GeoPoint(Double.parseDouble(splitcoordinates[0]), Double.parseDouble(splitcoordinates[1]));
				this.setLocation(gp);
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public TimelineEvent() {
	}

	public Drawable getImage() {
		return image;
	}

	public void setImage(Drawable image) {
		this.image = image;
	}

	protected GeoPoint location;
	protected Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {

		this.dateTime = dateTime;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}
	
	public long getDate() {
		return date;
	}
	
	public String getRelativeDate(Context context){
		return DateUtils.getRelativeDateTimeString(context, getDate(),DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_24HOUR).toString();
	}

	public void setDate(long l) {
		this.date = l;
	}


}
