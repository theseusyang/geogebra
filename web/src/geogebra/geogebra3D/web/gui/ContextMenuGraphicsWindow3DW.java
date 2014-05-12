package geogebra.geogebra3D.web.gui;

import geogebra.geogebra3D.web.euclidian3D.EuclidianView3DW;
import geogebra.geogebra3D.web.gui.images.StyleBar3DResources;
import geogebra.web.gui.ContextMenuGraphicsWindowW;
import geogebra.web.gui.images.StyleBarResources;
import geogebra.web.gui.menubar.MainMenu;
import geogebra.web.javax.swing.GCheckBoxMenuItem;
import geogebra.web.main.AppW;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * @author mathieu
 *
 */
public class ContextMenuGraphicsWindow3DW extends ContextMenuGraphicsWindowW{

	/**
	 * constructor
	 * @param app application
	 * @param px mouse x
	 * @param py mouse y
	 */
	public ContextMenuGraphicsWindow3DW(final AppW app, double px, double py) {
	    super(app);
	    
	    this.px = px;
	    this.py = py;
	    

	    setTitle(app.getPlain("GraphicsView3D"));
	    
	    addAxesAndGridCheckBoxes();      


	    wrappedPopup.addSeparator();

	    MenuItem miStandardView = new MenuItem(app.getPlain("StandardView"), new Command() {

	    	public void execute() {
	    		((EuclidianView3DW) app.getEuclidianView3D()).setStandardView(true);
	    	}
	    });
	    miStandardView.addStyleName("mi_no_image");
	    wrappedPopup.addItem(miStandardView);


	    addMiProperties("GraphicsView3D");
    }
	
	

	@Override
	protected void addAxesAndGridCheckBoxes(){

        // checkboxes for axes and grid
		String htmlString = MainMenu.getMenuBarHtml(StyleBarResources.INSTANCE.axes().getSafeUri().asString(), app.getMenu("Axes"));
		GCheckBoxMenuItem cbShowAxes = new GCheckBoxMenuItem(htmlString, ((GuiManager3DW) app.getGuiManager()).getShowAxes3DAction());
		cbShowAxes.setSelected(((EuclidianView3DW) app.getEuclidianView3D()).axesAreAllVisible());
        getWrappedPopup().addItem(cbShowAxes);
        
        
		htmlString = MainMenu.getMenuBarHtml(StyleBarResources.INSTANCE.grid().getSafeUri().asString(), app.getMenu("Grid"));
		GCheckBoxMenuItem cbShowGrid = new GCheckBoxMenuItem(htmlString, ((GuiManager3DW) app.getGuiManager()).getShowGrid3DAction());
		cbShowGrid.setSelected(((EuclidianView3DW) app.getEuclidianView3D()).getxOyPlane().isGridVisible());
        getWrappedPopup().addItem(cbShowGrid);

		htmlString = MainMenu.getMenuBarHtml(StyleBar3DResources.INSTANCE.plane().getSafeUri().asString(), app.getMenu("Plane"));
		GCheckBoxMenuItem cbShowPlane = new GCheckBoxMenuItem(htmlString, ((GuiManager3DW) app.getGuiManager()).getShowPlane3DAction());
		cbShowPlane.setSelected(((EuclidianView3DW) app.getEuclidianView3D()).getxOyPlane().isPlateVisible());
        getWrappedPopup().addItem(cbShowPlane);
		
	}
	

}