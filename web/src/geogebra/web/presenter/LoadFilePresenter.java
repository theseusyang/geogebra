package geogebra.web.presenter;

import geogebra.web.helper.FileLoadCallback;
import geogebra.web.helper.UrlFetcher;
import geogebra.web.html5.View;
import geogebra.web.jso.JsUint8Array;
import geogebra.web.main.Application;
import geogebra.web.util.DataUtil;

import com.google.gwt.core.client.JsArrayInteger;

public class LoadFilePresenter extends BasePresenter {
	
	private final UrlFetcher urlFetcher;
	

	public LoadFilePresenter(UrlFetcher urlFetcher) {
		this.urlFetcher = urlFetcher;
	}
	
	public void onPageLoad() {
		
		View view = getView();
		String filename = view.getDataParamFileName();
		String base64String;
		
		if (!"".equals(filename)) {
			fetch(filename);
		} else if (!"".equals((base64String = view.getDataParamBase64String()))) {
			process(base64String);
		} else if (urlFetcher.isGgbFileParameterSpecified()) {
			fetch(urlFetcher.getAbsoluteGgbFileUrlFromParameter());
		} else {
			view.promptUserForGgbFile();
		}
		
		Application app = view.getApplication();

		//app.setUndoActive(undoActive);			
		//app.setUseBrowserForJavaScript(useBrowserForJavaScript);
		//app.setRightClickEnabled(enableRightClick);
		//app.setChooserPopupsEnabled(enableChooserPopups);
		//app.setErrorDialogsActive(errorDialogsActive);
		//if (customToolBar != null && customToolBar.length() > 0 && showToolBar)
		//	app.getGuiManager().setToolBarDefinition(customToolBar);
		//app.setMaxIconSize(maxIconSize);

		//app.setShowMenuBar(view.getDataParamShowMenuBar());
		//app.setShowAlgebraInput(view.getDataParamShowAlgebraInput(), true);
		//app.setShowToolBar(view.getDataParamShowToolBar(), view.getDataParamShowToolBarHelp());	
		
		app.setLabelDragsEnabled(view.getDataParamEnableLabelDrags());
		app.setShiftDragZoomEnabled(view.getDataParamShiftDragZoomEnabled());
		app.setShowResetIcon(view.getDataParamShowResetIcon());
		
	}

	public boolean isGgbFileParameterSpecified() {
		return urlFetcher.isGgbFileParameterSpecified();
	}

	private void process(String dataParamBase64String) {
		getView().showLoadAnimation();
		byte[] bytes = DataUtil.decode(dataParamBase64String);
		JsArrayInteger jsBytes = JsArrayInteger.createArray().cast();
		jsBytes.setLength(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			int x = bytes[i];
			if (x < 0) x += 256;
			
			jsBytes.set(i, x);
		}
	   getView().fileContentLoaded(jsBytes);
	}

	public void onWorksheetConstructionFailed(String errorMessage) {
		getView().showError(errorMessage);
	}
	
	public void onWorksheetReady() {
		getView().hide();
	}
	
	//Reverse MVP	
	public void fetchGgbFileFromUserInput(String userUrl) {
		fetch(urlFetcher.getAbsoluteGgbFileUrl(userUrl));
	}
	
	public FileLoadCallback getFileLoadCallback() {
		return fileLoadCallback;
	}
		
	// Private Methods
	private void fetch(String absoluteUrl) {
		getView().showLoadAnimation();
		urlFetcher.fetchGgbFileFrom(absoluteUrl, fileLoadCallback);
	}
	
	private final FileLoadCallback fileLoadCallback = new FileLoadCallback() {
		public void onSuccess(JsUint8Array zippedContent) {
			JsArrayInteger jsBytes = JsArrayInteger.createArray().cast();
			jsBytes.setLength(zippedContent.getLength());
			for (int i = 0; i < zippedContent.getLength(); i++) {
				int x = zippedContent.get(i);
				if (x < 0) x += 256;
				
				jsBytes.set(i, x);
			}
		   getView().fileContentLoaded(jsBytes);
		}
		
		public void onError(String errorMessage) {
			getView().showError(errorMessage);
		}
	};
	
}
