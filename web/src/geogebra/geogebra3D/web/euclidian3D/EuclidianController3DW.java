package geogebra.geogebra3D.web.euclidian3D;

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.euclidian.EuclidianController;
import geogebra.common.euclidian.EuclidianView;
import geogebra.common.euclidian.event.AbstractEvent;
import geogebra.common.euclidian.event.PointerEventType;
import geogebra.common.geogebra3D.euclidian3D.EuclidianController3D;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.arithmetic.MyDouble;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.main.App;
import geogebra.common.util.MyMath;
import geogebra.common.util.debug.GeoGebraProfiler;
import geogebra.common.util.debug.Log;
import geogebra.html5.Browser;
import geogebra.html5.euclidian.EnvironmentStyleW;
import geogebra.html5.euclidian.EuclidianControllerW;
import geogebra.html5.euclidian.EuclidianViewW;
import geogebra.html5.euclidian.IsEuclidianController;
import geogebra.html5.event.HasOffsets;
import geogebra.html5.event.PointerEvent;
import geogebra.html5.event.ZeroOffset;
import geogebra.html5.gui.inputfield.AutoCompleteTextFieldW;
import geogebra.html5.gui.tooltip.ToolTipManagerW;
import geogebra.html5.gui.util.LongTouchManager;
import geogebra.html5.gui.util.LongTouchTimer.LongTouchHandler;
import geogebra.html5.main.AppW;
import geogebra.web.euclidian.EuclidianStyleBarW;
import geogebra.web.gui.GuiManagerW;

import java.util.LinkedList;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.GestureChangeEvent;
import com.google.gwt.event.dom.client.GestureChangeHandler;
import com.google.gwt.event.dom.client.GestureEndEvent;
import com.google.gwt.event.dom.client.GestureEndHandler;
import com.google.gwt.event.dom.client.GestureStartEvent;
import com.google.gwt.event.dom.client.GestureStartHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

