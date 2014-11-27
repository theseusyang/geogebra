///* 
//GeoGebra - Dynamic Mathematics for Everyone
//http://www.geogebra.org
//
//This file is part of GeoGebra.
//
//This program is free software; you can redistribute it and/or modify it 
//under the terms of the GNU General Public License as published by 
//the Free Software Foundation.
//
// */

package geogebra.web.gui.dialog;

import geogebra.common.gui.dialog.ToolManagerDialogModel;
import geogebra.common.gui.dialog.ToolManagerDialogModel.ToolManagerDialogListener;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.Macro;
import geogebra.html5.main.AppW;
import geogebra.html5.main.LocalizationW;
import geogebra.web.gui.ToolNameIconPanel;

import java.util.ArrayList;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;


public class ToolManagerDialogW extends DialogBoxW implements
		ClickHandler, ToolManagerDialogListener {

	private static final long serialVersionUID = 1L;

	AppW app;
	final LocalizationW loc;
	private ToolManagerDialogModel model;

	private Button btUp;

	private Button btDown;

	private Button btRemove;

	public ToolManagerDialogW(AppW app) {
		setModal(true);

		model = new ToolManagerDialogModel(app, this);

		this.app = app;
		this.loc = (LocalizationW) app.getLocalization();
		initGUI();
		center();
	}

	@Override
	public void setVisible(boolean flag) {
		if (flag) {
			app.setMoveMode();
		} else {
			// recreate tool bar of application window
			updateToolBar();
		}

		super.setVisible(flag);
	}

	/**
	 * Updates the order of macros using the listModel.
	 */
	private void updateToolBar() {
		//model.addMacros(listModel.toArray());
		app.updateToolBar();
	}

	/**
	 * Deletes all selected tools that are not used in the construction.
	 */
	private void deleteTools(ListBox toolList) {
//		Object[] sel = toolList.getSelectedValues();
//		if (sel == null || sel.length == 0)
//			return;
//
//		// ARE YOU SURE ?
//		// Michael Borcherds 2008-05-04
//		Object[] options = { loc.getMenu("DeleteTool"),
//				loc.getMenu("DontDeleteTool") };
//		int returnVal = JOptionPane.showOptionDialog(this,
//				loc.getMenu("Tool.DeleteQuestion"), loc.getPlain("Question"),
//				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
//				options, options[1]);
//		if (returnVal == 1)
//			return;
//
//		for (Macro macro : model.getDeletedMacros()) {
//			listModel.removeElement(macro);
//		}
//
//		if (model.deleteTools(sel)) {
//			updateToolBar(listModel);
//		}
//
	}

	private FlowPanel createListUpDownRemovePanel() {
		btUp = new Button("\u25b2");
		btUp.setTitle(app.getPlain("Up"));
		btUp.addClickHandler(this);
		btUp.getElement().getStyle().setMargin(3, Style.Unit.PX);

		btDown = new Button("\u25bc");
		btDown.setTitle(app.getPlain("Down"));
		btDown.addClickHandler(this);
		btDown.getElement().getStyle().setMargin(3, Style.Unit.PX);

		btRemove = new Button("\u2718");
		btRemove.setTitle(app.getPlain("Remove"));
		btRemove.addClickHandler(this);
		btRemove.getElement().getStyle().setMargin(3, Style.Unit.PX);

		FlowPanel panel = new FlowPanel();
		panel.add(btUp);
		panel.add(btDown);
		panel.add(btRemove);

		return panel;
	}

	private void initGUI() {
		addStyleName("GeoGebraPopup");
		getCaption().setText(loc.getMenu("Tool.Manage"));


		FlowPanel panel = new FlowPanel();

		FlowPanel toolListPanel = new FlowPanel();
		Label lblTitle = new Label(loc.getMenu("Tools"));
		lblTitle.setStyleName("panelTitle");
		panel.add(lblTitle);
		panel.add(toolListPanel);
		setWidget(panel);
		
		final ListBox toolList = new ListBox();
		insertTools(toolList);
		//toolList.setCellRenderer(new MacroCellRenderer());
		toolList.setVisibleItemCount(6);

		FlowPanel centerPanel = createListUpDownRemovePanel();

		// JScrollPane jScrollPane1 = new JScrollPane(toolList);
		// jScrollPane1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED
		// ));
		// toolListPanel.add(jScrollPane1, BorderLayout.CENTER);
		toolListPanel.add(toolList);
		toolListPanel.add(centerPanel);

		FlowPanel toolButtonPanel = new FlowPanel();
		toolListPanel.add(toolButtonPanel);

		final Button btDelete = new Button();
		toolButtonPanel.add(btDelete);
		btDelete.setText(loc.getPlain("Delete"));

		final Button btOpen = new Button();
		toolButtonPanel.add(btOpen);
		btOpen.setText(loc.getPlain("Open"));

		final Button btSave = new Button();
		toolButtonPanel.add(btSave);
		btSave.setText(loc.getMenu("SaveAs") + " ...");

		final Button btShare = new Button();
		toolButtonPanel.add(btShare);
		btShare.setText(loc.getMenu("Share") + " ...");

		// name & icon
		final ToolNameIconPanel namePanel = new ToolNameIconPanel(app);
		namePanel.setTitle(app.getMenu("NameIcon"));
		panel.add(namePanel);

		FlowPanel closePanel = new FlowPanel();
		final Button btClose = new Button(loc.getMenu("Close"));
		closePanel.add(btClose);
		panel.add(closePanel);

		ClickHandler btnClickHandler = new ClickHandler() {
			public void actionPerformed(Object src) {
				if (src == btClose) {
					// ensure to set macro properties from namePanel
				//	namePanel.init(null, null);

					// make sure new macro command gets into dictionary
					app.updateCommandDictionary();

					// destroy dialog
					hide();
					
				} else if (src == btDelete) {
					deleteTools(toolList);
				} else if (src == btOpen) {
					openTools(toolList);
				} else if (src == btSave) {
					saveTools(toolList);
				} else if (src == btShare) {
					uploadToGeoGebraTube(toolList);
				}
			}
			
			public void onClick(ClickEvent event) {
				actionPerformed(event.getSource());
	}
		};

		btShare.addClickHandler(btnClickHandler);
		btSave.addClickHandler(btnClickHandler);
		btDelete.addClickHandler(btnClickHandler);
		btOpen.addClickHandler(btnClickHandler);
		btClose.addClickHandler(btnClickHandler);

		// add selection listener for list
//		final ListSelectionModel selModel = toolList.getSelectionModel();
//		ListSelectionListener selListener = new ListSelectionListener() {
//			public void valueChanged(ListSelectionEvent e) {
//				if (selModel.getValueIsAdjusting())
//					return;
//
//				int[] selIndices = toolList.getSelectedIndices();
//				if (selIndices == null || selIndices.length != 1) {
//					// no or several tools selected
//					namePanel.init(null, null);
//				} else {
//					Macro macro = (Macro) toolsModel
//							.getElementAt(selIndices[0]);
//					namePanel.init(ToolManagerDialogW.this, macro);
//				}
//			}
//		};
//		selModel.addListSelectionListener(selListener);

//		// select first tool in list
//		if (toolsModel.size() > 0)
//			toolList.setSelectedIndex(0);
//		else
//			namePanel.init(null, null);
//
//		setResizable(true);
//		namePanel.setPreferredSize(new Dimension(400, 200));
//
//		app.setComponentOrientation(this);
//
//		pack();
//		setLocationRelativeTo(app.getFrame()); // center
}

	/**
	 * Opens tools in different windows
	 * 
	 * @author Zbynek Konecny
	 * @param toolList
	 *            Tools to be opened
	 */
	private void openTools(ListBox toolList) {
//		Object[] sel = toolList.getSelectedValues();
//		if (sel == null || sel.length == 0)
//			return;
//
//		for (int i = 0; i < sel.length; i++) {
//			final Macro macro = (Macro) sel[i];
//			Thread runner = new Thread() {
//				@Override
//				public void run() {
//					App.debug("before" + app.hashCode());
//					app.setWaitCursor();
//					GeoGebraFrame newframe = GeoGebraFrame.createNewWindow(
//							null, macro);
//					newframe.setTitle(macro.getCommandName());
//					byte[] byteArray = app.getMacroFileAsByteArray();
//					newframe.getApplication().loadMacroFileFromByteArray(
//							byteArray, false);
//					newframe.getApplication().openMacro(macro);
//					app.setDefaultCursor();
//
//				}
//			};
//			runner.start();
//
//			this.setVisible(false);
//		}
	}

	private void insertTools(ListBox list) {
		Kernel kernel = app.getKernel();
		int size = kernel.getMacroNumber();
		for (int i = 0; i < size; i++) {
			Macro macro = kernel.getMacro(i);
			list.addItem(macro.getToolName());
		}
	}

//	private class MacroCellRenderer extends DefaultListCellRenderer {
//
//		private static final long serialVersionUID = 1L;
//
//		/*
//		 * This is the only method defined by ListCellRenderer. We just
//		 * reconfigure the Jlabel each time we're called.
//		 */
//		@Override
//		public Component getListCellRendererComponent(JList list, Object value, // value
//																				// to
//																				// display
//				int index, // cell index
//				boolean iss, // is the cell selected
//				boolean chf) // the list and the cell have the focus
//		{
//			/*
//			 * The DefaultListCellRenderer class will take care of the JLabels
//			 * text property, it's foreground and background colors, and so on.
//			 */
//			super.getListCellRendererComponent(list, value, index, iss, chf);
//
//			if (value != null) {
//				Macro macro = (Macro) value;
//				StringBuilder sb = new StringBuilder();
//				sb.append("<html><b>");
//				sb.append(macro.getToolName());
//				sb.append("</b>: ");
//				sb.append(macro.getNeededTypesString());
//				sb.append("</html>");
//				setText(sb.toString());
//
//				MyImageD img = app.getExternalImage(macro.getIconFileName());
//				if (img != null) {
//					setIcon(new ImageIcon(img.getImage()));
//					Dimension dim = getPreferredSize();
//					dim.height = img.getHeight();
//					setPreferredSize(dim);
//					setMinimumSize(dim);
//				}
//			}
//			return this;
//		}
//	}

	/*
	 * upload selected Tools to GeoGebraTube
	 */
	private void uploadToGeoGebraTube(final ListBox toolList) {
//
//		Thread runner = new Thread() {
//			@Override
//			public void run() {
//				model.uploadToGeoGebraTube(toolList.getSelectedValues());
//			}
//		};
//		runner.start();
	}

	/**
	 * Saves all selected tools in a new file.
	 */
	private void saveTools(ListBox toolList) {
//		Object[] sel = toolList.getSelectedValues();
//		if (sel == null || sel.length == 0)
//			return;
//
//		File file = app.getGuiManager()
//				.showSaveDialog(
//						AppD.FILE_EXT_GEOGEBRA_TOOL,
//						null,
//						GeoGebraConstants.APPLICATION_NAME + " "
//								+ loc.getMenu("Tools"), true, false);
//		if (file == null)
//			return;
//
//		// save selected macros
//		app.saveMacroFile(file, model.getAllTools(sel));
	}

	public void removeMacroFromToolbar(int i) {
		app.getGuiManager().removeFromToolbarDefinition(i);
	}

	public void refreshCustomToolsInToolBar() {
		//app.getGuiManager().refreshCustomToolsInToolBar();

	}

	public void uploadWorksheet(ArrayList<Macro> macros) {
		// create new exporter
//		geogebra.export.GeoGebraTubeExportDesktop exporter = new geogebra.export.GeoGebraTubeExportDesktop(
//				app);
//
//		exporter.uploadWorksheet(macros);

	}

	public void onClick(ClickEvent event) {
	    // TODO Auto-generated method stub
	    
    }

}