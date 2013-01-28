package de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.TwAjax;

public class Friendica {
	final static String API_PATH = "/api/";
	final static String API_TYPE_JSON = ".json";
	final Integer ITEMS_PER_PAGE = 20;
	Integer curLoadPage = 1;
	private Context context = null;

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void testExecuteMethod() {
		String userId = new String();
		HashMap<String, String> queryElements = new HashMap<String, String>();
		queryElements.put("count", String.valueOf(ITEMS_PER_PAGE));
		queryElements.put("page", String.valueOf(curLoadPage));
		if (userId != null && !userId.equals("")) {
			queryElements.put("userId", userId);
		}

		JsonProcessor jsonProcessor = executeAjaxQuery("statuses/user_timeline", queryElements, new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				ArrayList<JSONObject> jAr = result.getResult();
				for (JSONObject jsonObject : jAr) {
					System.out.println(jsonObject);
				}
			}
		});
		if (jsonProcessor.getFinished()) {
			System.out.println("Already finished");
		}
	}

	public JsonProcessor executeAjaxQuery(String command, JsonFinishReaction<ArrayList<JSONObject>> finishReaction) {
		return executeAjaxQuery(command, new HashMap<String, String>(), finishReaction);
	}

	public JsonProcessor executeAjaxQuery(String command, HashMap<String, String> queryElements, JsonFinishReaction<ArrayList<JSONObject>> finishReaction) {
		ResultObject<ArrayList<JSONObject>> result = new ResultObject<ArrayList<JSONObject>>();
		Uri uri = Uri.parse(Max.getServer(getContext()) + API_PATH + command + API_TYPE_JSON);
		Builder uriBuilder = uri.buildUpon();
		for (Entry<String, String> queryElement : queryElements.entrySet()) {
			uriBuilder.appendQueryParameter(queryElement.getKey(), queryElement.getValue());
		}
		final TwAjax t = new TwAjax(getContext(), true, true);
		JsonProcessor jsonProcessor = new JsonProcessor(t, finishReaction, result);
		t.getUrlContent(uriBuilder.build().toString(), jsonProcessor);
		return jsonProcessor;
	}

	public class ResultObject<TargetResultClass extends Object> {
		private TargetResultClass result = null;
		public TargetResultClass getResult() {
			return result;
		}
		public void setResult(TargetResultClass result) {
			this.result = result;
		}
	}

	public interface JsonFinishReaction<TargetResultClass> {
		public void onFinished(ResultObject<TargetResultClass> result);
	}

	public class JsonProcessor implements Runnable {
		private TwAjax t;
		private JsonFinishReaction<ArrayList<JSONObject>> jsonFinishReaction;
		private ResultObject<ArrayList<JSONObject>> result;
		private ArrayList<JSONObject> resultArray = new ArrayList<JSONObject>();
		private Boolean finished = false;

		public JsonProcessor(TwAjax t, JsonFinishReaction<ArrayList<JSONObject>> finishReaction, ResultObject<ArrayList<JSONObject>> result) {
			this.t = t;
			this.jsonFinishReaction = finishReaction;
			this.result = result;
		}

		public Boolean getFinished() {
			return finished;
		}

		@Override
		public void run() {
			JSONArray jAr = (JSONArray) t.getJsonResult();
			for (int i = 0; i < jAr.length(); i++) {
				JSONObject jObj;
				try {
					jObj = jAr.getJSONObject(i);
					resultArray.add(jObj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			result.setResult(resultArray);
			jsonFinishReaction.onFinished(result);
			finished = true;
		}
	}
}