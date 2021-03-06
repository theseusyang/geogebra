package geogebra.web.gui;

import geogebra.common.main.Localization;
import geogebra.html5.gui.FastClickHandler;
import geogebra.web.css.GuiResources;
import geogebra.web.gui.browser.BrowseResources;
import geogebra.web.gui.util.StandardButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Common superclass for worksheet and browse GUIs
 * 
 * @author Zbynek
 * 
 */
public class AuxiliaryHeaderPanel extends FlowPanel {

	protected StandardButton backButton;
	private FlowPanel backPanel;
	protected FlowPanel rightPanel;
	private final Label headerText;
	private final MyHeaderPanel gui;
	protected final Localization loc;

	protected AuxiliaryHeaderPanel(final Localization loc,
	        final MyHeaderPanel gui) {
		this.setStyleName("headerbar");
		this.loc = loc;
		this.gui = gui;

		addBackPanel();

		this.rightPanel = new FlowPanel();
		this.rightPanel.setStyleName("headerSecond");

		this.headerText = new Label("");
		this.headerText.addStyleName("locationTitle");
		this.add(this.headerText);

	}

	private void addBackPanel() {
		this.backPanel = new FlowPanel();
		this.backPanel.setStyleName("headerFirst");
		this.backPanel.addDomHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				gui.close();
			}
		}, ClickEvent.getType());

		// TODO - use new icon; this is just a placeholder
		this.backButton = new StandardButton(BrowseResources.INSTANCE.back());
		this.backButton.addFastClickHandler(new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				gui.close();
			}
		});
		this.backButton.addStyleName("backButton");
		this.backPanel.add(this.backButton);

		SimplePanel ggbLogoPanel = new SimplePanel();
		ggbLogoPanel.setStyleName("ggbLogoPanel");
		// TODO - use new icon; this is just a placeholder
		ImageResource image = GuiResources.INSTANCE.header_back();
		ggbLogoPanel.getElement().setInnerHTML(
		        "<div class=\"image\"> <img src=\""
		                + image.getSafeUri().asString() + "\" /></div>");
		this.backPanel.add(ggbLogoPanel);
		this.add(this.backPanel);
	}

	public void setLabels() {
		// this.backButton.setLabel(this.loc.getMenu("Back"));
	}

	public void setText(final String title) {
		this.headerText.setText(title);
	}
}
