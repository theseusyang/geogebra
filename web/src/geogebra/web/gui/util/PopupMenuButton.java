package geogebra.web.gui.util;

import geogebra.common.main.App;
import geogebra.html5.awt.GDimensionW;
import geogebra.html5.gui.util.Slider;
import geogebra.html5.main.AppW;
import geogebra.web.css.GuiResources;
import geogebra.web.euclidian.EuclidianStyleBarW;
import geogebra.web.gui.images.AppResourcesConverter;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PopupPanel;


public class PopupMenuButton extends MyCJButton implements ChangeHandler {
	
	protected AppW app;
	protected ImageOrText[] data;
	private ButtonPopupMenu myPopup;
	private PopupMenuHandler popupHandler;
	private Slider mySlider;
	private SelectionTable myTable;
	private boolean hasTable;
	/** flag to determine if the popup should persist after a mouse click */
	private boolean keepVisible = true;
	private boolean isIniting = true;
	private ImageOrText fixedIcon;
	private boolean isFixedIcon = false;
	private HandlerRegistration actionListener;
	
	/**
	 * @param app
	 *            {@link AppW}
	 * @param data
	 *            {@link ImageOrText}
	 * @param rows
	 *            {@code Integer}
	 * @param columns
	 *            {@code Integer}
	 * @param iconSize
	 *            {@link GDimensionW}
	 * @param mode
	 *            {@link SelectionTable}
	 */
	public PopupMenuButton(AppW app, ImageOrText[] data, Integer rows,
	        Integer columns, GDimensionW iconSize,
	        geogebra.common.gui.util.SelectionTable mode) {
		this(app, data, rows, columns, iconSize, mode, true, false);
	}
	
