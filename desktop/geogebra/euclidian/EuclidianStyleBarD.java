package geogebra.euclidian;

import geogebra.common.awt.GColor;
import geogebra.common.awt.GFont;
import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.euclidian.EuclidianController;
import geogebra.common.euclidian.EuclidianStyleBar;
import geogebra.common.euclidian.EuclidianStyleBarStatic;
import geogebra.common.euclidian.EuclidianView;
import geogebra.common.euclidian.EuclidianViewInterfaceCommon;
import geogebra.common.euclidian.Previewable;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.ConstructionDefaults;
import geogebra.common.kernel.algos.AlgoAttachCopyToView;
import geogebra.common.kernel.algos.AlgoTableText;
import geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import geogebra.common.kernel.geos.GeoButton;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoImage;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.kernel.geos.GeoNumeric;
import geogebra.common.kernel.geos.GeoText;
import geogebra.common.kernel.geos.PointProperties;
import geogebra.common.kernel.geos.TextProperties;
import geogebra.common.main.SelectionManager;
import geogebra.common.main.settings.EuclidianSettings;
import geogebra.common.plugin.EuclidianStyleConstants;
import geogebra.gui.color.ColorPopupMenuButton;
import geogebra.gui.util.GeoGebraIcon;
import geogebra.gui.util.MyToggleButton;
import geogebra.gui.util.PopupMenuButton;
import geogebra.main.AppD;
import geogebra.main.LocalizationD;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Stylebar for the Euclidian Views
 * 
 * @author G. Sturr
 */
