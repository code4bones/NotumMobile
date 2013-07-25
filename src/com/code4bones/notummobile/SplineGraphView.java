package com.code4bones.notummobile;


import java.util.List;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class SplineGraphView extends View {

	public SplineGraphView(Context context) {
		super(context);
	}	
	
	public SplineGraphView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}	
	
	public SplineGraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//TODO:
		mRect.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		this.setMeasuredDimension(mRect.width(), mRect.height());
	}
	
	final public Paint mPaintTrg = new Paint();
	final public Paint mGraphPaintX = new Paint();
	final public Paint mBgPaintGrid = new Paint();
	final public Paint mBgPaintY = new Paint();
	final public Paint mBgPaintX = new Paint();
	final public Paint mGraphPaint = new Paint();
	final public Path mGraphPath = new Path();
	final public Paint mGraphPaintLine = new Paint();
	final public Paint mGraphPaintPoint = new Paint();
	
	final public Path mGraphPath2 = new Path();
	public LinearGradient mGradientTrg = null;
	public LinearGradient mGradient = null;
	public LinearGradient mGradientX = null;
	public Coord mCrMin = null;
	public Coord mCrMax = null;
	final public Rect mRect = new Rect();
	final public Handler mHandler = new Handler();
	public List<Coord> mPts = null;
	public List<Coord> mLst = null;
	public float mMinY;
	public float mMaxY;
	public float mMinX;
	public float mMaxX;
	public float mRangeY;
	public float mRangeX;
	public float mStartValue = Float.MAX_VALUE;
	public float mTargetValue = Float.MAX_VALUE;
	public boolean mLimitsSet = false;

	
	public void localInit() {
		
		if ( mGradient != null )
			return;
		
		mGradientTrg = new LinearGradient(0,0,0,mRect.height(),0x80ffff19,0x80ffff19,TileMode.CLAMP);
	
		mGradient = new LinearGradient(0,0,0,mRect.height(),0x800000ff, 0x80000080,TileMode.CLAMP);
		mGradientX = new LinearGradient(0,0,0,mRect.height(),0x800000ff,0x800000cc,TileMode.CLAMP);
		mGraphPaint.setAntiAlias(true);
		mGraphPaint.setStyle(Style.FILL);
		mGraphPaint.setStrokeJoin(Join.ROUND);
		mGraphPaint.setStrokeCap(Cap.ROUND);
		mGraphPaint.setDither(true);
		mGraphPaint.setStrokeWidth(3);
		mGraphPaint.setShader(mGradient);
		mGraphPaint.setAlpha(220);
		

		mPaintTrg.set(mGraphPaint);
		mPaintTrg.setShader(mGradientTrg);
		
		mGraphPaintLine.set(mGraphPaint);
		mGraphPaintPoint.clearShadowLayer();
		mGraphPaintPoint.setColor(Color.RED);
		mGraphPaintPoint.setStyle(Style.FILL);
		
		mBgPaintGrid.setStyle(Style.STROKE);
		mBgPaintGrid.setColor(Color.GRAY);
		
		mBgPaintY.setColor(Color.RED);
		mBgPaintY.setAntiAlias(true);

		mBgPaintX.setColor(Color.WHITE);
		mBgPaintX.setAntiAlias(true);

		
		mGraphPaintX.setShader(mGradientX);
		
	}
	
	public void setPoints(List<Coord> pts,List<Coord> lst) {
		mPts = pts;
		mLst = lst;
		
	}
	
	public void setLimitsValue(float start,float target) {
		mStartValue = start;//translateXY(new Coord(1,start),true).y;
		mTargetValue = target;//translateXY(new Coord(1,target),true).y;
	}
	
	
	public void getMinMax(List<Coord> lst) {
		mMaxY = Float.MIN_VALUE;
		mMinY = Float.MAX_VALUE;
		mMaxX = Float.MIN_VALUE;
		mMinX = Float.MAX_VALUE;
		for ( Coord c : lst ) {
			mMinX = Math.min(mMinX,c.x);
			mMinY = Math.min(mMinY,c.y);

			mMaxY = Math.max(mMaxY,c.y);
			mMaxX = Math.max(mMaxX,c.x);
		}
		
		
		if ( mTargetValue <= mMinY )
			mMinY = mTargetValue-((1+mTargetValue * 0.01f));
		else if ( mTargetValue >= mMaxY )
			mMaxY = mTargetValue+((1+mTargetValue * 0.01f));
		
		mMinY -=  (1+mMinY * 0.01f);
		mMaxY +=  (1+mMaxY * 0.01f);
		
		mRangeY = mMaxY - mMinY;
		mRangeX = mMaxX - mMinX;

		
		mCrMin = translateXY(new Coord(mMinX,mMinY),true);
		mCrMax = translateXY(new Coord(mMaxX,mMaxY),true);
		NetLog.v("MinXY(%f,%f) -> MaxXy(%f,%f) [%f,%f]",mMinX,mMinY,mMaxX,mMaxY,mRangeX,mRangeY);
	}
	
	public Coord translateXY(Coord pt,boolean inv) {
		
		int height = mRect.height();//-20;
		int width  = mRect.width()-45;
		
		Coord res = new Coord();
		float val = pt.y - mMinY;
		float y = (val / mRangeY)*height; 
		res.y = (inv?(height-y):y);//-10;
		
		val = pt.x - mMinX;
		float x = 40+(val / mRangeX)*width;
		res.x = x;
		return res;
	}
	
	public void drawLabel(Canvas c,int count) {
		float width = ((mRect.width()-40) / count)-2;
		
		RectF rc = new RectF(mRect.left+40,mRect.bottom-13,mRect.left+40+width,mRect.bottom);
		int step = mLst.size() / count;
		int day = 1;
		int day_to = day;
		for ( int idx = 0; idx < count;idx++ ) {
			c.drawRoundRect(rc, 0, 0, mGraphPaintX);
			
			if ( idx == count-1 )
				day_to = mLst.size();
			else
				day_to = day+step-1;
			
			String str = String.format("%d - %d",day,day_to);
			c.drawText(str,rc.centerX()-14,rc.bottom-2,mBgPaintX);
			day += step;

			rc.offset(width+2, 0);
		}
	}
	
	public void drawLabels(Canvas c) {
		int daysTotal = mLst.size();
		float weeks = daysTotal / 7;
		float month = weeks * 4;
		float years = month * 12;
		int what = daysTotal;
		if ( years > 0 ) {
			what = (int) years;
		} else if ( month > 0 ) {
			what = (int)month;
		} else if ( weeks > 0 ) {
			what = (int)weeks;
		} 
		NetLog.v("WHAT days = %d,%d",daysTotal,what);
		drawLabel(c,3);
	}
	
	
	
	public void drawBackground(Canvas c) {
		
		float rowCount = 6;
		getMinMax(mLst);
		float rowHeight = mRect.height() / rowCount;
		float step = mRangeY / rowCount;
		float startVal = mMaxY;
		Coord cr = mCrMin;//translateXY(new Coord(mRangeX,mMinY),true);
		float y = mCrMax.y;//+10;
		for ( int row = 0; row < rowCount; row++) {
			if ( cr.y < y+rowHeight )
				break;
			c.drawLine(mRect.left+40, y, mRect.right, y, mBgPaintGrid);
			y+= rowHeight;
			float val = startVal;
			String sVal = String.format("%.2f",val);
			c.drawText(sVal, mRect.left, y-rowHeight+5 ,mBgPaintY);
			startVal-=step;
		}/*
		c.drawLine(mRect.left+40, cr.y, mRect.right, cr.y, mBgPaintGrid);
		String sVal = String.format("%.2f",mMinY);
		c.drawText(sVal, mRect.left, cr.y+5 ,mBgPaintY);
		*/
	}
	
	
	
	@Override
	public void onDraw(Canvas c) {
		
		c.save();
		//c.translate(0,15);
		
		c.getClipBounds(mRect);
		
		// called once
		localInit();

	
		drawBackground(c);
		drawLabels(c);
		getMinMax(mPts);

		
		Coord prev = null;

		int sY = 2;
		for ( Coord pt : mPts) {
			Coord cr = translateXY(pt,true);
			if ( prev == null ) {
				prev = cr;
				mGraphPath.moveTo((float)cr.x, sY+(float)cr.y);
				continue;
			}
			mGraphPath.quadTo((float)prev.x, sY+(float)prev.y, (float)cr.x, sY+(float)cr.y);
			prev = cr;
		}
		
		//c.drawLine(mRect.left+40, tr.y, mRect.right, tr.y, this.mGraphPaintPoint);
		
		// enclose the path
		Coord cr = translateXY(mPts.get(0),true);
		mGraphPath.lineTo(prev.x, mCrMin.y-15);
		mGraphPath.lineTo(cr.x, mCrMin.y-15);
		mGraphPath.lineTo(cr.x, cr.y);
		c.drawPath(mGraphPath, mGraphPaint);
		mGraphPath.rewind();

		Coord tr = translateXY(new Coord(1,mTargetValue),true);
		c.drawLine(mRect.left+40, tr.y, mRect.right-5, tr.y, this.mBgPaintY);
		c.drawText(String.format("Целевое значение! - %.2f",mTargetValue), mRect.centerX()-50, tr.y+6, this.mBgPaintY);
		
		getMinMax(mLst);
		mGraphPath2.rewind();
		mGraphPaintLine.setColor(Color.YELLOW);
		mGraphPaintPoint.setColor(Color.RED);

		
		//mLst.remove(0);
		prev = null;
		for ( Coord pt : mLst ) {
			cr = translateXY(pt,true);
			if ( prev == null ) {
				prev = cr;
			}
			c.drawLine(prev.x, prev.y, cr.x, cr.y, mGraphPaintLine);
			c.drawCircle(cr.x, cr.y, 3, mGraphPaintPoint);
			prev = cr;
		}
		c.restore();
	}
	
	public void repaint() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}
	
}
