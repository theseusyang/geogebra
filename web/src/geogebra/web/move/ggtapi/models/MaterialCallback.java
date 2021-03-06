package geogebra.web.move.ggtapi.models;

import geogebra.common.move.ggtapi.models.Material;
import geogebra.common.util.debug.Log;
import geogebra.html5.gui.tooltip.ToolTipManagerW;

import java.util.List;

public abstract class MaterialCallback {

	public abstract void onLoaded(List<Material> parseResponse);

	public void onError(Throwable exception) {
		Log.error(exception.getMessage());
		// TODO
		ToolTipManagerW.sharedInstance().showBottomMessage(
		        exception.getMessage(), true);
	}

}
