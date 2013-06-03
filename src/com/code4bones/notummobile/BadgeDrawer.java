package com.code4bones.notummobile;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

public class BadgeDrawer {

	public Canvas mCanvas = null;
	public RectF  mRect   = null;
	public String[] mColors = new String[4];
	private Paint pnBorder = new Paint();
	private Paint pnBgr1 = new Paint();
	private Paint pnBgr2 = new Paint();
	private RectF rcOut;
	private RectF rcInn;
	private int   mRound = 12;
	
	public BadgeDrawer(RectF rc,Canvas c,String[] clrs) {
		mCanvas = c;
		mRect = rc;
		mColors = clrs;
		pnBorder.setAntiAlias(true);
		pnBgr1.setAntiAlias(true);
		pnBgr2.setAntiAlias(true);
		rcOut = new RectF(mRect);
		rcInn = new RectF(mRect);

		pnBorder.setStyle(Style.STROKE);
		pnBorder.setColor(Color.parseColor(mColors[0]));//setARGB(255,200, 0, 0);
		pnBorder.setStrokeWidth(2);
		pnBorder.setShadowLayer(1, 1, 1, Color.WHITE);
		
		pnBgr1.setStyle(Style.FILL_AND_STROKE);
		pnBgr1.setColor(Color.parseColor(mColors[1]));//setARGB(255, 220, 0, 0);
		
		pnBgr2.setStyle(Style.FILL);
		pnBgr2.setColor(Color.parseColor(mColors[2]));//setARGB(255, 255, 0, 0);
		pnBgr2.setShadowLayer(2, 2, 2, Color.WHITE);
		
		
		rcOut.inset(1f, 1f);
		rcInn.inset(0.5f, 2.f);
	
	}
	
	public void setRadius(int rd) {
		mRound = rd;
	}
	
	public void inset(float dx, float dy) {
		mRect.inset(dx, dy);
		rcOut = new RectF(mRect);
		rcInn = new RectF(mRect);
	}
	
	public void draw() {
		mCanvas.drawRoundRect(rcOut, mRound, mRound, pnBgr1);
		mCanvas.drawRoundRect(rcInn, mRound, mRound, pnBgr2);
		mCanvas.drawRoundRect(mRect, mRound, mRound, pnBorder);
	}
	
	public void drawInner() {
		mCanvas.drawRoundRect(rcInn, mRound, mRound, pnBgr2);
	}
	
	public void drawOutter() {
		mCanvas.drawRoundRect(rcOut, mRound, mRound, pnBgr1);
	}
	
	public void drawBorder() {
		mCanvas.drawRoundRect(mRect, mRound, mRound, pnBorder);
	}
	
	public void addBubble(int part,int alpha) {
		Paint pnBubble = new Paint();
		RectF rcBubble = new RectF(mRect);
		rcBubble.bottom = rcBubble.top + rcBubble.height()/(part==0?2:part);
		rcBubble.inset(-1, -2);
		pnBubble.setAntiAlias(true);
		pnBubble.setStyle(Style.FILL_AND_STROKE);
		pnBubble.setStrokeWidth(2);
		pnBubble.setARGB(alpha,255,255,255);
		mCanvas.drawRoundRect(rcBubble, mRound, mRound, pnBubble);
	}
	
}
