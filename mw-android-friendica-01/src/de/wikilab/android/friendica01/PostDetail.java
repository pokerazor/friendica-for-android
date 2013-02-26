package de.wikilab.android.friendica01;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;

public class PostDetail extends ContentFragment {
	protected ListView list;

	protected String refreshTarget;

	protected String conversationId;

	private Friendica friendicaAbstraction;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.pd_listviewinner, container, false);
		list = (ListView) myView.findViewById(R.id.listview);

		friendicaAbstraction = new Friendica(getActivity());

		return myView;
	}

	protected void onNavigate(String target) {
		if (target != null && target.startsWith("conversation:")) {
			conversationId = target.substring(13);
			loadInitialPost();
		}
	}

	public void loadInitialPost() {
		friendicaAbstraction.executeAjaxQuery("statuses/show/" + conversationId, new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				list.setAdapter(new PostListAdapter(getActivity(), result.getResult()));
				
				JSONObject firstElement = result.getResult().get(0);

				try { //TODO What for?
					if (firstElement.has("statusnet_conversation_id") && firstElement.getString("statusnet_conversation_id").equals("0") == false) {
						conversationId = firstElement.getString("statusnet_conversation_id");
					}
				} catch (JSONException e) {
					list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[] { "Error: " + e.getMessage() }));
				}
				fillCommentList(conversationId,list);
			}
		});
	}
	
	public void fillCommentList(String conversationId, final ListView list) {
		HashMap<String,String> arguments = new HashMap<String, String>();
		arguments.put("conversation","true");
		friendicaAbstraction.executeAjaxQuery("statuses/show/" + conversationId,arguments ,new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				PostListAdapter pla = new PostListAdapter(getActivity(),  result.getResult());
				pla.isPostDetails = true;
				list.setAdapter(pla);
			}
		}, true);
	}
}