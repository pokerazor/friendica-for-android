package de.wikilab.android.friendica01;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.WrapperListAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon.TimelineEvent;

public class PostListFragment extends ContentFragment {
	private static final String TAG = "Friendica/PostListFragment";

	private static final Integer TIMELINE_MODE_HOME = 0;
	private static final Integer TIMELINE_MODE_USER = 1;

	protected Friendica friendicaAbstraction = null;

	PullToRefreshListView refreshListView;
	ListView list;
	ListAdapter listAdapter;
	
	private boolean locationListenerAttached;
	private final LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	        double longitude = location.getLongitude();
	        double latitude = location.getLatitude();
	        viewLatLon.setText(getString(R.string.viewLatLon)+"\n"+"Lat="+String.valueOf(latitude)+"  Long="+String.valueOf(longitude));
	    }

		@Override
		public void onProviderDisabled(String provider) {
			viewLatLon.setText(getString(R.string.viewLatLon)+"\nDisabled");
		}

		@Override
		public void onProviderEnabled(String provider) {
			viewLatLon.setText(getString(R.string.viewLatLon)+"\nEnabled");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			viewLatLon.setText(getString(R.string.viewLatLon)+"\nStatus="+String.valueOf(status));
		}
	};

	String refreshTarget;
	
	ImageButton submitBtn;
	TextView viewLatLon;

	final int ITEMS_PER_PAGE = 20;
	int curLoadPage = 1;
	boolean loadFinished = false;

	HashSet<Long> containedIds = new HashSet<Long>();

	private LinearLayout lastOpenedPost;

	public void fillCommentList(String conversationId, final LinearLayout list) {
		list.removeAllViews();
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("conversation", "true");
		friendicaAbstraction.executeAjaxQuery("statuses/show/" + conversationId, arguments, new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				result.getResult().remove(0);

				for (JSONObject curElement : result.getResult()) {
					TimelineEvent timelineEvent=new TimelineEvent(curElement);
					TextView commentTextView=new TextView(getActivity());
					commentTextView.setText(timelineEvent.getSpannableHtml());
					list.addView(commentTextView);
					list.requestLayout();
				}
			}
		}, true);
	}

	/*@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setRetainInstance(true);
	}*/
	
	void detachLocationListener() {
		if (locationListenerAttached) {
			LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(locationListener);
			this.locationListenerAttached = false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "==> onCreateView ");

		friendicaAbstraction = new Friendica(getActivity());

		myView = inflater.inflate(R.layout.pl_listviewinner, container, false);
		refreshListView = (PullToRefreshListView) myView.findViewById(R.id.listview);
		list = refreshListView.getRefreshableView();
		
        viewLatLon = (TextView) myView.findViewById(R.id.viewLatLon);
        
        submitBtn = (ImageButton) myView.findViewById(R.id.btn_submit);
        submitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
        
        
        
        
        ToggleButton sendLatLon = (ToggleButton) myView.findViewById(R.id.sendLatLon);
        sendLatLon.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (!locationListenerAttached) {
						LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
						Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						
						lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
						
						viewLatLon.setText(getString(R.string.viewLatLon)+"\n"+"Loading...");
						locationListenerAttached = true;
					}
				} else {
					detachLocationListener();
					
				}
			}
		});
		
		
		

		refreshListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (loadFinished) {
					curLoadPage = 1;
					onNavigate(refreshTarget);
				}
			}
		});

		refreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (loadFinished && getPostListAdapter() != null) {
					Toast.makeText(getActivity(), "Loading more items...", Toast.LENGTH_SHORT).show();
					curLoadPage++;
					onNavigate(refreshTarget);
				} else {
					Log.i(TAG, "OnLastItemVisibleListener -- skip! lf=" + loadFinished + " listAdapter:" + list.getAdapter().getClass().toString());
				}
			}
		});

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> self, final View view, int position, long id) {
				if (refreshTarget.equals("notifications")) {
					SendMessage(FRGM_MSG_SHW_LOADING_ANIMATION, Integer.valueOf(View.VISIBLE), null);
					final Notification n = ((Notification.NotificationsListAdapter) getListAdapter()).getItem(position - 1);
					n.resolveTarget(getActivity(), new Runnable() {
						@Override
						public void run() {
							SendMessage(FRGM_MSG_SHW_LOADING_ANIMATION, Integer.valueOf(View.INVISIBLE), null);
							if (n.targetComponent != null && n.targetComponent.equals("conversation:")) {
								SendMessage(FRGM_MSG_NAV_CONVERSATION, String.valueOf(n.targetData), null);
							} else {
								Max.alert(getActivity(), "Unable to navigate to notification target<br><br><a href='" + n.targetUrl + "'>" + n.targetUrl + "</a>", "Not implemented");
							}
						}
					});
				} else {
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					if (lastOpenedPost != null) {						
						lastOpenedPost.removeView(lastOpenedPost.findViewById(R.id.timelineItemDetailsBar));
					}

					final LinearLayout postView = (LinearLayout) view.findViewById(R.id.postLinearInner);
					final TimelineEvent timelineEvent = (TimelineEvent) view.getTag(R.id.postLinearRoot);
					if (postView != null) { //TODO only for comments, not for images yet
						lastOpenedPost = (LinearLayout) postView;
					
						View detailsBar = inflater.inflate(R.layout.post_item_detail, postView);

						TextView coordinates = (TextView) postView.findViewById(R.id.coordinates);
						coordinates.setText("My coords");
						
						fillCommentList(id + "", (LinearLayout) detailsBar.findViewById(R.id.listview));
						
						((Button) postView.findViewById(R.id.btn_upload)).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								postComment(((EditText) postView.findViewById(R.id.comment_text)).getText().toString(),Long.valueOf(timelineEvent.getId()), view);
								Toast.makeText(getActivity(), "postButton clicked", Toast.LENGTH_SHORT).show();
							}
						});

					}
				}
			}
		});

		listAdapter = new PostListAdapter(getActivity(), new ArrayList<JSONObject>());
		list.setAdapter(listAdapter);

		if (listAdapter != null && getPostListAdapter() == null) {
			// navigate(refreshTarget);
			list.setAdapter(listAdapter);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey("listviewState")) {
			list.onRestoreInstanceState(savedInstanceState.getParcelable("listviewState"));
		}

		return myView;
	}
	
	private void sendMessage() {
		EditText txt_status = (EditText) myView.findViewById(R.id.text_input);
		ToggleButton geo_en = (ToggleButton) myView.findViewById(R.id.sendLatLon);
		
		final ProgressDialog pd = ProgressDialog.show(getActivity(), "Posting status...", "Please wait", true, false);
		
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.addPostData("status", txt_status.getText().toString());
		t.addPostData("source", "<a href='http://andfrnd.wikilab.de'>Friendica for Android</a>");
		if (geo_en.isChecked()) {
			LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE); 
			Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null) {
				Toast.makeText(getActivity(), "Unable to get location info - please try again.", Toast.LENGTH_LONG).show();
				pd.dismiss();
				return;
			}
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();

			t.addPostData("lat", String.valueOf(latitude));
			t.addPostData("long", String.valueOf(longitude));
		}
		t.postData(Max.getServer(getActivity()) + "/api/statuses/update.json", new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				//getActivity().finish();
				((FragmentParentListener)getActivity()).OnFragmentMessage("Finished", null, null);
			}
		});
	}
	
	protected void postComment(String commentText, final Long inReplyTo, final View view) {
//		Toast.makeText(getActivity(), "postComment "+commentText+" "+inReplyTo, Toast.LENGTH_SHORT).show();
		
		SendMessage(FRGM_MSG_SHW_LOADING_ANIMATION, Integer.valueOf(View.VISIBLE), null);
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.addPostData("status", commentText);
		t.addPostData("source", getString(R.string.app_title_html));
		t.addPostData("in_reply_to_status_id", inReplyTo.toString());
		((Button) view.findViewById(R.id.btn_upload)).setEnabled(false);
		t.postData(Max.getServer(getActivity()) + "/api/statuses/update", new Runnable() {
			@Override
			public void run() {
				((EditText) view.findViewById(R.id.comment_text)).setText("");
				((Button) view.findViewById(R.id.btn_upload)).setEnabled(true);
				fillCommentList(inReplyTo.toString(),(LinearLayout) view.findViewById(R.id.listview));
			}
		});
		
	}
	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("listviewState", list.onSaveInstanceState());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d(TAG, "==> onStart ");
	}

	protected void onNavigate(String target) {
		/*if (myView != null) {
			list.setVisibility(View.GONE);
			progbar.setVisibility(View.VISIBLE);
		}*/
		if (curLoadPage == 1)
			refreshListView.setRefreshing();
		refreshTarget = target;
		loadFinished = false;

		SendMessage(FRGM_MSG_SHW_LOADING_ANIMATION, Integer.valueOf(View.VISIBLE), null);
		if (target != null && target.equals("mywall")) {
			SendMessage(FRGM_MSG_SET_HEADERTEXT, getString(R.string.mm_mywall), null);
			loadWall(null);
		} else if (target != null && target.startsWith("userwall:")) {
			SendMessage(FRGM_MSG_SET_HEADERTEXT, getString(R.string.mm_mywall), null);
			loadWall(target.substring(9));
		} else if (target != null && target.equals("notifications")) {
			SendMessage(FRGM_MSG_SET_HEADERTEXT, getString(R.string.mm_notifications), null);
			loadNotifications();
		} else {
			SendMessage(FRGM_MSG_SET_HEADERTEXT, getString(R.string.mm_timeline), null);
			loadTimeline();
		}
	}

	public void hideProgBar() {
		try {
			if (curLoadPage == 1)
				refreshListView.onRefreshComplete();

			SendMessage(FRGM_MSG_SHW_LOADING_ANIMATION, Integer.valueOf(View.INVISIBLE), null);
		} catch (Exception ignoreException) {
		}

	}

	private PostListAdapter getPostListAdapter() {
		Adapter a = getListAdapter();
		if (a instanceof PostListAdapter)
			return (PostListAdapter) a;
		return null;
	}

	private Adapter getListAdapter() {
		Adapter a = list.getAdapter();
		if (a instanceof WrapperListAdapter)
			a = ((WrapperListAdapter) a).getWrappedAdapter();
		return a;
	}

	private void setItems(ArrayList<JSONObject> resultArray) {
		PostListAdapter curContent = getPostListAdapter();
		curContent.addAll(resultArray);
		curContent.notifyDataSetChanged();
		Toast.makeText(getActivity(), "More posts retrieved. scroll down", Toast.LENGTH_SHORT).show();
		loadFinished = true;
	}

	public void loadWall(String userId) {
		loadTimeline(TIMELINE_MODE_USER, userId);
	}

	public void loadTimeline() {
		loadTimeline(TIMELINE_MODE_HOME, null);
	}

	public void loadTimeline(final Integer mode, final String userId) {
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("count", String.valueOf(ITEMS_PER_PAGE));
		arguments.put("page", String.valueOf(curLoadPage));
		arguments.put("exclude_replies", "1");

		if (userId != null && !userId.equals("")) {
			arguments.put("user_id", userId);
		}
		String targetpath = "";
		if (mode == TIMELINE_MODE_HOME) {
			targetpath = "statuses/home_timeline";
		} else if (mode == TIMELINE_MODE_USER) {
			targetpath = "statuses/user_timeline";
		}

		friendicaAbstraction.executeAjaxQuery(targetpath, arguments, new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				setItems(result.getResult());
				hideProgBar();
			}
		}, true);
	}

	void loadNotifications() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlXmlDocument(Max.getServer(getActivity()) + "/ping", new Runnable() {
			@Override
			public void run() {
				try {
					Document xd = t.getXmlDocumentResult();
					Node el = xd.getElementsByTagName("notif").item(0);
					ArrayList<Notification> notifs = new ArrayList<Notification>();

					for (int i = 0; i < el.getChildNodes().getLength(); i++) {
						if (el.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
							notifs.add(Notification.fromXmlNode(el.getChildNodes().item(i)));
						}
					}
					// ListView lvw = (ListView) findViewById(R.id.listview);
					listAdapter = new Notification.NotificationsListAdapter(getActivity(), notifs);
					list.setAdapter(listAdapter);

				} catch (Exception e) {
					if (list != null)
						list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[] { "Error: " + e.getMessage(), Max.Hexdump(t.getResult().getBytes()) }));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});
	}
}