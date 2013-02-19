package de.wikilab.android.friendica01;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;

public class PostListFragment extends ContentFragment {
	private static final String TAG = "Friendica/PostListFragment";

	private static final Integer TIMELINE_MODE_HOME = 0;
	private static final Integer TIMELINE_MODE_USER = 1;

	protected Friendica friendicaAbstraction = null;

	PullToRefreshListView refreshListView;
	ListView list;
	ListAdapter listAdapter;

	String refreshTarget;

	final int ITEMS_PER_PAGE = 20;
	int curLoadPage = 1;
	boolean loadFinished = false;

	HashSet<Long> containedIds = new HashSet<Long>();

	/*@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setRetainInstance(true);
	}*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "==> onCreateView ");

		friendicaAbstraction = new Friendica(getActivity());

		myView = inflater.inflate(R.layout.pl_listviewinner, container, false);
		refreshListView = (PullToRefreshListView) myView.findViewById(R.id.listview);
		list = refreshListView.getRefreshableView();

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
			public void onItemClick(AdapterView<?> self, View view, int position, long id) {
				if (refreshTarget.equals("notifications")) {
					SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
					final Notification n = ((Notification.NotificationsListAdapter) getListAdapter()).getItem(position - 1);
					n.resolveTarget(getActivity(), new Runnable() {
						@Override
						public void run() {
							SendMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);
							if (n.targetComponent != null && n.targetComponent.equals("conversation:")) {
								SendMessage("Navigate Conversation", String.valueOf(n.targetData), null);
							} else {
								Max.alert(getActivity(), "Unable to navigate to notification target<br><br><a href='" + n.targetUrl + "'>" + n.targetUrl + "</a>", "Not implemented");
							}
						}
					});
				} else {
					SendMessage("Navigate Conversation", String.valueOf(id), null);
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

		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
		if (target != null && target.equals("mywall")) {
			SendMessage("Set Header Text", getString(R.string.mm_mywall), null);
			loadWall(null);
		} else if (target != null && target.startsWith("userwall:")) {
			SendMessage("Set Header Text", getString(R.string.mm_mywall), null);
			loadWall(target.substring(9));
		} else if (target != null && target.equals("notifications")) {
			SendMessage("Set Header Text", getString(R.string.mm_notifications), null);
			loadNotifications();
		} else {
			SendMessage("Set Header Text", getString(R.string.mm_timeline), null);
			loadTimeline();
		}
	}

	public void hideProgBar() {
		/*refreshListView.setAddStatesFromChildren(addsStates)
		list.setVisibility(View.VISIBLE);
		progbar.setVisibility(View.GONE);*/
		try {
			if (curLoadPage == 1)
				refreshListView.onRefreshComplete();

			SendMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);
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
		Toast.makeText(getActivity(), "Done loading more items - scroll down :)", Toast.LENGTH_SHORT).show();
		loadFinished = true;
	}

	public void loadTimeline(Integer mode, String userId) {
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("count", String.valueOf(ITEMS_PER_PAGE));
		arguments.put("page", String.valueOf(curLoadPage));
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
		});
	}

	public void loadTimeline() {
		loadTimeline(TIMELINE_MODE_HOME, null);
	}

	public void loadWall(String userId) {
		loadTimeline(TIMELINE_MODE_USER, userId);
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