public class EuclidianStyleBarD extends JToolBar implements ActionListener,
		EuclidianStyleBar {

	/***/
	private static final long serialVersionUID = 1L;

	/**
	 * Class for buttons visible only when no geo is selected and no geo is to
	 * be created
	 * 
	 * @author mathieu
	 * 
	 */
	protected class MyToggleButtonVisibleIfNoGeo extends MyToggleButton {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * constructor
		 * 
		 * @param icon
		 *            icon of the button
		 * @param height
		 *            height of the button
		 */
		public MyToggleButtonVisibleIfNoGeo(ImageIcon icon, int height) {
			super(icon, height);

		}

		@Override
		public void update(Object[] geos) {
			this.setVisible(geos.length == 0 && !EuclidianView.isPenMode(mode)
					&& mode != EuclidianConstants.MODE_DELETE);
		}

		/*
		 * @Override public Point getToolTipLocation(MouseEvent e) { return new
		 * Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
		 */

	}

	// ggb
	EuclidianController ec;
	protected EuclidianViewInterfaceCommon ev;
	protected AppD app;
	private Construction cons;

	// buttons and lists of buttons
	protected ColorPopupMenuButton btnColor, btnBgColor, btnTextColor;

	protected PopupMenuButton btnLineStyle, btnPointStyle, btnTextSize,
			btnTableTextJustify, btnTableTextBracket, btnLabelStyle,
			btnPointCapture;

	private MyToggleButton btnPen;

	protected MyToggleButton btnShowGrid;

	protected MyToggleButton btnStandardView;

	protected MyToggleButton btnShowAxes;
	protected MyToggleButton btnDeleteSize[];

	MyToggleButton btnBold;

	MyToggleButton btnItalic;

	private MyToggleButton btnDelete;

	private MyToggleButton btnTableTextLinesV;

	private MyToggleButton btnTableTextLinesH;

	MyToggleButton btnFixPosition;

	private PopupMenuButton[] popupBtnList;
	private MyToggleButton[] toggleBtnList;

	// fields for setting/unsetting default geos
	protected HashMap<Integer, Integer> defaultGeoMap;
	private ArrayList<GeoElement> defaultGeos;
	private GeoElement oldDefaultGeo;

	// flags and constants
	protected int iconHeight = 18;
	private Dimension iconDimension = new Dimension(16, iconHeight);
	public int mode = -1;
	protected boolean isIniting;
	private boolean needUndo = false;
	private Integer oldDefaultMode;
	private boolean modeChanged = true;

	// button-specific fields
	// TODO: create button classes so these become internal
	AlgoTableText tableText;

	HashMap<Integer, Integer> lineStyleMap;

	HashMap<Integer, Integer> pointStyleMap;
	protected final LocalizationD loc;

	/*************************************************
	 * Constructs a styleBar
	 * 
	 * @param ev
	 *            view
	 */
	public EuclidianStyleBarD(EuclidianViewInterfaceCommon ev) {

		isIniting = true;

		this.ev = ev;
		ec = ev.getEuclidianController();
		app = (AppD) ev.getApplication();
		this.loc = app.getLocalization();
		cons = app.getKernel().getConstruction();

		// init handling of default geos
		createDefaultMap();
		defaultGeos = new ArrayList<GeoElement>();

		// toolbar display settings
		setFloatable(false);
		Dimension d = getPreferredSize();
		d.height = iconHeight + 8;
		setPreferredSize(d);

		// init button-specific fields
		// TODO: put these in button classes
		EuclidianStyleBarStatic.pointStyleArray = EuclidianView
				.getPointStyles();
		pointStyleMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < EuclidianStyleBarStatic.pointStyleArray.length; i++)
			pointStyleMap.put(EuclidianStyleBarStatic.pointStyleArray[i], i);

		EuclidianStyleBarStatic.lineStyleArray = EuclidianView.getLineTypes();
		lineStyleMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < EuclidianStyleBarStatic.lineStyleArray.length; i++)
			lineStyleMap.put(EuclidianStyleBarStatic.lineStyleArray[i], i);

		setLabels(); // this will also init the GUI

		isIniting = false;

		setMode(ev.getMode()); // this will also update the stylebar

	}

	private boolean firstPaint = true;

	public void resetFirstPaint() {
		firstPaint = true;
	}

	@Override
	public void paint(Graphics g) {

		if (firstPaint) {
			firstPaint = false;
			updateGUI();
		}

		super.paint(g);
	}

	/**
	 * create default map between default geos and modes
	 */
	protected void createDefaultMap() {
		defaultGeoMap = EuclidianStyleBarStatic.createDefaultMap();
	}

	/**
	 * @return euclidian mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Handles ggb mode changes.
	 * 
	 * @param mode
	 *            new mode
	 */
	public void setMode(int mode) {

		if (this.mode == mode) {
			modeChanged = false;
			return;
		}
		modeChanged = true;
		this.mode = mode;

		// MODE_TEXT temporarily switches to MODE_SELECTION_LISTENER
		// so we need to ignore this.
		if (mode == EuclidianConstants.MODE_SELECTION_LISTENER) {
			modeChanged = false;
			return;
		}

		updateStyleBar();

	}

	protected boolean isVisibleInThisView(GeoElement geo) {
		return geo.isVisibleInView(ev.getViewID());
	}

	public void restoreDefaultGeo() {
		if (oldDefaultGeo != null)
			oldDefaultGeo = cons.getConstructionDefaults().getDefaultGeo(
					oldDefaultMode);
	}

	protected ArrayList<GeoElement> activeGeoList;

	/**
	 * Updates the state of the stylebar buttons and the defaultGeo field.
	 */
	public void updateStyleBar() {

		// -----------------------------------------------------
		// Create activeGeoList, a list of geos the stylebar can adjust.
		// These are either the selected geos or the current default geo.
		// Each button uses this list to update its gui and set visibility
		// -----------------------------------------------------
		activeGeoList = new ArrayList<GeoElement>();

		// -----------------------------------------------------
		// MODE_MOVE case: load activeGeoList with all selected geos
		// -----------------------------------------------------
		if (mode == EuclidianConstants.MODE_MOVE) {
			SelectionManager selection = ev.getApplication()
					.getSelectionManager();
			boolean hasGeosInThisView = false;
			for (GeoElement geo : selection.getSelectedGeos()) {
				if (isVisibleInThisView(geo) && geo.isEuclidianVisible()
						&& !geo.isAxis()) {
					hasGeosInThisView = true;
					break;
				}
			}
			for (GeoElement geo : ec.getJustCreatedGeos()) {
				if (isVisibleInThisView(geo) && geo.isEuclidianVisible()) {
					hasGeosInThisView = true;
					break;
				}
			}
			if (hasGeosInThisView) {
				activeGeoList = selection.getSelectedGeos();

				// we also update stylebars according to just created geos
				activeGeoList.addAll(ec.getJustCreatedGeos());
			}
		}
		// -----------------------------------------------------
		// display a selection for the drag-delete-tool
		// can't use a geo element for this
		// -----------------------------------------------------
		else if (mode == EuclidianConstants.MODE_DELETE) {

		}

		// -----------------------------------------------------
		// All other modes: load activeGeoList with current default geo
		// -----------------------------------------------------
		else if (defaultGeoMap.containsKey(mode)) {

			// Save the current default geo state in oldDefaultGeo.
			// Stylebar buttons can temporarily change a default geo, but this
			// default geo is always restored to its previous state after a mode
			// change.

			if (oldDefaultGeo != null && modeChanged) {
				// add oldDefaultGeo to the default map so that the old default
				// is restored
				cons.getConstructionDefaults().addDefaultGeo(oldDefaultMode,
						oldDefaultGeo);
				oldDefaultGeo = null;
				oldDefaultMode = null;
			}

			// get the current default geo
			GeoElement geo = cons.getConstructionDefaults().getDefaultGeo(
					defaultGeoMap.get(mode));
			if (geo != null)
				activeGeoList.add(geo);

			// update the defaultGeos field (needed elsewhere for adjusting
			// default geo state)
			defaultGeos = activeGeoList;

			// update oldDefaultGeo
			if (modeChanged) {
				if (defaultGeos.size() == 0) {
					oldDefaultGeo = null;
					oldDefaultMode = -1;
				} else {
					oldDefaultGeo = defaultGeos.get(0);
					oldDefaultMode = defaultGeoMap.get(mode);
				}
			}

			// we also update stylebars according to just created geos
			activeGeoList.addAll(ec.getJustCreatedGeos());
		}

		updateButtons();

		addButtons();

	}

	protected void updateButtons() {
		// -----------------------------------------------------
		// update the buttons
		// note: this must always be done, even when activeGeoList is empty
		// -----------------------------------------------------
		Object[] geos = activeGeoList.toArray();
		tableText = EuclidianStyleBarStatic.updateTableText(geos, mode);
		for (int i = 0; i < popupBtnList.length; i++) {
			popupBtnList[i].update(geos);
		}
		for (int i = 0; i < toggleBtnList.length; i++) {
			toggleBtnList[i].update(geos);
		}

	}

	public void updateVisualStyle(GeoElement geo) {
		if (activeGeoList.contains(geo))
			updateButtons();
	}

	// =====================================================
	// Init GUI
	// =====================================================

	private void initGUI() {

		createButtons();
		createColorButton();
		createBgColorButton();
		createTextButtons();
		createTableTextButtons();
		setActionCommands();

		addButtons();

		popupBtnList = newPopupBtnList();
		toggleBtnList = newToggleBtnList();

		for (int i = 0; i < popupBtnList.length; i++) {
			// popupBtnList[i].setStandardButton(true);
		}

	}

	protected void setActionCommands() {
		btnShowAxes.setActionCommand("showAxes");
		btnShowGrid.setActionCommand("showGrid");
		btnStandardView.setActionCommand("standardView");
		btnPointCapture.setActionCommand("pointCapture");
	}

	/**
	 * adds/removes buttons (must be called on updates so that separators are
	 * drawn only when needed)
	 */
	private void addButtons() {

		removeAll();

		// --- order matters here

		// add graphics decoration buttons
		addGraphicsDecorationsButtons();
		addBtnPointCapture();

		add(btnColor);
		add(btnBgColor);
		add(btnTextColor);
		add(btnLineStyle);
		add(btnPointStyle);

		// add text decoration buttons
		if (btnBold.isVisible())
			addSeparator();

		add(btnBold);
		add(btnItalic);
		add(btnTextSize);

		add(btnTableTextJustify);
		add(btnTableTextLinesV);
		add(btnTableTextLinesH);
		add(btnTableTextBracket);

		// add(btnPenEraser);

		add(btnLabelStyle);
		// add(btnPointCapture);
		addBtnRotateView();
		// add(btnPenDelete);

		if (btnFixPosition.isVisible())
			addSeparator();
		add(btnFixPosition);

		if (btnDeleteSize[0].isVisible() && btnColor.isVisible()) {
			addSeparator();
		}
		for (int i = 0; i < 3; i++) {
			add(btnDeleteSize[i]);
		}

	}

	/**
	 * add axes, grid, ... buttons
	 */
	protected void addGraphicsDecorationsButtons() {
		add(btnShowAxes);
		add(btnShowGrid);
		addBtnShowPlane();
		add(btnStandardView);
	}

	/**
	 * in 3D, add show plane button
	 */
	protected void addBtnShowPlane() {
		// nothing to do in 2D
	}

	protected PopupMenuButton[] newPopupBtnList() {
		return new PopupMenuButton[] { btnColor, btnBgColor, btnTextColor,
				btnLineStyle, btnPointStyle, btnTextSize, btnTableTextJustify,
				btnTableTextBracket, btnLabelStyle, btnPointCapture, };
	}

	protected MyToggleButton[] newToggleBtnList() {
		return new MyToggleButton[] { btnPen, btnShowGrid, btnShowAxes,
				btnStandardView, btnBold, btnItalic, btnDelete,
				btnTableTextLinesV, btnTableTextLinesH, btnFixPosition,
				this.btnDeleteSize[0], this.btnDeleteSize[1],
				this.btnDeleteSize[2] };
	}

	protected void addBtnPointCapture() {
		add(btnPointCapture);
	}

	protected void addBtnRotateView() {
		// do nothing here (overridden function)
	}

	// =====================================================
	// Create Buttons
	// =====================================================

	protected void createButtons() {

		// ========================================
		// mode button

		// ========================================
		// pen button
		btnPen = new MyToggleButton(
				((AppD) ev.getApplication())
						.getImageIcon("applications-graphics.png"),
				iconHeight) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				this.setVisible((geos.length == 0 && mode == EuclidianConstants.MODE_MOVE)
						|| EuclidianView.isPenMode(mode));
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		btnPen.addActionListener(this);
		// add(btnPen);

		// ========================================
		// delete button
		btnDelete = new MyToggleButton(
				((AppD) ev.getApplication()).getImageIcon("delete_small.gif"),
				iconHeight) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				this.setVisible((geos.length == 0 && mode == EuclidianConstants.MODE_MOVE)
						|| mode == EuclidianConstants.MODE_DELETE);
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		btnDelete.addActionListener(this);
		add(btnDelete);

		// ========================================
		// delete-drag square size
		btnDeleteSize = new MyToggleButton[3];
		String[] deleteIcons = new String[] { "stylingbar_delete_small.png",
				"stylingbar_delete_medium.png", "stylingbar_delete_big.png" };
		for (int i = 0; i < 3; i++) {
			btnDeleteSize[i] = new MyToggleButton(
					app.getImageIcon(deleteIcons[i]), iconHeight) {

				private static final long serialVersionUID = 1L;

				@Override
				public void update(Object[] geos) {
					this.setVisible(mode == EuclidianConstants.MODE_DELETE);
				}
			};
			btnDeleteSize[i].addActionListener(this);
		}
		// ========================================
		// show axes button
		btnShowAxes = new MyToggleButtonVisibleIfNoGeo(
				app.getImageIcon("axes.gif"), iconHeight);
		// btnShowAxes.setPreferredSize(new Dimension(16,16));
		btnShowAxes.addActionListener(this);

		// ========================================
		// show grid button
		btnShowGrid = new MyToggleButtonVisibleIfNoGeo(
				app.getImageIcon("grid.gif"), iconHeight);
		// btnShowGrid.setPreferredSize(new Dimension(16,16));
		btnShowGrid.addActionListener(this);

		// ========================================
		// standard view button
		btnStandardView = new MyToggleButtonVisibleIfNoGeo(
				app.getImageIcon("standard_view.gif"), iconHeight);
		// btnShowGrid.setPreferredSize(new Dimension(16,16));
		btnStandardView.setFocusPainted(false);
		btnStandardView.setBorderPainted(false);
		btnStandardView.setContentAreaFilled(false);
		btnStandardView.addActionListener(this);

		// ========================================
		// line style button

		// create line style icon array
		final Dimension lineStyleIconSize = new Dimension(80, iconHeight);
		ImageIcon[] lineStyleIcons = new ImageIcon[EuclidianStyleBarStatic.lineStyleArray.length];
		for (int i = 0; i < EuclidianStyleBarStatic.lineStyleArray.length; i++)
			lineStyleIcons[i] = GeoGebraIcon.createLineStyleIcon(
					EuclidianStyleBarStatic.lineStyleArray[i], 2,
					lineStyleIconSize, Color.BLACK, null);

		// create button
		btnLineStyle = new PopupMenuButton(app, lineStyleIcons, -1, 1,
				lineStyleIconSize,
				geogebra.common.gui.util.SelectionTable.MODE_ICON) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {

				if (EuclidianView.isPenMode(mode)) {
					this.setVisible(true);
					setFgColor(geogebra.awt.GColorD.getAwtColor(ec.getPen()
							.getPenColor()));
					setSliderValue(ec.getPen().getPenSize());
					setSelectedIndex(lineStyleMap.get(ec.getPen()
							.getPenLineStyle()));
				} else {
					boolean geosOK = (geos.length > 0);
					for (int i = 0; i < geos.length; i++) {
						GeoElement geo = ((GeoElement) geos[i])
								.getGeoElementForPropertiesDialog();
						if (!geo.showLineProperties()) {
							geosOK = false;
							break;
						}
					}

					this.setVisible(geosOK);

					if (geosOK) {
						// setFgColor(((GeoElement)geos[0]).getObjectColor());

						setFgColor(Color.black);
						setSliderValue(((GeoElement) geos[0])
								.getLineThickness());

						setSelectedIndex(lineStyleMap
								.get(((GeoElement) geos[0]).getLineType()));

						this.setKeepVisible(mode == EuclidianConstants.MODE_MOVE);
					}
				}
			}

			@Override
			public ImageIcon getButtonIcon() {
				if (getSelectedIndex() > -1) {
					return GeoGebraIcon.createLineStyleIcon(
							EuclidianStyleBarStatic.lineStyleArray[this
									.getSelectedIndex()],
							this.getSliderValue(), lineStyleIconSize,
							Color.BLACK, null);
				}
				return GeoGebraIcon.createEmptyIcon(lineStyleIconSize.width,
						lineStyleIconSize.height);
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */

		};

		btnLineStyle.getMySlider().setMinimum(1);
		btnLineStyle.getMySlider().setMaximum(13);
		btnLineStyle.getMySlider().setMajorTickSpacing(2);
		btnLineStyle.getMySlider().setMinorTickSpacing(1);
		btnLineStyle.getMySlider().setPaintTicks(true);
		btnLineStyle.setStandardButton(true); // popup on the whole button
		btnLineStyle.addActionListener(this);

		// ========================================
		// point style button

		// create line style icon array
		final Dimension pointStyleIconSize = new Dimension(20, iconHeight);
		ImageIcon[] pointStyleIcons = new ImageIcon[EuclidianStyleBarStatic.pointStyleArray.length];
		for (int i = 0; i < EuclidianStyleBarStatic.pointStyleArray.length; i++)
			pointStyleIcons[i] = GeoGebraIcon.createPointStyleIcon(
					EuclidianStyleBarStatic.pointStyleArray[i], 4,
					pointStyleIconSize, Color.BLACK, null);

		// create button
		btnPointStyle = new PopupMenuButton(app, pointStyleIcons, 2, -1,
				pointStyleIconSize,
				geogebra.common.gui.util.SelectionTable.MODE_ICON) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				GeoElement geo;
				boolean geosOK = (geos.length > 0);
				// btnPointStyle.getMyTable().setVisible(true);
				for (int i = 0; i < geos.length; i++) {
					geo = (GeoElement) geos[i];
					if (!(geo.getGeoElementForPropertiesDialog().isGeoPoint())
							&& (!(geo.isGeoList() && ((GeoList) geo)
									.showPointProperties()))) {
						geosOK = false;
						break;
					}
				}
				this.setVisible(geosOK);

				if (geosOK) {
					// setFgColor(((GeoElement)geos[0]).getObjectColor());
					setFgColor(Color.black);

					// if geo is a matrix, this will return a GeoNumeric...
					geo = ((GeoElement) geos[0])
							.getGeoElementForPropertiesDialog();

					// ... so need to check
					if (geo instanceof PointProperties) {
						setSliderValue(((PointProperties) geo).getPointSize());
						int pointStyle = ((PointProperties) geo)
								.getPointStyle();
						if (pointStyle == -1) // global default point style
							pointStyle = EuclidianStyleConstants.POINT_STYLE_DOT;
						selectPointStyle(pointStyleMap.get(pointStyle));
						this.setKeepVisible(mode == EuclidianConstants.MODE_MOVE);
					}
				}
			}

			@Override
			public ImageIcon getButtonIcon() {
				if (getSelectedIndex() > -1) {
					return GeoGebraIcon.createPointStyleIcon(
							EuclidianStyleBarStatic.pointStyleArray[this
									.getSelectedIndex()],
							this.getSliderValue(), pointStyleIconSize,
							Color.BLACK, null);
				}
				return GeoGebraIcon.createEmptyIcon(pointStyleIconSize.width,
						pointStyleIconSize.height);
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		btnPointStyle.getMySlider().setMinimum(1);
		btnPointStyle.getMySlider().setMaximum(9);
		btnPointStyle.getMySlider().setMajorTickSpacing(2);
		btnPointStyle.getMySlider().setMinorTickSpacing(1);
		btnPointStyle.getMySlider().setPaintTicks(true);
		btnPointStyle.setStandardButton(true); // popup on the whole button
		btnPointStyle.addActionListener(this);

		// ========================================
		// caption style button

		String[] captionArray = new String[] { loc.getPlain("stylebar.Hidden"), // index
																				// 4
				loc.getPlain("Name"), // index 0
				loc.getPlain("NameAndValue"), // index 1
				loc.getPlain("Value"), // index 2
				loc.getPlain("Caption") // index 3
		};

		btnLabelStyle = new PopupMenuButton(app, captionArray, -1, 1,
				new Dimension(0, iconHeight),
				geogebra.common.gui.util.SelectionTable.MODE_TEXT) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				boolean geosOK = false;
				GeoElement geo = null;
				if (mode == EuclidianConstants.MODE_MOVE) {
					for (int i = 0; i < geos.length; i++) {
						if (((GeoElement) geos[i]).isLabelShowable()
								|| ((GeoElement) geos[i]).isGeoAngle()
								|| (((GeoElement) geos[i]).isGeoNumeric() ? ((GeoNumeric) geos[i])
										.isSliderFixed() : false)) {
							geo = (GeoElement) geos[i];
							geosOK = true;
							break;
						}
					}
				} else if (app.getLabelingStyle() == ConstructionDefaults.LABEL_VISIBLE_ALWAYS_OFF) {
					this.setVisible(false);
					return;
				} else if (app.getLabelingStyle() == ConstructionDefaults.LABEL_VISIBLE_POINTS_ONLY) {
					for (int i = 0; i < geos.length; i++) {
						if (((GeoElement) geos[i]).isLabelShowable()
								&& ((GeoElement) geos[i]).isGeoPoint()) {
							geo = (GeoElement) geos[i];
							geosOK = true;
							break;
						}
					}
				} else {
					for (int i = 0; i < geos.length; i++) {
						if (((GeoElement) geos[i]).isLabelShowable()
								|| ((GeoElement) geos[i]).isGeoAngle()
								|| (((GeoElement) geos[i]).isGeoNumeric() ? ((GeoNumeric) geos[i])
										.isSliderFixed() : false)) {
							geo = (GeoElement) geos[i];
							geosOK = true;
							break;
						}
					}
				}
				this.setVisible(geosOK);

				if (geosOK) {
					if (!geo.isLabelVisible())
						setSelectedIndex(0);
					else
						setSelectedIndex(geo.getLabelMode() + 1);

				}
			}

			@Override
			public ImageIcon getButtonIcon() {
				return (ImageIcon) this.getIcon();
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		ImageIcon ic = app.getImageIcon("mode_showhidelabel_16.gif");
		btnLabelStyle.setIconSize(new Dimension(ic.getIconWidth(), iconHeight));
		btnLabelStyle.setIcon(ic);
		btnLabelStyle.setStandardButton(true);
		btnLabelStyle.addActionListener(this);
		btnLabelStyle.setKeepVisible(false);

		// ========================================
		// point capture button

		String[] strPointCapturing = { app.getMenu("Labeling.automatic"),
				app.getMenu("SnapToGrid"), app.getMenu("FixedToGrid"),
				app.getMenu("off") };

		btnPointCapture = new PopupMenuButton(app, strPointCapturing, -1, 1,
				new Dimension(0, iconHeight),
				geogebra.common.gui.util.SelectionTable.MODE_TEXT) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				this.setVisible(geos.length == 0
						&& !EuclidianView.isPenMode(mode)
						&& mode != EuclidianConstants.MODE_DELETE);
			}

			@Override
			public ImageIcon getButtonIcon() {
				return (ImageIcon) this.getIcon();
			}
		};

		ImageIcon ptCaptureIcon = app
				.getImageIcon("stylingbar_graphicsview_point_capturing.gif");
		btnPointCapture.setIconSize(new Dimension(ptCaptureIcon.getIconWidth(),
				iconHeight));
		btnPointCapture.setIcon(ptCaptureIcon);
		btnPointCapture.setStandardButton(true); // popup on the whole button
		btnPointCapture.addActionListener(this);
		btnPointCapture.setKeepVisible(false);

		// ========================================
		// fixed position button
		btnFixPosition = new MyToggleButton(app.getImageIcon("pin.png"),
				iconHeight) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {

				boolean geosOK = checkGeos(geos);

				setVisible(geosOK);
				if (geosOK) {
					if (geos[0] instanceof AbsoluteScreenLocateable
							&& !((GeoElement) geos[0]).isGeoList()) {
						AbsoluteScreenLocateable geo = (AbsoluteScreenLocateable) ((GeoElement) geos[0])
								.getGeoElementForPropertiesDialog();
						btnFixPosition.setSelected(geo
								.isAbsoluteScreenLocActive());
					} else if (((GeoElement) geos[0]).getParentAlgorithm() instanceof AlgoAttachCopyToView) {
						btnFixPosition.setSelected(true);
					} else {
						btnFixPosition.setSelected(false);
					}
				}
			}

			private boolean checkGeos(Object[] geos) {
				if (geos.length <= 0) {
					return false;
				}

				for (int i = 0; i < geos.length; i++) {
					GeoElement geo = (GeoElement) geos[i];

					if (!geo.isPinnable()) {
						return false;
					}

					if (geo.isGeoSegment()) {
						if (geo.getParentAlgorithm() != null
								&& geo.getParentAlgorithm().getInput().length == 3) {
							// segment is output from a Polygon
							return false;
						}
					}

				}
				return true;
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */

		};
		btnFixPosition.addActionListener(this);

	}

	// ========================================
	// object color button (color for everything except text)

	protected void createColorButton() {

		final Dimension colorIconSize = new Dimension(20, iconHeight);
		btnColor = new ColorPopupMenuButton(app, colorIconSize,
				ColorPopupMenuButton.COLORSET_DEFAULT, true) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {

				if (EuclidianView.isPenMode(mode)) {
					this.setVisible(true);

					setSelectedIndex(getColorIndex(geogebra.awt.GColorD
							.getAwtColor(ec.getPen().getPenColor())));

					setSliderValue(100);
					getMySlider().setVisible(false);

				} else {
					boolean geosOK = (geos.length > 0 || EuclidianView
							.isPenMode(mode));
					for (int i = 0; i < geos.length; i++) {
						GeoElement geo = ((GeoElement) geos[i])
								.getGeoElementForPropertiesDialog();
						if (geo instanceof GeoImage || geo instanceof GeoText
								|| geo instanceof GeoButton) {
							geosOK = false;
							break;
						}
					}

					setVisible(geosOK);

					if (geosOK) {
						// get color from first geo
						geogebra.common.awt.GColor geoColor;
						geoColor = ((GeoElement) geos[0]).getObjectColor();

						// check if selection contains a fillable geo
						// if true, then set slider to first fillable's alpha
						// value
						float alpha = 1.0f;
						boolean hasFillable = false;
						for (int i = 0; i < geos.length; i++) {
							if (((GeoElement) geos[i]).isFillable()) {
								hasFillable = true;
								alpha = ((GeoElement) geos[i]).getAlphaValue();
								break;
							}
						}

						if (hasFillable)
							setToolTipText(app
									.getPlain("stylebar.ColorTransparency"));
						else
							setToolTipText(loc.getPlain("stylebar.Color"));

						setSliderValue(Math.round(alpha * 100));

						updateColorTable();

						// find the geoColor in the table and select it
						int index = this.getColorIndex(geogebra.awt.GColorD
								.getAwtColor(geoColor));
						setSelectedIndex(index);
						setDefaultColor(alpha, geoColor);

						this.setKeepVisible(mode == EuclidianConstants.MODE_MOVE);
					}
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */

		};

		btnColor.setStandardButton(true); // popup on the whole button
		btnColor.addActionListener(this);
	}

	protected void createBgColorButton() {

		final Dimension bgColorIconSize = new Dimension(20, iconHeight);

		btnBgColor = new ColorPopupMenuButton(app, bgColorIconSize,
				ColorPopupMenuButton.COLORSET_BGCOLOR, false) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {

				boolean geosOK = (geos.length > 0);
				for (int i = 0; i < geos.length; i++) {
					GeoElement geo = ((GeoElement) geos[i])
							.getGeoElementForPropertiesDialog();
					if (!(geo instanceof GeoText)
							&& !(geo instanceof GeoButton)) {
						geosOK = false;
						break;
					}
				}

				setVisible(geosOK);

				if (geosOK) {
					// get color from first geo
					geogebra.common.awt.GColor geoColor;
					geoColor = ((GeoElement) geos[0]).getBackgroundColor();

					/*
					 * // check if selection contains a fillable geo // if true,
					 * then set slider to first fillable's alpha value float
					 * alpha = 1.0f; boolean hasFillable = false; for (int i =
					 * 0; i < geos.length; i++) { if (((GeoElement)
					 * geos[i]).isFillable()) { hasFillable = true; alpha =
					 * ((GeoElement) geos[i]).getAlphaValue(); break; } }
					 * getMySlider().setVisible(hasFillable);
					 * setSliderValue(Math.round(alpha * 100));
					 */
					float alpha = 1.0f;
					updateColorTable();

					// find the geoColor in the table and select it
					int index = getColorIndex(geogebra.awt.GColorD
							.getAwtColor(geoColor));
					setSelectedIndex(index);
					setDefaultColor(alpha, geoColor);

					// if nothing was selected, set the icon to show the
					// non-standard color
					if (index == -1) {
						this.setIcon(GeoGebraIcon.createColorSwatchIcon(alpha,
								bgColorIconSize,
								geogebra.awt.GColorD.getAwtColor(geoColor),
								null));
					}
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */

		};
		btnBgColor.setKeepVisible(true);
		btnBgColor.setStandardButton(true); // popup on the whole button
		btnBgColor.addActionListener(this);
	}

	// =====================================================
	// Text Format Buttons
	// =====================================================

	static boolean checkGeoText(Object[] geos) {
		boolean geosOK = (geos.length > 0);
		for (int i = 0; i < geos.length; i++) {
			if (!(((GeoElement) geos[i]).getGeoElementForPropertiesDialog() instanceof TextProperties)) {
				geosOK = false;
				break;
			}
		}
		return geosOK;
	}

	protected void createTextButtons() {

		// ========================
		// text color button
		final Dimension textColorIconSize = new Dimension(20, iconHeight);

		btnTextColor = new ColorPopupMenuButton(app, textColorIconSize,
				ColorPopupMenuButton.COLORSET_DEFAULT, false) {

			private static final long serialVersionUID = 1L;

			private Color geoColor;

			@Override
			public void update(Object[] geos) {

				boolean geosOK = checkGeoText(geos);
				setVisible(geosOK);

				if (geosOK) {
					GeoElement geo = ((GeoElement) geos[0])
							.getGeoElementForPropertiesDialog();
					geoColor = geogebra.awt.GColorD.getAwtColor(geo
							.getObjectColor());
					updateColorTable();

					// find the geoColor in the table and select it
					int index = this.getColorIndex(geoColor);
					setSelectedIndex(index);

					// if nothing was selected, set the icon to show the
					// non-standard color
					if (index == -1) {
						this.setIcon(getButtonIcon());
					}

					setFgColor(geoColor);
					setFontStyle(((TextProperties) geo).getFontStyle());
				}
			}

			@Override
			public ImageIcon getButtonIcon() {
				return GeoGebraIcon.createTextSymbolIcon("A",
						app.getPlainFont(), textColorIconSize,
						geogebra.awt.GColorD.getAwtColor(getSelectedColor()),
						null);
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */

		};

		btnTextColor.setStandardButton(true); // popup on the whole button
		btnTextColor.addActionListener(this);

		// ========================================
		// bold text button
		ImageIcon boldIcon = GeoGebraIcon.createStringIcon(loc.getPlain("Bold")
				.substring(0, 1), app.getPlainFont(), true, false, true,
				iconDimension, Color.black, null);
		btnBold = new MyToggleButton(boldIcon, iconHeight) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {

				boolean geosOK = checkGeoText(geos)
						&& !((GeoElement) geos[0]).isGeoTextField();
				setVisible(geosOK);
				if (geosOK) {
					GeoElement geo = ((GeoElement) geos[0])
							.getGeoElementForPropertiesDialog();
					int style = ((TextProperties) geo).getFontStyle();
					btnBold.setSelected(style == Font.BOLD
							|| style == (Font.BOLD + Font.ITALIC));
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		btnBold.addActionListener(this);

		// ========================================
		// italic text button
		ImageIcon italicIcon = GeoGebraIcon.createStringIcon(
				loc.getPlain("Italic").substring(0, 1), app.getPlainFont(),
				false, true, true, iconDimension, Color.black, null);
		btnItalic = new MyToggleButton(italicIcon, iconHeight) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {

				boolean geosOK = checkGeoText(geos)
						&& !((GeoElement) geos[0]).isGeoTextField();
				setVisible(geosOK);
				this.setVisible(geosOK);
				if (geosOK) {
					GeoElement geo = ((GeoElement) geos[0])
							.getGeoElementForPropertiesDialog();
					int style = ((TextProperties) geo).getFontStyle();
					btnItalic.setSelected(style == Font.ITALIC
							|| style == (Font.BOLD + Font.ITALIC));
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */

		};
		btnItalic.addActionListener(this);

		// ========================================
		// text size button

		String[] textSizeArray = app.getLocalization().getFontSizeStrings();

		btnTextSize = new PopupMenuButton(app, textSizeArray, -1, 1,
				new Dimension(-1, iconHeight),
				geogebra.common.gui.util.SelectionTable.MODE_TEXT) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {

				boolean geosOK = checkGeoText(geos);
				setVisible(geosOK);

				if (geosOK) {
					GeoElement geo = ((GeoElement) geos[0])
							.getGeoElementForPropertiesDialog();
					setSelectedIndex(GeoText
							.getFontSizeIndex(((TextProperties) geo)
									.getFontSizeMultiplier())); // font size
																// ranges from
					// -4 to 4, transform
					// this to 0,1,..,4
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		btnTextSize.addActionListener(this);
		btnTextSize.setStandardButton(true); // popup on the whole button
		btnTextSize.setKeepVisible(false);
	}

	// ================================================
	// Create TableText buttons
	// ================================================

	protected void createTableTextButtons() {

		// ==============================
		// justification popup
		ImageIcon[] justifyIcons = new ImageIcon[] {
				app.getImageIcon("format-justify-left.png"),
				app.getImageIcon("format-justify-center.png"),
				app.getImageIcon("format-justify-right.png") };
		btnTableTextJustify = new PopupMenuButton((AppD) ev.getApplication(),
				justifyIcons, 1, -1, new Dimension(20, iconHeight),
				geogebra.common.gui.util.SelectionTable.MODE_ICON) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				if (tableText != null) {
					this.setVisible(true);
					String justification = tableText.getJustification();
					if (justification.equals("c"))
						btnTableTextJustify.setSelectedIndex(1);
					else if (justification.equals("r"))
						btnTableTextJustify.setSelectedIndex(2);
					else
						btnTableTextJustify.setSelectedIndex(0); // left align

				} else {
					this.setVisible(false);
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};

		btnTableTextJustify.addActionListener(this);
		btnTableTextJustify.setKeepVisible(false);

		// ==============================
		// bracket style popup

		ImageIcon[] bracketIcons = new ImageIcon[EuclidianStyleBarStatic.bracketArray.length];
		for (int i = 0; i < bracketIcons.length; i++) {
			bracketIcons[i] = GeoGebraIcon.createStringIcon(
					EuclidianStyleBarStatic.bracketArray[i],
					app.getPlainFont(), true, false, true, new Dimension(30,
							iconHeight), Color.BLACK, null);
		}

		btnTableTextBracket = new PopupMenuButton((AppD) ev.getApplication(),
				bracketIcons, 2, -1, new Dimension(30, iconHeight),
				geogebra.common.gui.util.SelectionTable.MODE_ICON) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				if (tableText != null) {
					this.setVisible(true);
					String s = tableText.getOpenSymbol() + " "
							+ tableText.getCloseSymbol();
					int index = 0;
					for (int i = 0; i < EuclidianStyleBarStatic.bracketArray.length; i++) {
						if (s.equals(EuclidianStyleBarStatic.bracketArray[i])) {
							index = i;
							break;
						}
					}
					// System.out.println("index" + index);
					btnTableTextBracket.setSelectedIndex(index);

				} else {
					this.setVisible(false);
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};

		btnTableTextBracket.addActionListener(this);
		btnTableTextBracket.setKeepVisible(false);

		// ====================================
		// vertical grid lines toggle button
		btnTableTextLinesV = new MyToggleButton(
				GeoGebraIcon.createVGridIcon(iconDimension), iconHeight) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				if (tableText != null) {
					setVisible(true);
					setSelected(tableText.isVerticalLines());
				} else {
					setVisible(false);
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		btnTableTextLinesV.addActionListener(this);

		// ====================================
		// horizontal grid lines toggle button
		btnTableTextLinesH = new MyToggleButton(
				GeoGebraIcon.createHGridIcon(iconDimension), iconHeight) {

			private static final long serialVersionUID = 1L;

			@Override
			public void update(Object[] geos) {
				if (tableText != null) {
					setVisible(true);
					setSelected(tableText.isHorizontalLines());
				} else {
					setVisible(false);
				}
			}

			/*
			 * @Override public Point getToolTipLocation(MouseEvent e) { return
			 * new Point(TOOLTIP_LOCATION_X, TOOLTIP_LOCATION_Y); }
			 */
		};
		btnTableTextLinesH.addActionListener(this);
	}

	// =====================================================
	// Event Handlers
	// =====================================================

	public void updateGUI() {

		if (isIniting)
			return;

		btnPointCapture.removeActionListener(this);
		updateButtonPointCapture(ev.getPointCapturingMode());
		btnPointCapture.addActionListener(this);

		btnPen.removeActionListener(this);
		btnPen.setSelected(EuclidianView.isPenMode(mode));
		btnPen.addActionListener(this);

		btnDelete.removeActionListener(this);
		btnDelete.setSelected(mode == EuclidianConstants.MODE_DELETE);
		btnDelete.addActionListener(this);

		btnShowAxes.removeActionListener(this);
		btnShowAxes.setSelected(ev.getShowXaxis());
		btnShowAxes.addActionListener(this);

		btnShowGrid.removeActionListener(this);
		btnShowGrid.setSelected(ev.getShowGrid());
		btnShowGrid.addActionListener(this);

		btnStandardView.removeActionListener(this);
		btnStandardView.setSelected(false);
		btnStandardView.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		needUndo = false;

		ArrayList<GeoElement> targetGeos = new ArrayList<GeoElement>();
		targetGeos.addAll(ec.getJustCreatedGeos());
		if (mode != EuclidianConstants.MODE_MOVE) {
			targetGeos.addAll(defaultGeos);
			Previewable p = ev.getPreviewDrawable();
			if (p != null) {
				GeoElement geo = p.getGeoElement();
				if (geo != null) {
					targetGeos.add(geo);
				}
			}
		} else
			targetGeos.addAll(app.getSelectionManager().getSelectedGeos());

		processSource(source, targetGeos);

		if (needUndo) {
			app.storeUndoInfo();
			needUndo = false;
		}

	}

	/**
	 * process the action performed
	 * 
	 * @param source
	 *            toggle / popup button
	 * @param targetGeos
	 *            geos
	 */
	protected void processSource(Object source, ArrayList<GeoElement> targetGeos) {

		if ((source instanceof JButton)
				&& (EuclidianStyleBarStatic.processSourceCommon(
						((JButton) source).getActionCommand(), targetGeos, ev)))
			return;

		else if (source == btnColor) {
			if (EuclidianView.isPenMode(mode)) {
				ec.getPen().setPenColor((btnColor.getSelectedColor()));
				// btnLineStyle.setFgColor((Color)btnColor.getSelectedValue());
			} else {
				GColor color = btnColor.getSelectedColor();
				float alpha = btnColor.getSliderValue() / 100.0f;
				needUndo = EuclidianStyleBarStatic.applyColor(targetGeos,
						color, alpha, app);
				// btnLineStyle.setFgColor((Color)btnColor.getSelectedValue());
				// btnPointStyle.setFgColor((Color)btnColor.getSelectedValue());
			}
		}

		else if (source == btnBgColor) {
			if (btnBgColor.getSelectedIndex() >= 0) {
				GColor color = btnBgColor.getSelectedColor();
				float alpha = btnBgColor.getSliderValue() / 100.0f;
				needUndo = EuclidianStyleBarStatic.applyBgColor(targetGeos,
						color, alpha);
			}
		}

		else if (source == btnTextColor) {
			if (btnTextColor.getSelectedIndex() >= 0) {
				GColor color = btnTextColor.getSelectedColor();
				needUndo = EuclidianStyleBarStatic.applyTextColor(targetGeos,
						color);
				// btnTextColor.setFgColor((Color)btnTextColor.getSelectedValue());
				// btnItalic.setForeground((Color)btnTextColor.getSelectedValue());
				// btnBold.setForeground((Color)btnTextColor.getSelectedValue());
			}
		} else if (source == btnLineStyle) {
			if (btnLineStyle.getSelectedValue() != null) {
				if (EuclidianView.isPenMode(mode)) {
					ec.getPen().setPenLineStyle(
							EuclidianStyleBarStatic.lineStyleArray[btnLineStyle
									.getSelectedIndex()]);
					ec.getPen().setPenSize(btnLineStyle.getSliderValue());
				} else {
					int selectedIndex = btnLineStyle.getSelectedIndex();
					int lineSize = btnLineStyle.getSliderValue();
					needUndo = EuclidianStyleBarStatic.applyLineStyle(
							targetGeos, selectedIndex, lineSize);
				}

			}
		} else if (source == btnPointStyle) {
			if (btnPointStyle.getSelectedValue() != null) {
				int pointStyleSelIndex = btnPointStyle.getSelectedIndex();
				int pointSize = btnPointStyle.getSliderValue();
				needUndo = EuclidianStyleBarStatic.applyPointStyle(targetGeos,
						pointStyleSelIndex, pointSize);
			}
		} else if (source == btnBold) {
			needUndo = EuclidianStyleBarStatic.applyFontStyle(targetGeos,
					GFont.ITALIC, btnBold.isSelected() ? GFont.BOLD
							: GFont.PLAIN);
		} else if (source == btnItalic) {
			needUndo = EuclidianStyleBarStatic.applyFontStyle(targetGeos,
					GFont.BOLD, btnItalic.isSelected() ? GFont.ITALIC
							: GFont.PLAIN);
		} else if (source == btnTextSize) {
			needUndo = EuclidianStyleBarStatic.applyTextSize(targetGeos,
					btnTextSize.getSelectedIndex());
		} else if (source == btnLabelStyle) {
			needUndo = EuclidianStyleBarStatic.applyCaptionStyle(targetGeos,
					mode, btnLabelStyle.getSelectedIndex());
		}

		else if (source == btnTableTextJustify || source == btnTableTextLinesH
				|| source == btnTableTextLinesV
				|| source == btnTableTextBracket) {
			EuclidianStyleBarStatic.applyTableTextFormat(targetGeos,
					btnTableTextJustify.getSelectedIndex(),
					btnTableTextLinesH.isSelected(),
					btnTableTextLinesV.isSelected(),
					btnTableTextBracket.getSelectedIndex(), app);
		}

		else if (source == btnFixPosition) {
			needUndo = EuclidianStyleBarStatic.applyFixPosition(targetGeos,
					btnFixPosition.isSelected(), ev) != null;
		}

		for (int i = 0; i < 3; i++) {
			if (source == btnDeleteSize[i]) {
				setDelSize(i);
			}
		}
	}

	private void setDelSize(int s) {
		ev.getSettings().setDeleteToolSize(EuclidianSettings.DELETE_SIZES[s]);
		for (int i = 0; i < 3; i++) {
			btnDeleteSize[i].setSelected(i == s);
			btnDeleteSize[i].setEnabled(i != s);
		}
	}

	public void updateButtonPointCapture(int mode1) {
		if (mode1 == 3 || mode1 == 0)
			mode1 = 3 - mode1; // swap 0 and 3
		btnPointCapture.setSelectedIndex(mode1);
	}

	// ==============================================
	// Apply Styles
	// ==============================================

	/**
	 * Set labels with localized strings.
	 */
	public void setLabels() {

		initGUI();
		updateStyleBar();

		btnShowGrid.setToolTipText(loc.getPlainTooltip("stylebar.Grid"));
		btnShowAxes.setToolTipText(loc.getPlainTooltip("stylebar.Axes"));
		btnStandardView.setToolTipText(loc
				.getPlainTooltip("stylebar.ViewDefault"));
		btnPointCapture.setToolTipText(loc.getPlainTooltip("stylebar.Capture"));

		btnLabelStyle.setToolTipText(loc.getPlainTooltip("stylebar.Label"));

		btnColor.setToolTipText(loc.getPlainTooltip("stylebar.Color"));
		btnBgColor.setToolTipText(loc.getPlainTooltip("stylebar.BgColor"));

		btnLineStyle.setToolTipText(loc.getPlainTooltip("stylebar.LineStyle"));
		btnPointStyle
				.setToolTipText(loc.getPlainTooltip("stylebar.PointStyle"));

		btnTextColor.setToolTipText(loc.getPlainTooltip("stylebar.TextColor"));
		btnTextSize.setToolTipText(loc.getPlainTooltip("stylebar.TextSize"));
		btnBold.setToolTipText(loc.getPlainTooltip("stylebar.Bold"));
		btnItalic.setToolTipText(loc.getPlainTooltip("stylebar.Italic"));
		btnTableTextJustify.setToolTipText(loc
				.getPlainTooltip("stylebar.Align"));
		btnTableTextBracket.setToolTipText(loc
				.getPlainTooltip("stylebar.Bracket"));
		btnTableTextLinesV.setToolTipText(loc
				.getPlainTooltip("stylebar.VerticalLine"));
		btnTableTextLinesH.setToolTipText(loc
				.getPlainTooltip("stylebar.HorizontalLine"));

		btnPen.setToolTipText(loc.getPlainTooltip("stylebar.Pen"));
		btnFixPosition.setToolTipText(loc
				.getPlainTooltip("AbsoluteScreenLocation"));

		btnDeleteSize[0].setToolTipText(loc.getPlainTooltip("Small"));
		btnDeleteSize[1].setToolTipText(loc.getPlainTooltip("Medium"));
		btnDeleteSize[2].setToolTipText(loc.getPlainTooltip("Large"));
	}

	public int getPointCaptureSelectedIndex() {
		return btnPointCapture.getSelectedIndex();
	}

	@Override
	public void hidePopups() {
		// not needed in Desktop
	}

	protected PopupMenuButton getBtnPointStyle() {
		return btnPointStyle;
	}

	protected void selectPointStyle(int idx) {
		btnPointStyle.setSelectedIndex(idx);
	}


}