	/**
	 * @param app
	 *            {@link AppW}
	 * @param data
	 *            {@link ImageOrText}
	 * @param rows
	 *            {@code Integer}
	 * @param columns
	 *            {@code Integer}
	 * @param iconSize
	 *            {@link GDimensionW}
	 * @param mode
	 *            {@link SelectionTable}
	 * @param hasTable
	 *            {@code boolean}
	 * @param hasSlider
	 *            {@code boolean}
	 */
	public PopupMenuButton(AppW app, ImageOrText[] data, Integer rows,
	        Integer columns, GDimensionW iconSize,
	        geogebra.common.gui.util.SelectionTable mode,
	        final boolean hasTable, boolean hasSlider) {
		super();
		this.app = app;
		this.hasTable = hasTable;

		createPopup();

		// add a mouse listener to our button that triggers the popup
		addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if (!PopupMenuButton.this.isEnabled()) {
					return;
				}
				handleClick();
			}
		});

		if (hasTable) {
			createSelectionTable(data, rows, columns, iconSize, mode);
		}

		// create slider
		if (hasSlider) {
			getMySlider();
		}
		isIniting = false;
	}


	/**
	 * creates a new {@link ButtonPopupMenu}
	 */
	private void createPopup() {
		myPopup = new ButtonPopupMenu() {
			@Override
			public void setVisible(boolean visible) {
				super.setVisible(visible);

				// if another button is pressed only the visibility is changed,
				// by firing the event we can react as if it was closed
				CloseEvent.fire(this, this, false);
			}

			@Override
			public void hide() {
				super.hide();
				if (EuclidianStyleBarW.CURRENT_POP_UP.equals(this)) {
					EuclidianStyleBarW.CURRENT_POP_UP = null;
				}
			}
		};
		myPopup.setAutoHideEnabled(true);
    }

	/**
	 * handle click on {@link PopupMenuButton this button}
	 */
	void handleClick() {
		onClickAction();
		if (EuclidianStyleBarW.CURRENT_POP_UP != myPopup
		        || !app.wasPopupJustClosed()) {
			if (EuclidianStyleBarW.CURRENT_POP_UP != null) {
				EuclidianStyleBarW.CURRENT_POP_UP.hide();
			}
			EuclidianStyleBarW.CURRENT_POP_UP = myPopup;

			app.registerPopup(myPopup);
			myPopup.showRelativeTo(getWidget());
			myPopup.getFocusPanel().getElement().focus();
		} else {
			myPopup.setVisible(false);
			EuclidianStyleBarW.CURRENT_POP_UP = null;
		}
	}
	
	/**
	 * @param data
	 * @param rows
	 * @param columns
	 * @param iconSize
	 * @param mode
	 */
	private void createSelectionTable(ImageOrText[] data,
	        Integer rows, Integer columns, GDimensionW iconSize,
	        geogebra.common.gui.util.SelectionTable mode) {
		this.data = data;

		myTable = new SelectionTable(data, rows, columns, iconSize, mode);
		setSelectedIndex(0);

		myTable.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				handlePopupActionEvent();
			}
		});

		myPopup.getPanel().add(myTable);
    }

	/**
	 * @param popupMenuHandler
	 *            {@link PopupMenuHandler}
	 */
	public void addPopupHandler(PopupMenuHandler popupMenuHandler) {
		this.popupHandler = popupMenuHandler;
	}
	
	/**
	 * @return {@link PopupPanel}
	 */
	public PopupPanel getMyPopup() {
		return myPopup;
	}

	/**
	 * @return {@link SelectionTable}
	 */
	public SelectionTable getMyTable() {
		return myTable;
	}

	/**
	 * called by click on button
	 */
	protected void onClickAction() {
		// called by click on button
		// overridden in (e.g.) EuclidianStayleBar3DW
	}
	
	/**
	 * Pass a popup action event up to the button invoker. If the first button
	 * click triggered our popup (the click was in the triangle region), then we
	 * must pass action events from the popup to the invoker
	 */
	public void handlePopupActionEvent(){
		if (popupHandler != null) {
			popupHandler.fireActionPerformed(this);
		} else {
			App.debug("PopupMenubutton has null popupHandler");
		}
		
		updateGUI();
		if(!keepVisible) {
			myPopup.hide();
		}
	}
	
	private void updateGUI(){
		if(isIniting) return;

		if(hasTable){
			setIcon(getButtonIcon());
		}
	}
	
	// ==============================================
	// Icon Handling
	// ==============================================

	/**
	 * @return {@link ImageOrText}
	 */
	public ImageOrText getButtonIcon() {
		return data[getSelectedIndex()];
	}
		
	/**
	 * Append a downward triangle image to the right hand side of an input icon.
	 */
	@Override
	public void setIcon(ImageOrText icon) {

		if (isFixedIcon) {			
			super.setIcon(fixedIcon);
			return;
		}

		// add a down_triangle image to the left of the icon
		if (icon != null) {
			super.setIcon(icon);
		} else {
			AppResourcesConverter.setIcon(GuiResources.INSTANCE.toolbar_further_tools(), this);
		}
	}


	/**
	 * @param icon
	 *            {@link ImageOrText}
	 */
	public void setFixedIcon(ImageOrText icon){
		isFixedIcon = true;
		this.fixedIcon = icon;
		setIcon(icon);
	}

	/**
	 * @param selectedIndex
	 *            {@code Integer}
	 */
	public void setSelectedIndex(Integer selectedIndex) {
		if (selectedIndex == null) {
			selectedIndex = -1;
		}
		myTable.setSelectedIndex(selectedIndex.intValue());
		updateGUI();
	}
	
	@Override
	public void onChange(ChangeEvent event) {
		if(mySlider != null) {
			   setSliderValue(mySlider.getValue());
		}
		((EuclidianStyleBarW)app.getActiveEuclidianView().getStyleBar()).fireActionPerformed(this);
		fireActionPerformed();
		updateGUI();
	}
	
	protected void fireActionPerformed() {
	    //implemented in subclass
    }

	/**
	 * @return {@link Slider}
	 */
	public Slider getMySlider() {
		if (mySlider == null) {
			initSlider();
		}
		return mySlider;
	}
	
	private void initSlider() {
		mySlider = new Slider(0,100);
		mySlider.setMajorTickSpacing(25);
		mySlider.setMinorTickSpacing(5);
		mySlider.setPaintTicks(false);
		mySlider.setPaintLabels(false);
		mySlider.addChangeHandler(this);

		myPopup.getPanel().add(mySlider);
	}
	
	/**
	 * @param value
	 *            {@code int}
	 */
	public void setSliderValue(int value) {
		if (mySlider == null) {
			return;
		}
		mySlider.setValue(value);
		updateGUI();
	}

	/**
	 * @return selected index of {@link #myTable}
	 */
	public int getSelectedIndex() {
		return myTable.getSelectedIndex();
	}

	public void update(Object[] array) {
	    // will be overwritten from instances
    }
	
	/**
	 * @return selected Object of the {@link SelectionTable table}
	 */
	public Object getSelectedValue() {
		return myTable.getSelectedValue();
	}
	
	/**
	 * @return {@code int} or {@code -1} if the {@link Slider slider} is null
	 */
	public int getSliderValue() {
		return mySlider == null ? -1 : mySlider.getValue();
	}

	/**
	 * @param euclidianStyleBar
	 *            {@link EuclidianStyleBarW}
	 */
	public void removeActionListener(EuclidianStyleBarW euclidianStyleBar) {
		if (actionListener != null) {
			actionListener.removeHandler();
		}
	}

	/**
	 * @param keepVisible
	 *            {@code boolean}
	 */
	public void setKeepVisible(boolean keepVisible) {
		this.keepVisible = keepVisible;
	}
	
	/**
	 * @return {@code true} if it has a slider, {@code false} otherwise
	 */
	protected boolean hasSlider() {
		return mySlider != null;
	}
}
