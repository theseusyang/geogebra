package geogebra.web.gui.menubar;

import geogebra.common.javax.swing.GOptionPane;
import geogebra.common.move.views.BooleanRenderable;
import geogebra.common.util.AsyncOperation;
import geogebra.html5.javax.swing.GOptionPaneW;
import geogebra.html5.main.AppW;
import geogebra.html5.main.StringHandler;
import geogebra.web.css.GuiResources;
import geogebra.web.gui.GuiManagerW;
import geogebra.web.gui.dialog.DialogManagerW;

import com.google.gwt.user.client.ui.MenuItem;

/**
 * Web implementation of FileMenu
 */
public class FileMenuW extends GMenuBar implements BooleanRenderable {
	
	/** Application */
	AppW app;
	private MenuItem uploadToGGT;
	Runnable onFileOpen;
	Runnable newConstruction;
	
	/**
	 * @param app application
	 * @param onFileOpen Runnable
	 */
	public FileMenuW(final AppW app, Runnable onFileOpen) {
	    super(true);
	    this.app = app;
	    this.onFileOpen = onFileOpen;
	    this.newConstruction = new Runnable() {
			
			@Override
			public void run() {
				app.setWaitCursor();
				app.fileNew();
				app.setDefaultCursor();
				app.showStartScreen();
			}
		};
	    addStyleName("GeoGebraMenuBar");
	    initActions();
		update();
	}

	private void update() { 
	    // TODO Auto-generated method stub
	    
    }
	
	native void nativeShare(String base64, String title)/*-{
		if ($wnd.android) {
			$wnd.android.share(base64, title, 'ggb');

		}
	}-*/;
	
	native boolean nativeShareSupported()/*-{
		if ($wnd.android && $wnd.android.share) {
			return true;
		}
		return false;
	}-*/;

	native void exitExam() /*-{
		$wnd.close();
	}-*/;
	
	private void initActions() {

		final boolean exam = app.getLAF().isExam();
		if (exam) {
			addItem(MainMenu.getMenuBarHtml(GuiResources.INSTANCE.menu_icon_sign_out().getSafeUri().asString(),app.getMenu("Close"), true),true,new MenuCommand(app) {

				@Override
				public void doExecute() {
					// set Firefox dom.allow_scripts_to_close_windows in about:config to true to make this work
					String[] optionNames = { app.getMenu("Cancel"), app.getMenu("ExitExamShutDown") };

					GOptionPaneW.INSTANCE.showOptionDialog(app, app.getMenu("ExitExamConfirm"), app.getMenu("ExitExamConfirmTitle"),
					        GOptionPane.CUSTOM_OPTION, GOptionPane.WARNING_MESSAGE, null,
					        optionNames, new AsyncOperation() {
						        @Override
						        public void callback(Object obj) {
						        	String[] dialogResult = (String[])obj;
							        if ("1".equals(dialogResult[0])) {
							        	exitExam();
							        }
						        }
					        });
				}
			});
			return;
		}
		
		
		// this is enabled always
		addItem(MainMenu.getMenuBarHtml(GuiResources.INSTANCE.menu_icon_file_new().getSafeUri().asString(),app.getMenu("New"), true),true,new MenuCommand(app) {

			@Override
			public void doExecute() {
				((DialogManagerW) app.getDialogManager()).getSaveDialog().showIfNeeded(newConstruction);
			}
		});

		// open menu is always visible in menu
		
			addItem(MainMenu.getMenuBarHtml(GuiResources.INSTANCE.menu_icon_file_open().getSafeUri().asString(), app.getPlain("Open"), true),true,new MenuCommand(app) {
    		
				@Override
				public void doExecute() {
			        app.openSearch(null);
					if(FileMenuW.this.onFileOpen!=null){
						FileMenuW.this.onFileOpen.run();
					}
				}
			});	
		
		
		if(app.getLAF().undoRedoSupported()) {
			addItem(MainMenu.getMenuBarHtml(GuiResources.INSTANCE.menu_icon_file_save().getSafeUri().asString(), app.getMenu("Save"), true),true,new MenuCommand(app) {
		
				@Override
				public void doExecute() {
			        app.getGuiManager().save();
				}
			});			
		}

		// this is enabled always
	    uploadToGGT = addItem(MainMenu.getMenuBarHtml(GuiResources.INSTANCE.menu_icon_file_share().getSafeUri().asString(),app.getMenu("Share"), true),true,new MenuCommand(app) {
	    	
	    	
	    	@Override
	    	public void doExecute() {
	    		if(!nativeShareSupported()){
	    			app.uploadToGeoGebraTube();
	    		} else {
	    			app.getGgbApi().getBase64(true, new StringHandler(){

	    				@Override
	    				public void handle(String s) {
	    					String title = app.getKernel().getConstruction().getTitle();
	    					nativeShare(s, "".equals(title) ? "construction" : title);
	    				}}); 
	    		}
	    	}
	    });
	    
		if (app.getLAF().exportSupported()) {

			addItem(MainMenu.getMenuBarHtml(GuiResources.INSTANCE
			        .menu_icons_file_export().getSafeUri().asString(),
			        app.getMenu("Export"), true), true, new MenuCommand(app) {

				@Override
				public void doExecute() {
					((GuiManagerW) app.getGuiManager()).openFilePicker();
				}
			});
		}
	    /*addItem(MainMenu.getMenuBarHtml(AppResources.INSTANCE.empty().getSafeUri().asString(), app.getMenu("Export"), true),
		        true, new ExportMenuW(app));*/
	    
	    app.getNetworkOperation().getView().add(this);
	    
	    if (!app.getNetworkOperation().isOnline()) {
	    	render(false);    	
	    }
	}

	/**
	 * @param online wether the application is online
	 * renders a the online - offline state of the FileMenu
	 */
	public void render(boolean online) {
	    uploadToGGT.setEnabled(online);
	    if (!online) {
	    	uploadToGGT.setTitle(app.getMenu("YouAreOffline"));
		} else {
			uploadToGGT.setTitle("");
		}
    }
}