public class EuclidianController3DW extends EuclidianController3D implements
        MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOutHandler,
        MouseOverHandler, MouseWheelHandler, TouchStartHandler,
        TouchEndHandler, TouchMoveHandler, TouchCancelHandler,
        GestureStartHandler, GestureEndHandler, GestureChangeHandler,
        HasOffsets, IsEuclidianController, LongTouchHandler {

	private long lastMoveEvent = 0;
	private AbstractEvent waitingTouchMove = null;
	private PointerEvent waitingMouseMove = null;

	public EnvironmentStyleW style;

	@Override
	public EnvironmentStyleW getEnvironmentStyle() {
		return style;
	}

	private LongTouchManager longTouchManager;

	/**
	 * recalculates cached styles concerning browser environment
	 */
	@Override
	public void calculateEnvironment() {
		if (view == null) {
			return;
		}
		style = new EnvironmentStyleW();
		style.setWidthScale(getEnvWidthScale());
		style.setHeightScale(getEnvHeightScale());
		style.setxOffset(getEnvXoffset());
		style.setyOffset(getEnvYoffset());
		style.setScaleX(((AppW) app).getArticleElement().getScaleX());
		style.setScaleY(((AppW) app).getArticleElement().getScaleY());
		style.setScrollLeft(Window.getScrollLeft());
		style.setScrollTop(Window.getScrollTop());
	}

	private float getEnvWidthScale() {
		if (view == null) {
			return 1;
		}
		EuclidianView3DW v = (EuclidianView3DW) view;
		if (v.g2p.getOffsetWidth() != 0) {
			return v.g2p.getCoordinateSpaceWidth() / v.g2p.getOffsetWidth();
		}
		return 0;
	}

	private float getEnvHeightScale() {
		if (view == null) {
			return 1;
		}
		EuclidianView3DW v = (EuclidianView3DW) view;
		if (v.g2p.getOffsetHeight() != 0) {
			return v.g2p.getCoordinateSpaceHeight() / v.g2p.getOffsetHeight();
		}
		return 0;
	}

	private int getEnvXoffset() {
		// return EuclidianViewXOffset;
		// the former solution doesn't update on scrolling
		return Math.round((((EuclidianView3DW) view).getAbsoluteLeft() - Window
		        .getScrollLeft()));

	}

	// private int EuclidianViewXOffset;

	// private int EuclidianViewYOffset;
	/**
	 * @return offset to get correct getY() in mouseEvents
	 */
	private int getEnvYoffset() {
		// return EuclidianViewYOffset;
		// the former solution doesn't update on scrolling
		return ((EuclidianView3DW) view).getAbsoluteTop()
		        - Window.getScrollTop();
	}

	private boolean EuclidianOffsetsInited = false;

	public boolean isOffsetsUpToDate() {
		return EuclidianOffsetsInited;
	}

	private Timer repaintTimer = new Timer() {
		@Override
		public void run() {
			moveIfWaiting();
		}
	};
	private boolean ignoreNextMouseEvent;

	@Override
	public void moveIfWaiting() {
		long time = System.currentTimeMillis();
		if (this.waitingMouseMove != null) {
			GeoGebraProfiler.moveEventsIgnored--;
			this.onMouseMoveNow(waitingMouseMove, time);
			return;
		}
		if (this.waitingTouchMove != null) {
			GeoGebraProfiler.moveEventsIgnored--;
			this.onTouchMoveNow(waitingTouchMove, time);
		}

	}

	public EuclidianController3DW(Kernel kernel) {
		super(kernel.getApplication());
		setKernel(kernel);
		// RealSense.initIfSupported(this);
		// RealSense.createInstance();

		Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				calculateEnvironment();
			}
		});

		Window.addWindowScrollHandler(new Window.ScrollHandler() {

			@Override
			public void onWindowScroll(Window.ScrollEvent event) {
				calculateEnvironment();
			}
		});

		tempNum = new MyDouble(kernel);
		longTouchManager = LongTouchManager.getInstance();
	}

	public void handleLongTouch(int x, int y) {
		PointerEvent event = new PointerEvent(x, y, PointerEventType.TOUCH,
		        ZeroOffset.instance);
		event.setIsRightClick(true);
		wrapMouseReleased(event);
	}

	@Override
	public void setView(EuclidianView view) {
		this.view = view;
		setView3D(view);
	}

	@Override
	public void onGestureChange(GestureChangeEvent event) {
		// AbstractEvent e =
		// geogebra.web.euclidian.event.TouchEvent.wrapEvent(event.getNativeEvent());
		// to not move the canvas (later some sophisticated handling must be
		// find out)
		// event.preventDefault();
		// event.stopPropagation();
	}

	@Override
	public void onGestureEnd(GestureEndEvent event) {
		// AbstractEvent e =
		// geogebra.web.euclidian.event.TouchEvent.wrapEvent(event.getNativeEvent());
		// to not move the canvas (later some sophisticated handling must be
		// find out)
		// event.preventDefault();
		// event.stopPropagation();
	}

	@Override
	public void onGestureStart(GestureStartEvent event) {
		// AbstractEvent e =
		// geogebra.web.euclidian.event.TouchEvent.wrapEvent(event.getNativeEvent());
		// to not move the canvas (later some sophisticated handling must be
		// find out)
		// event.preventDefault();
		// event.stopPropagation();
	}

	@Override
	public void onTouchCancel(TouchCancelEvent event) {
		// AbstractEvent e =
		// geogebra.web.euclidian.event.TouchEvent.wrapEvent(event.getNativeEvent());
		Log.debug(event.getAssociatedType().getName());
	}

	@Override
	public void onTouchMove(TouchMoveEvent event) {
		GeoGebraProfiler.drags++;
		long time = System.currentTimeMillis();
		JsArray<Touch> targets = event.getTargetTouches();
		event.stopPropagation();
		event.preventDefault();
		if (targets.length() == 1) {
			if (time < this.lastMoveEvent
			        + EuclidianViewW.DELAY_BETWEEN_MOVE_EVENTS) {
				AbstractEvent e = PointerEvent.wrapEvent(
				        targets.get(targets.length() - 1), this);
				boolean wasWaiting = waitingTouchMove != null
				        || waitingMouseMove != null;
				this.waitingTouchMove = e;
				this.waitingMouseMove = null;
				GeoGebraProfiler.moveEventsIgnored++;
				if (wasWaiting) {
					this.repaintTimer
					        .schedule(EuclidianViewW.DELAY_UNTIL_MOVE_FINISH);
				}
				return;
			}
			AbstractEvent e = PointerEvent.wrapEvent(
			        targets.get(targets.length() - 1), this);
			if (!draggingBeyondThreshold) {
				longTouchManager.rescheduleTimerIfRunning(this, e.getX(),
				        e.getY(), false);
			} else {
				longTouchManager.cancelTimer();
			}
			onTouchMoveNow(e, time);
		} else if (targets.length() == 2 && app.isShiftDragZoomEnabled()) {
			longTouchManager.cancelTimer();
			AbstractEvent first = PointerEvent.wrapEvent(event.getTouches()
			        .get(0), this);
			AbstractEvent second = PointerEvent.wrapEvent(event.getTouches()
			        .get(1), this);
			this.twoTouchMove(first.getX(), first.getY(), second.getX(),
			        second.getY());
			first.release();
			second.release();
		} else {
			longTouchManager.cancelTimer();
		}
	}

	private static double distance(final AbstractEvent t1,
	        final AbstractEvent t2) {
		return Math.sqrt(Math.pow(t1.getX() - t2.getX(), 2)
		        + Math.pow(t1.getY() - t2.getY(), 2));
	}

	private void onTouchMoveNow(AbstractEvent event, long time) {
		this.lastMoveEvent = time;
		wrapMouseDragged(event);

		this.waitingTouchMove = null;
		this.waitingMouseMove = null;
		int dragTime = (int) (System.currentTimeMillis() - time);
		GeoGebraProfiler.dragTime += dragTime;
		if (dragTime > EuclidianViewW.DELAY_UNTIL_MOVE_FINISH) {
			EuclidianViewW.DELAY_UNTIL_MOVE_FINISH = dragTime + 10;
		}

	}

	@Override
	public void onTouchEnd(TouchEndEvent event) {
		this.ignoreNextMouseEvent = true;
		this.moveIfWaiting();
		EuclidianViewW.resetDelay();
		event.stopPropagation();
		longTouchManager.cancelTimer();
		if (!comboBoxHit()) {
			event.preventDefault();
		}
		if (event.getTouches().length() == 0) {
			// mouseLoc was already adjusted to the EVs coords, do not use
			// offset again
			this.wrapMouseReleased(new PointerEvent(mouseLoc.x, mouseLoc.y,
			        PointerEventType.TOUCH, ZeroOffset.instance));
		}
	}

	@Override
	public void onTouchStart(TouchStartEvent event) {
		this.ignoreNextMouseEvent = true;
		JsArray<Touch> targets = event.getTargetTouches();
		event.stopPropagation();
		calculateEnvironment();
		if (app.getGuiManager() != null) {
			((GuiManagerW) app.getGuiManager())
			        .setActiveToolbarId(App.VIEW_EUCLIDIAN3D);
		}
		if (targets.length() == 1) {
			AbstractEvent e = PointerEvent.wrapEvent(targets.get(0), this);
			if (mode == EuclidianConstants.MODE_MOVE) {
				longTouchManager.scheduleTimer(this, e.getX(), e.getY());
			} else {
				longTouchManager.scheduleTimer(this, e.getX(), e.getY(), 1500);
			}
			wrapMousePressed(e);
			e.release();
		} else if (targets.length() == 2) {
			longTouchManager.cancelTimer();
			AbstractEvent first = PointerEvent.wrapEvent(event.getTouches()
			        .get(0), this);
			AbstractEvent second = PointerEvent.wrapEvent(event.getTouches()
			        .get(1), this);
			this.twoTouchStart(first.getX(), first.getY(), second.getX(),
			        second.getY());
			first.release();
			second.release();
		} else {
			longTouchManager.cancelTimer();
		}
		if ((!isTextfieldHasFocus()) && (!comboBoxHit())) {
			event.preventDefault();
		}
	}

	private static boolean DRAGMODE_MUST_BE_SELECTED = false;
	private static boolean DRAGMODE_IS_RIGHT_CLICK = false;
	private int deltaSum = 0;

	@Override
	public void onMouseWheel(MouseWheelEvent event) {
		// don't want to roll the scrollbar
		double delta = event.getDeltaY();
		// we are on device where many small scrolls come, we want to merge them
		if (delta == 0) {
			deltaSum += getNativeDelta(event.getNativeEvent());
			if (Math.abs(deltaSum) > 40) {
				double ds = deltaSum;
				deltaSum = 0;
				wrapMouseWheelMoved(mouseEventX(event.getClientX()),
				        mouseEventY(event.getClientY()), ds,
				        event.isShiftKeyDown() || event.isMetaKeyDown(),
				        event.isAltKeyDown());
			}
			// normal scrolling
		} else {
			deltaSum = 0;
			wrapMouseWheelMoved(mouseEventX(event.getClientX()),
			        mouseEventY(event.getClientY()), delta,
			        event.isShiftKeyDown() || event.isMetaKeyDown(),
			        event.isAltKeyDown());
		}
		event.preventDefault();
	}

	private native double getNativeDelta(NativeEvent evt) /*-{
		return -evt.wheelDelta;
	}-*/;

	@Override
	public void onMouseOver(MouseOverEvent event) {
		wrapMouseEntered();
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		// hide dialogs if they are open
		int x = event.getClientX() + Window.getScrollLeft();
		int y = event.getClientY() + Window.getScrollTop(); // why scrollLeft &
		                                                    // scrollTop; see
		                                                    // ticket #4049

		int ex = ((EuclidianView3DW) view).getAbsoluteLeft();
		int ey = ((EuclidianView3DW) view).getAbsoluteTop();
		int eWidth = ((EuclidianView3DW) view).getWidth();
		int eHeight = ((EuclidianView3DW) view).getHeight();
		if ((x < ex || x > ex + eWidth) || (y < ey || y > ey + eHeight)) {
			ToolTipManagerW.sharedInstance().hideToolTip();
		}
		// ((EuclidianView3DW) view).resetMsZoomer();
		AbstractEvent e = PointerEvent.wrapEvent(event, this);
		wrapMouseExited(e);
		e.release();
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if (isExternalHandling()) {
			return;
		}

		PointerEvent e = PointerEvent.wrapEvent(event, this);
		event.preventDefault();
		GeoGebraProfiler.drags++;
		long time = System.currentTimeMillis();

		if (time < this.lastMoveEvent
		        + EuclidianViewW.DELAY_BETWEEN_MOVE_EVENTS) {
			boolean wasWaiting = waitingTouchMove != null
			        || waitingMouseMove != null;
			this.waitingMouseMove = e;
			this.waitingTouchMove = null;
			GeoGebraProfiler.moveEventsIgnored++;
			if (wasWaiting) {
				this.repaintTimer
				        .schedule(EuclidianViewW.DELAY_UNTIL_MOVE_FINISH);
			}
			return;
		}

		onMouseMoveNow(e, time);
	}

	public void onMouseMoveNow(PointerEvent event, long time) {
		this.lastMoveEvent = time;
		if (!DRAGMODE_MUST_BE_SELECTED) {
			wrapMouseMoved(event);
		} else {
			event.setIsRightClick(DRAGMODE_IS_RIGHT_CLICK);
			wrapMouseDragged(event);
		}
		event.release();
		this.waitingMouseMove = null;
		this.waitingTouchMove = null;
		int dragTime = (int) (System.currentTimeMillis() - time);
		GeoGebraProfiler.dragTime += dragTime;
		if (dragTime > EuclidianViewW.DELAY_UNTIL_MOVE_FINISH) {
			EuclidianViewW.DELAY_UNTIL_MOVE_FINISH = dragTime + 10;
		}
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		if (this.ignoreNextMouseEvent) {
			this.ignoreNextMouseEvent = false;
			return;
		}

		event.preventDefault();

		AbstractEvent e = PointerEvent.wrapEvent(event, this);
		this.moveIfWaiting();
		EuclidianViewW.resetDelay();
		DRAGMODE_MUST_BE_SELECTED = false;

		// hide dialogs if they are open
		if (app.getGuiManager() != null)
			((GuiManagerW) app.getGuiManager()).removePopup();

		wrapMouseReleased(e);
		e.release();
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		deltaSum = 0;
		if (this.ignoreNextMouseEvent) {
			this.ignoreNextMouseEvent = false;
			return;
		}
		if ((!isTextfieldHasFocus()) && (!comboBoxHit())) {
			event.preventDefault();
		}
		AbstractEvent e = PointerEvent.wrapEvent(event, this);
		if (app.getGuiManager() != null)
			((GuiManagerW) app.getGuiManager())
			        .setActiveToolbarId(App.VIEW_EUCLIDIAN3D);

		if ((!AutoCompleteTextFieldW.showSymbolButtonFocused)
		        && (!isTextfieldHasFocus())) {
			DRAGMODE_MUST_BE_SELECTED = true;
			DRAGMODE_IS_RIGHT_CLICK = e.isRightClick();
		}

		wrapMousePressed(e);
		// hide PopUp if no hits was found.
		if (view.getHits().isEmpty()) {
			if (EuclidianStyleBarW.CURRENT_POP_UP != null) {
				EuclidianStyleBarW.CURRENT_POP_UP.hide();
			}
		}
		e.release();
	}

	private boolean comboBoxHit() {
		if (view.getHits() == null) {
			return false;
		}
		int i = 0;
		while (i < view.getHits().size()) {
			GeoElement hit = view.getHits().get(i++);
			if (hit instanceof GeoList && ((GeoList) hit).drawAsComboBox()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void initToolTipManager() {
		// set tooltip manager
		ToolTipManagerW ttm = ToolTipManagerW.sharedInstance();
		// ttm.setInitialDelay(defaultInitialDelay / 2);
		// ttm.setEnabled((AppW.getAllowToolTips());

	}

	@Override
	protected void resetToolTipManager() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean hitResetIcon() {
		return app.showResetIcon()
		        && ((mouseLoc.y < 20) && (mouseLoc.x > (view.getViewWidth() - 18)));
	}

	private LinkedList<PointerEvent> mousePool = new LinkedList<PointerEvent>();

	@Override
	public LinkedList<PointerEvent> getMouseEventPool() {
		return mousePool;
	}

	private LinkedList<PointerEvent> touchPool = new LinkedList<PointerEvent>();

	@Override
	public LinkedList<PointerEvent> getTouchEventPool() {
		return touchPool;
	}

	@Override
	protected boolean textfieldJustFocusedW(int x, int y, PointerEventType type) {
		return view.textfieldClicked(x, y, type);
	}

	public int touchEventX(int clientX) {
		if (((AppW) app).getLAF().isSmart()) {
			return mouseEventX(clientX - style.getxOffset());
		}
		// IE touch events are mouse events
		return Browser.supportsPointerEvents() ? mouseEventX(clientX)
		        : (clientX - style.getxOffset());
	}

	public int touchEventY(int clientY) {
		if (((AppW) app).getLAF().isSmart()) {
			return mouseEventY(clientY - style.getyOffset());
		}
		// IE touch events are mouse events
		return Browser.supportsPointerEvents() ? mouseEventX(clientY)
		        : (clientY - style.getyOffset());
	}

	/**
	 * @return the multiplier that must be used to multiply the native event
	 *         coordinates
	 */
	public float getScaleXMultiplier() {
		return style.getScaleXMultiplier();
	}

	/**
	 * @return the multiplier that must be used to multiply the native event
	 *         coordinates
	 */
	public float getScaleYMultiplier() {
		return style.getScaleYMultiplier();
	}

	@Override
	public int mouseEventX(int clientX) {
		return Math.round((clientX) * (1 / style.getScaleX())
		        * (1 / style.getHeightScale()));
	}

	@Override
	public int mouseEventY(int clientY) {
		return Math.round((clientY) * (1 / style.getScaleY())
		        * (1 / style.getHeightScale()));
	}

	@Override
	public int getEvID() {
		return view.getEuclidianViewNo();
	}

	@Override
	protected void updateSelectionRectangle(boolean keepScreenRatio) {
		// TODO

	}

	@Override
	protected void processMouseMoved(AbstractEvent e) {

		super.processMouseMoved(e);
		processMouseMoved();

	}

	@Override
	public void update() {
		// no picking with shaders
	}

	/**
	 * coordinates of the center of the multitouch-event
	 */
	protected int oldCenterX, oldCenterY;

	@Override
	public void twoTouchStart(double x1, double y1, double x2, double y2) {

		oldCenterX = (int) (x1 + x2) / 2;
		oldCenterY = (int) (y1 + y2) / 2;

		super.twoTouchStart(x1, y1, x2, y2);

	}

	@Override
	public void twoTouchMove(double x1d, double y1d, double x2d, double y2d) {
		int x1 = (int) x1d;
		int x2 = (int) x2d;
		int y1 = (int) y1d;
		int y2 = (int) y2d;

		// pinch
		super.twoTouchMove(x1, y1, x2, y2);

		int centerX = (x1 + x2) / 2;
		int centerY = (y1 + y2) / 2;

		if (MyMath.length(oldCenterX - centerX, oldCenterY - centerY) > EuclidianControllerW.MIN_MOVE) {
			view.rememberOrigins();
			view.setCoordSystemFromMouseMove(centerX - oldCenterX, centerY
			        - oldCenterY, EuclidianController.MOVE_ROTATE_VIEW);
			viewRotationOccured = true;
			view.repaintView();

			oldCenterX = centerX;
			oldCenterY = centerY;
		}
	}

	/**
	 * @param mx
	 * @param my
	 * @param mz
	 * @param ox
	 * @param oy
	 * @param oz
	 * @param ow
	 * @param name
	 */
	public void onHandValues(int mx, int my, int mz, int ox, int oy, int oz,
	        int ow, String name) {

		App.debug(mx + "," + my + "," + mz + " -- " + name);

	}

}
