package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.GenericJsonAdapter;
import de.wikilab.android.friendica01.ContentFragment;
import de.wikilab.android.friendica01.R;

public class RouteAdminFragment extends ContentFragment {
	ListView list;
	private Friendica friendicaAbstraction;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.route_admin, container, false);
		list = (ListView) myView.findViewById(R.id.routesList);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		friendicaAbstraction = new Friendica(getActivity());

		return myView;
	}

	public void onNavigate(String target) {
		loadList();
	}

	public void loadList() {
		friendicaAbstraction.executeAjaxQuery("routes/list", new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				ArrayList<JSONObject> myRoutes = result.getResult();
				for (JSONObject jsonObject : myRoutes) {
//					System.out.println(jsonObject);
				}
//				list.setAdapter(new GenericJsonAdapter(getActivity(), myRoutes));

				list.setAdapter(new RoutesAdapter(getActivity(), myRoutes));

				((GenericJsonAdapter) list.getAdapter()).setElementLayoutId(android.R.layout.simple_list_item_multiple_choice);

				/*	
				((GenericJsonAdapter) list.getAdapter()).setElementLayoutId(R.layout.routeadmin_route);
				*/
				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> self, View view, int position, long id) {
						try {
							Integer routeId = ((JSONObject) self.getAdapter().getItem(position)).getInt("id");
							friendicaAbstraction.executeAjaxQuery("routes/setactive/"+routeId, new JsonFinishReaction<ArrayList<JSONObject>>() {
								@Override
								public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
									// TODO Auto-generated method stub

								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
	}
}