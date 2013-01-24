package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import org.osmdroid.util.GeoPoint;

public class TimelineEvent {

	public static final Integer TYPE_DEFAULT = 0;
	public static final Integer TYPE_STATUS = 1;
	public static final Integer TYPE_IMAGE = 2;
	public static final Integer TYPE_FRIENDSHIP = 3;

	protected Integer type;
	protected String text;
	protected String dateTime;
	protected boolean image;
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



}
