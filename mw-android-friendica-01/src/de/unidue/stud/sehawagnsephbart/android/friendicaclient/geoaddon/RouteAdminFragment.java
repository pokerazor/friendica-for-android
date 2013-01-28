package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.GenericJsonAdapter;
import de.wikilab.android.friendica01.ContentFragment;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.PhotoGalleryAdapter;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.UserProfileActivity;

public class RouteAdminFragment extends ContentFragment {
	ListView list;
	private Friendica friendicaAbstraction;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.route_admin, container, false);
		list = (ListView) myView.findViewById(R.id.routesList);
		friendicaAbstraction = new Friendica();
		friendicaAbstraction.setContext(getActivity());

		return myView;
	}

	public void onNavigate(String target) {
		loadList();
	}

	public void loadList() {
		friendicaAbstraction.executeAjaxQuery("statuses/friends", new JsonFinishReaction<ArrayList<JSONObject>>() {
			
			
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				ArrayList<JSONObject> myRoutes = result.getResult();
				for (JSONObject jsonObject : myRoutes) {
					System.out.println(jsonObject);
				}
				
				list.setAdapter(new GenericJsonAdapter(getActivity(), myRoutes));

					
				//displayList();
			}
		});
	}

	void displayList() {
		String in = "<empty>";

		try {
			JSONArray j = (JSONArray) new JSONTokener(in).nextValue();
			PhotoGalleryAdapter.Pic[] picArray = new PhotoGalleryAdapter.Pic[j.length()];

			for (int i = 0; i < j.length(); i++) {
				JSONObject d = j.getJSONObject(i);
				picArray[i] = new PhotoGalleryAdapter.Pic(d.getString("profile_image_url").replace("-6", "-4"), Max.IMG_CACHE_DIR + "/friend_pi_" + d.getString("id") + "_.jpg", d.getString("name"), d.getString("id"), d);
			}

			PhotoGalleryAdapter pga = new PhotoGalleryAdapter(getActivity(), R.layout.friendlist_item, picArray);
			list.setAdapter(pga);

			pga.clickListener = new View.OnClickListener() {
				@Override
				public void onClick(View arg1) {
					Log.i("FriendListFragment", "click!");
					PhotoGalleryAdapter.Pic p = (PhotoGalleryAdapter.Pic) arg1.getTag();

					String userId = p.data1;
					Toast.makeText(getActivity(), "userId " + userId, Toast.LENGTH_SHORT).show();

					Intent i = new Intent(getActivity(), UserProfileActivity.class);
					i.putExtra("userId", userId);
					startActivity(i);
				}
			};

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}