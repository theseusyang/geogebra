package geogebra.web.move.ggtapi.models;

import geogebra.common.move.ggtapi.models.Request;
import geogebra.common.move.ggtapi.models.json.JSONObject;
import geogebra.common.move.ggtapi.models.json.JSONString;
import geogebra.html5.main.AppW;

/**
 * Upload request for GeoGebraTube
 *
 */
public class SyncRequest implements Request {

	private final String API = "1.0.0";
	private final String GGB = "geogebra";
	private final String TASK = "sync";
	private AppW app;
	private long timestamp;

	/**
	 * Used to upload the actual opened application to GeoGebraTube
	 * 
	 * @param app
	 *            AppW
	 * @param timestamp
	 *            since when we want to see the events
	 */
	SyncRequest(AppW app, long timestamp) {
		this.app = app;
		this.timestamp = timestamp;
	}

	@Override
	public String toJSONString() {
		// TODO for save we only need title
		// request
		JSONObject request = new JSONObject();

		JSONObject api = new JSONObject();
		api.put("-api", new JSONString(this.API));

		// login
		JSONObject login = new JSONObject();
		login.put("-type", new JSONString(this.GGB));
		login.put("-token", new JSONString(app.getLoginOperation().getModel()
		        .getLoggedInUser().getLoginToken()));
		api.put("login", login);

		// task
		JSONObject task = new JSONObject();
		task.put("-type", new JSONString(this.TASK));

		// type
		task.put("timestamp", new JSONString(this.timestamp + ""));



		api.put("task", task);
		request.put("request", api);

		return request.toString();
	}
}
