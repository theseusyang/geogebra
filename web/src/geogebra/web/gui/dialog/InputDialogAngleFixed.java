package geogebra.web.gui.dialog;

import geogebra.common.euclidian.EuclidianController;
import geogebra.common.gui.InputHandler;
import geogebra.common.gui.dialog.handler.NumberInputHandler;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.kernel.kernelND.GeoSegmentND;
import geogebra.common.main.DialogManager;
import geogebra.common.util.StringUtil;
import geogebra.common.util.Unicode;
import geogebra.html5.gui.inputfield.AutoCompleteTextFieldW;
import geogebra.html5.main.AppW;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;

public class InputDialogAngleFixed extends AngleInputDialog{
	private static final long serialVersionUID = 1L;
	private static String defaultRotateAngle = "45\u00b0"; // 45 degrees

	private GeoPointND geoPoint1;
	GeoSegmentND[] segments;
	GeoPointND[] points;
	GeoElement[] selGeos;

	private Kernel kernel;
	
	private EuclidianController ec;
		
	public InputDialogAngleFixed(AppW app, String title, InputHandler handler, GeoSegmentND[] segments, GeoPointND[] points, GeoElement[] selGeos, Kernel kernel, EuclidianController ec) {
		super(app, app.getPlain("Angle"), title, defaultRotateAngle, false, handler, false);
		
		geoPoint1 = points[0];
		this.segments = segments;
		this.points = points;
		this.selGeos = selGeos;
		this.kernel = kernel;
		
		this.ec = ec;
		
	}

	/**
	 * Handles button clicks for dialog.
	 */
	@Override
	public void onClick(ClickEvent e) {
		actionPerformed(e);
	}
	
	@Override
	protected void actionPerformed(DomEvent e){
		Object source = e.getSource();
		try {
			if (source == btOK || sourceShouldHandleOK(source)) {
				if (!processInput()) {
					//wrappedPopup.show();
					inputPanel.getTextComponent().hideTablePopup();
				} else {
					wrappedPopup.hide();
					inputPanel.getTextComponent().hideTablePopup();
					app.getActiveEuclidianView().requestFocusInWindow();
				}
				//setVisibleForTools(!processInput());
			//} else if (source == btApply) {
			//	processInput();
			} else if (source == btCancel) {
				//setVisibleForTools(false);
				wrappedPopup.hide();
				inputPanel.getTextComponent().hideTablePopup();
				app.getActiveEuclidianView().requestFocusInWindow();
			} 
		} catch (Exception ex) {
			// do nothing on uninitializedValue		
			//setVisibleForTools(false);
			wrappedPopup.hide();
			inputPanel.getTextComponent().hideTablePopup();
			app.getActiveEuclidianView().requestFocusInWindow();
		}
	}
	
	private boolean processInput() {
		
		// avoid labeling of num
		Construction cons = kernel.getConstruction();
		boolean oldVal = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);
		
		inputText = inputPanel.getText();
		
		// negative orientation ?
		if (rbClockWise.getValue()) {
			inputText = "-(" + inputText + ")";
		}
		
		boolean success1 = inputHandler.processInput(inputText);

		cons.setSuppressLabelCreation(oldVal);
		
		
		
		if (success1) {
			String angleText = inputPanel.getText();
			// keep angle entered if it ends with 'degrees'
			if (angleText.endsWith("\u00b0") ) defaultRotateAngle = angleText;
			else defaultRotateAngle = "45"+"\u00b0";


			DialogManager.doAngleFixed(kernel, segments, points, selGeos, ((NumberInputHandler)inputHandler).getNum(), rbClockWise.getValue(), ec);

			return true;
		}

		
		return false;
		
	}
/*
	@Override
	public void windowGainedFocus(WindowEvent arg0) {
		if (!isModal()) {
			app.setCurrentSelectionListener(null);
		}
		app.getGuiManager().setCurrentTextfield(this, true);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}*/

	/*
	 * auto-insert degree symbol when appropriate
	 */
	public void onKeyUp(KeyUpEvent e) {
		super.onKeyUp(e);
		
		// return unless digit typed (instead of !Character.isDigit)
		if (e.getNativeKeyCode() < 48 ||
			(e.getNativeKeyCode() >  57 && e.getNativeKeyCode() < 96) ||
			e.getNativeKeyCode() > 105)
			return;
		
		AutoCompleteTextFieldW tc = inputPanel.getTextComponent();
		String text = tc.getText();
		
		// if text already contains degree symbol or variable
		for (int i = 0 ; i < text.length() ; i++) {
			if (!StringUtil.isDigit(text.charAt(i))) return;
		}
		
		int caretPos = tc.getCaretPosition();
		
		tc.setText(tc.getText()+Unicode.degree);
		
		tc.setCaretPosition(caretPos);
		
	}
}
