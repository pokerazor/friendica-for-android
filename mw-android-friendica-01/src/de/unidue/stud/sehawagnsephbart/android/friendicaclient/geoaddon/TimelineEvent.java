package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ImageSpan;
import android.widget.ImageView;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;

public class TimelineEvent {

	public static final Integer TYPE_DEFAULT = 0;
	public static final Integer TYPE_STATUS = 1;
	public static final Integer TYPE_IMAGE = 2;
	public static final Integer TYPE_FRIENDSHIP = 3;
	public static final Integer TYPE_COMMENT = 4;
	public static final Integer TYPE_LIKE = 5;

	protected ImageView tempImageHolder = null;

	protected Integer type = -1;
	protected String dateTime = "";
	protected Long createdAt = (long) 0;

	protected Drawable image = null;
	protected String imageURI = "";
	protected Long date = (long) 0;
	protected Long inReplyTo = (long) 0;


	protected GeoPoint location = null;
	protected Integer id = -1;
	protected String text = "";
	protected JSONObject jsonPost;

	protected String html = "";

	public TimelineEvent(JSONObject jsonPost) {
		this.jsonPost = jsonPost;
		try {

			if (jsonPost.getString("verb").equals("http://activitystrea.ms/schema/1.0/like")) {
				this.setType(TYPE_LIKE);
			} else if (jsonPost.has("in_reply_to_status_id") && jsonPost.getString("in_reply_to_status_id").equals("0") == false) {
				this.setType(TYPE_LIKE);
			} else if (jsonPost.getString("statusnet_html").contains("<img")) {
				this.setType(TYPE_IMAGE);
			} else {
				this.setType(TYPE_DEFAULT); //FIXME not used?
				this.setType(TYPE_STATUS);

			}

			this.setId(Integer.parseInt(jsonPost.getString("id")));
			this.setText(jsonPost.getString("text"));
			this.setDateTime(jsonPost.getString("created_at"));
			this.setInReplyTo(jsonPost.getLong("in_reply_to_status_id"));


			this.setDate(java.util.Date.parse(getDateTime()));

			this.setHtml(jsonPost.getString("statusnet_html"));
			this.determineImage();

// System.err.println(jsonPost.getString("statusnet_html"));

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

	public String getCleanedHtml() {
		return html.replaceAll("(<br[^>]*>|</?div[^>]*>|</?p>)", "  ");
	}

	public Spannable getSpannableHtml() {
		html = getCleanedHtml();
		Spanned spanned = Html.fromHtml(html);
		Spannable htmlSpannable;
		if (spanned instanceof SpannableStringBuilder) {
			htmlSpannable = (SpannableStringBuilder) spanned;
		} else {
			htmlSpannable = new SpannableStringBuilder(spanned);
		}
		return htmlSpannable;
	}

	private void determineImage() {
		if (this.type == TYPE_IMAGE) {
			this.setImageURI(Friendica.getImageURIFromPost(getSpannableHtml()));
			// this.setImage(image);
		}
	}

	public void processImage(Context context) { 		// TODO handle asynchronously
		if (tempImageHolder == null) {
			tempImageHolder = new ImageView(context);
		}
		Friendica.displayProfileImageFromPost(jsonPost, tempImageHolder, context);
		this.loadImageIntoTarget(tempImageHolder, context); // if event is image, override profile image with actual image
		this.setImage(tempImageHolder.getDrawable());
	}

	public void placeImages(ImageView[] imageViews,Context context) {
		ImageSpan[] images = Friendica.getImagesFromPost(getSpannableHtml());
		
		for (int i = 0; i < imageViews.length; i++) {
			ImageView curImageView = imageViews[i];
			if(images.length>i && images[i]!=null){
				Friendica.placeImageFromURI(images[i].getSource(), curImageView, context, "pi");
			}
		}
	}

	public void loadImageIntoTarget(ImageView target, Context context) {
		if (this.getType() == TYPE_IMAGE) {
			Friendica.placeImageFromURI(this.getImageURI(), target, context, "pi");
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

	public Long getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(Long inReplyTo) {
		this.inReplyTo = inReplyTo;
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

	public String getRelativeDate(Context context) {
		return DateUtils.getRelativeDateTimeString(context, getDate(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_24HOUR).toString();
	}

	public void setDate(long l) {
		this.date = l;
	}

	public String getImageURI() {
		return imageURI;
	}

	public void setImageURI(String imageURI) {
		this.imageURI = imageURI;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public ImageView getTempImageHolder() {
		return tempImageHolder;
	}

	public void setTempImageHolder(ImageView tempImageHolder) {
		this.tempImageHolder = tempImageHolder;
	}
}