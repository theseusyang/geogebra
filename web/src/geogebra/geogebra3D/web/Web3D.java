package geogebra.geogebra3D.web;


import geogebra.common.GeoGebraConstants;
import geogebra.common.kernel.commands.AlgebraProcessor;
import geogebra.common.util.debug.GeoGebraProfiler;
import geogebra.common.util.debug.SilentProfiler;
import geogebra.html5.Browser;
import geogebra.html5.cas.giac.PNaCl;
import geogebra.html5.js.ResourcesInjector;
import geogebra.html5.util.ArticleElement;
import geogebra.html5.util.CustomElements;
import geogebra.html5.util.Dom;
import geogebra.web.Web;
import geogebra.web.gui.app.GeoGebraAppFrame;
import geogebra.web.gui.applet.AppletFactory;
import geogebra.web.gui.applet.GeoGebraFrameBoth;
import geogebra.web.main.BrowserDevice;
import geogebra.web.main.GDevice;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.RootPanel;



/**
 * @author apa
 *
 */
/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Web3D extends Web {

	public void t(String s,AlgebraProcessor ap) throws Exception{
		ap.processAlgebraCommandNoExceptionHandling(s, false, false, true, false);
	}
	
	private static ArrayList<ArticleElement> getGeoGebraMobileTags() {
		NodeList<Element> nodes = Dom.getElementsByClassName(GeoGebraConstants.GGM_CLASS_NAME);
		ArrayList<ArticleElement> articleNodes = new ArrayList<ArticleElement>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Date creationDate = new Date();
			nodes.getItem(i).setId(GeoGebraConstants.GGM_CLASS_NAME+i+creationDate.getTime());
			articleNodes.add(ArticleElement.as(nodes.getItem(i)));
		}
		return articleNodes;
	}
	
	private static boolean checkAppNeeded() {
		NodeList<Element> nodes = Dom.getElementsByClassName(GeoGebraConstants.GGM_CLASS_NAME);
		for (int i = 0; i < nodes.getLength(); i++) {
			if("true".equals(nodes.getItem(i).getAttribute("data-param-app"))){
				return true;
			}
		}
		return false;
	}

	/**
	 * set true if Google Api Js loaded
	 */

	public void onModuleLoad() {
		if(RootPanel.getBodyElement().getAttribute("data-param-laf")!=null
				&& !"".equals(RootPanel.getBodyElement().getAttribute("data-param-laf"))){
			//loading touch, ignore.
			return;			
		}
		Browser.checkFloat64();
		//use GeoGebraProfilerW if you want to profile, SilentProfiler  for production
		//GeoGebraProfiler.init(new GeoGebraProfilerW());
		GeoGebraProfiler.init(new SilentProfiler());
		
		GeoGebraProfiler.getInstance().profile();

		CustomElements.registerGeoGebraWebElement();
		exportGGBElementRenderer();
		
		
		
//		setLocaleToQueryParam();
				
		if (!Web3D.checkAppNeeded()) {
			//we dont want to parse out of the box sometimes...
			if (!calledFromExtension()) {
				loadAppletAsync();
			} else {
				loadExtensionAsync();
			}
		} else {
			loadAppAsync();
		}
		
		//just debug for now
		PNaCl.exportPNaCltoConsole();
	}

	private void loadExtensionAsync() {
		//GWT.runAsync(new RunAsyncCallback() {
			
		//	public void onSuccess() {
				ResourcesInjector.injectResources();
				 exportArticleTagRenderer();
				    //export other methods if needed
				    //call the registered methods if any
				    GGW_ext_webReady();
		//	}
			
		//	public void onFailure(Throwable reason) {
				// TODO Auto-generated method stub
				
		//	}
		//});
	   
    }

	public static void loadAppletAsync() {
	    //GWT.runAsync(new RunAsyncCallback() {
			
			//public void onSuccess() {
				startGeoGebra(getGeoGebraMobileTags());
			//}
			
			//ublic void onFailure(Throwable reason) {
				// TODO Auto-generated method stub
				
			//}
		//});
    }

	private void loadAppAsync() {
	    //GWT.runAsync(new RunAsyncCallback() {
			
		//	public void onSuccess() {
				ResourcesInjector.injectResources();
				createGeoGebraAppFrame(new BrowserDevice());
		//	}

		//	public void onFailure(Throwable reason) {
		//		Log.debug(reason);
		//	}
		//});
	    
    }
	
	
	/**
	 * create app frame
	 */
	@Override
	protected void createGeoGebraAppFrame(GDevice device){
		new GeoGebraAppFrame(Web.getLAF(getGeoGebraMobileTags()), device, (AppletFactory) GWT.create(AppletFactory.class) );
	}
	

	
	native void exportArticleTagRenderer() /*-{
	    $wnd.GGW_ext.render = $entry(@geogebra.geogebra3D.web.Web3D::renderArticleElement(Lcom/google/gwt/dom/client/Element;Lcom/google/gwt/core/client/JavaScriptObject;));
    }-*/;
	
	private native void exportGGBElementRenderer() /*-{
	 	$wnd.renderGGBElement = $entry(@geogebra.geogebra3D.web.Web3D::renderArticleElement(Lcom/google/gwt/dom/client/Element;Lcom/google/gwt/core/client/JavaScriptObject;));
		@geogebra.html5.gui.GeoGebraFrame::renderGGBElementReady()();
		window.addEventListener("message",function(event){$wnd.postMessage(event.data,"*");});
	}-*/;
    
	private native boolean calledFromExtension() /*-{
	    return (typeof $wnd.GGW_ext !== "undefined");
    }-*/;
	
	public static void renderArticleElement(Element el, JavaScriptObject clb){
		GeoGebraFrameBoth.renderArticleElement(el, (AppletFactory) GWT.create(AppletFactory.class) 
		, Web.getLAF(getGeoGebraMobileTags()), clb);
	}
	/*
	 * This method should never be called. Only copyed to external javascript files,
	 * if we like to use GeoGebraWeb as an library, and call its methods depending on
	 * it is loaded or not.
	 */
	private native void copyThisJsIfYouLikeToUseGeoGebraWebAsExtension() /*-{
		//GGW_ext namespace must be a property of the global scope
		window.GGW_ext = {
			startupFunctions : []
		};
		
		//register methods that will be called if web is loaded,
		//or if it is loaded, will be called immediately
		//GGW_ext.webReady("render",articleelement);
		GGW_ext.webReady = function(functionName, args) {
			if (typeof GGW_ext[functionName] === "function") {
				//web loaded
				this[functionName].apply(args);
			} else {
				this.startupFunctions.push([functionName,args]);
			}	
		}
	}-*/;
	
	private native void GGW_ext_webReady() /*-{
		var functions = null,
			i,l;
		if (typeof $wnd.GGW_ext === "object") {
			if ($wnd.GGW_ext.startupFunctions && $wnd.GGW_ext.startupFunctions.length) {
				functions = $wnd.GGW_ext.startupFunctions;
				for (i = 0, l = functions.length; i < l; i++) {
					if (typeof $wnd.GGW_ext[functions[i][0]] === "function") {
						$wnd.GGW_ext[functions[i][0]](functions[i][1]);
					}
				}
			} 
		}
	}-*/;
	
	
	static void startGeoGebra(ArrayList<ArticleElement> geoGebraMobileTags) {
	 	
		geogebra.web.gui.applet.GeoGebraFrameBoth.main(geoGebraMobileTags, (AppletFactory) GWT.create(AppletFactory.class), Web.getLAF(geoGebraMobileTags));
	   
    }

}
