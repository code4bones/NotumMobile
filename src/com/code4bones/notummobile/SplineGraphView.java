package com.code4bones.notummobile;


import java.util.ArrayList;
import java.util.List;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

public class SplineGraphView extends View implements View.OnTouchListener {

	interface SplineGraphAdapter {
		public String getXLabel(int item);
		public ArrayList<Coord> getItems();
	}

	
	public SplineGraphView(Context context) {
		super(context);
		this.setOnTouchListener(this);
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
	
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		localInit();
		this.setPoints(mAdapter.getItems());
		getMinMax(SP,mPts);
		getMinMax(LN,mLst);
		super.onSizeChanged(w, h, oldw, oldh);
    }	
	
	// X-Y Axis
	public int mClrLabelY = 0;
	public int mClrLabelBgY = 0;
	public int mClrLabelX = 0;
	public int mClrLabelBgX = 0;
	public Paint mPntLabel = PaintEx.create(true);
	
	// Graph Colors
	public int mClrGraphFill = 0;
	public int mClrGraphLine = 0;
	public int mClrGraphLine2 = 0;
	
	public int mClrGraphPoint = 0;
	public int mClrGraphGridX = 0;
	public int mClrGraphGridY = 0;
	public Paint mPntGraph = PaintEx.create(true);

	
	
	//Limits
	public int mClrLimitsTargetText = 0;
	public int mClrLimitsTargetBg = 0;
	public int mClrLimitsStartText = 0;
	public int mClrLimitsStartBg = 0;	
	public Paint mPntLimits = PaintEx.create(true);
	
	
	final RectF mRcIcon = new RectF();

	final public Path mGraphPath = new Path();
	final public Path mGraphPath2 = new Path();
	public LinearGradient mGradient = null;
	final public Rect mRect = new Rect();
	final public Handler mHandler = new Handler();
	public List<Coord> mPts = null;
	public List<Coord> mLst = null;

	public Coord mCrMin = null;
	public Coord mCrMax = null;
	
	public Coord[] mMin = new Coord[]{new Coord(),new Coord()};
	public Coord[] mMax = new Coord[]{new Coord(),new Coord()};
	
	public float[] mRangeY = new float[2];
	public float[] mRangeX = new float[2];
	public float mStartValue = Float.MAX_VALUE;
	public float mTargetValue = Float.MAX_VALUE;
	public boolean mLimitsSet = false;

	
	public Bitmap mBmpTarget = null;
	public Bitmap mBmpStart = null;
	
	public SplineGraphAdapter mAdapter = null;
	
	public void localInit() {
		
		if ( mGradient != null )
			return;
	
		mBmpTarget = BitmapFactory.decodeResource(this.getResources(), R.drawable.flag_green);
		mBmpStart  = BitmapFactory.decodeResource(this.getResources(), R.drawable.flag_red);
		
		mGradient = new LinearGradient(0,0,0,mRect.height(),0x800000ff, 0x80000080,TileMode.CLAMP);
		
		this.mClrGraphFill = Color.parseColor("#800000ff");
		this.mClrGraphGridX = Color.parseColor("#80ffffff");
		this.mClrGraphGridY = Color.parseColor("#80000000");
		this.mClrGraphLine = Color.parseColor("#dd00ff00");
		this.mClrGraphLine2 = Color.parseColor("#dd00b353");
		
		this.mClrGraphPoint = Color.parseColor("#cc0000");
		this.mClrLabelBgX = Color.parseColor("#ffff00");
		this.mClrLabelBgY = Color.parseColor("#ff0000");
		this.mClrLabelX = Color.parseColor("#6163c7");
		this.mClrLabelY = Color.parseColor("#6163c7");
		
		this.mClrLimitsStartBg = Color.parseColor("#200000bb");
		this.mClrLimitsStartText = Color.parseColor("#ff8030");
		this.mClrLimitsTargetBg = Color.parseColor("#200000bb");
		this.mClrLimitsTargetText = Color.parseColor("#ffffff");
	
		this.mPntLabel.setTypeface(Typeface.DEFAULT_BOLD);
	//	this.mPntLabel.setShadowLayer(6, 4, 4, Color.WHITE);
		this.mPntLimits.setTypeface(Typeface.DEFAULT_BOLD);
	//		this.mPntLimits.setShadowLayer(6, 4, 4, Color.WHITE);
	}
	

	public void setAdapter(SplineGraphAdapter a) {
		this.mAdapter = a;
	}
	
	public void setPoints(ArrayList<Coord> lst) {
		mLst = lst;
		BSpline bs = new BSpline(lst);
		mPts = bs.getInterpolated();
	}
	
	public void setLimitsValue(float start,float target) {
		mStartValue = start;
		mTargetValue = target;
		NetLog.v("Limits %f -> %f",mStartValue,mTargetValue);
	}
	
	
	public void getMinMax(int id,List<Coord> lst) {
		mMax[id].y = Float.MIN_VALUE;
		mMax[id].x = Float.MIN_VALUE;
		mMin[id].y = Float.MAX_VALUE;
		mMin[id].x = Float.MAX_VALUE;
		for ( Coord c : lst ) {
			mMin[id].x = Math.min(mMin[id].x,c.x);
			mMin[id].y = Math.min(mMin[id].y,c.y);

			mMax[id].y = Math.max(mMax[id].y,c.y);
			mMax[id].x = Math.max(mMax[id].x,c.x);
		}
		
		if ( mStartValue < mMin[id].y ) {
			mMin[id].y = mStartValue;
		}
		
		if ( mTargetValue != Float.MAX_VALUE  ) {
			if ( mTargetValue <= mMin[id].y )
				mMin[id].y = mTargetValue-((1+mTargetValue * 0.005f));
			else if ( mTargetValue >= mMax[id].y )
				mMax[id].y = mTargetValue+((1+mTargetValue * 0.005f));
		}
		
		
		mMin[id].y -=  (1+mMin[id].y * 0.005f);
		mMax[id].y +=  (1+mMax[id].y * 0.005f);
		
		mRangeY[id] = mMax[id].y - mMin[id].y;
		mRangeX[id] = mMax[id].x - mMin[id].x;

		
		mCrMin = translateXY(id,new Coord(mMin[id].x,mMin[id].y));
		mCrMax = translateXY(id,new Coord(mMax[id].x,mMax[id].y));
		//NetLog.v("MinXY(%f,%f) -> MaxXy(%f,%f) [%f,%f]",mMinX,mMinY,mMaxX,mMaxY,mRangeX,mRangeY);
	}
	
	public Coord translateXY(int id,Coord pt) {
		return translateXY(id,pt.x,pt.y);
	}

	
	public Coord translateXY(int id,float rx,float ry) {
		int height = mRect.height();
		//int width  = mRect.width()-45;
		int width = getDrawWidth()-45;
		
		Coord res = new Coord();
		float val = ry - mMin[id].y;
		float y = (val / mRangeY[id])*height; 
		res.y = height-y;
		
		val = rx - mMin[id].x;
		float x = 40+(val / mRangeX[id])*width;
		res.x = x+mOffsetX;
		return res;
	}

	public int mXLabelWidth = 40;
	
	public int getDrawWidth() {
		return mLst.size() * mXLabelWidth;
	}
	
	public void drawLabel(Canvas c,float x,int idx) {
		if ( idx > mLst.size()-1 )
			return;
		RectF rc = new RectF(/*mRect.left+40*/x,mRect.bottom-13,x+mXLabelWidth,mRect.bottom);

		this.mPntLabel.setColor(this.mClrLabelBgX);
		rc.left -= 15;
		rc.right -=4;
		//mPntLabel.setStyle(Style.STROKE);
		//c.drawRoundRect(rc, 3, 3, this.mPntLabel);
		this.mPntLabel.setColor(this.mClrLabelX);
		String str = this.mAdapter.getXLabel(idx);
		c.drawText(str,rc.left,rc.bottom-2,this.mPntLabel);
	}
	
	
	final public int LN = 0;
	final public int SP = 1;
	
	public void drawBackground(Canvas c) {
		
		float rowCount = 8;
		float rowHeight = mRect.height() / rowCount;
		float step = mRangeY[LN] / rowCount;
		float startVal = mMax[LN].y;
		Coord cr = mCrMin;
		float y = mCrMax.y;//+10;
		this.mPntGraph.setStrokeWidth(1);
		
		for ( int row = 0; row < rowCount; row++) {
			if ( cr.y < y+rowHeight )
				break;
			
			this.mPntGraph.setColor(this.mClrGraphGridX);
			c.drawLine(mRect.left+40, y, mRect.right, y, this.mPntGraph);
			
			y+= rowHeight;
			float val = startVal;
			String sVal = String.format("%.2f",val);
			
			this.mPntLabel.setColor(this.mClrLabelY);
			this.mPntLabel.setTextAlign(Align.LEFT);
			c.drawText(sVal, mRect.left, y-rowHeight+5 ,this.mPntLabel);
			startVal-=step;
		}/*
		c.drawLine(mRect.left+40, cr.y, mRect.right, cr.y, mBgPaintGrid);
		String sVal = String.format("%.2f",mMinY);
		c.drawText(sVal, mRect.left, cr.y+5 ,mBgPaintY);
		*/
	}
	
	public Rect mClipRect = new Rect();
	public Path mPointPath = new Path();
	
	@Override
	public void onDraw(Canvas c) {
		
		//c.save();
		//c.translate(0,15);

		c.getClipBounds(mRect);
		mClipRect.set(mRect);
	
		drawBackground(c);
		mClipRect.left = 40;
		c.clipRect(mClipRect);
		
		Coord prev = null;
		
		Path mark = null;

		mGraphPath.rewind();
		int sY = 2;
		for ( Coord pt : mPts) {
			Coord cr = translateXY(SP,pt);
			if ( cr.x < 0 || cr.x > mClipRect.right )
				continue;
			//////
			if ( prev == null ) {
				prev = cr;
				mGraphPath.moveTo((float)cr.x, sY+(float)cr.y);
				continue;
			}
			mGraphPath.quadTo((float)prev.x, sY+(float)prev.y, (float)cr.x, sY+(float)cr.y);
			prev = cr;
		}
		
		// enclose the path
		Coord cr = translateXY(SP,mPts.get(0));
		mGraphPath.lineTo(prev.x, mCrMin.y-15);
		mGraphPath.lineTo(cr.x, mCrMin.y-15);
		mGraphPath.lineTo(cr.x, cr.y);
		this.mPntGraph.setColor(this.mClrGraphFill);
		mPntGraph.setStyle(Style.FILL_AND_STROKE);
		this.mPntGraph.setStrokeWidth(3);
		
		mPntGraph.setShadowLayer(5, 2, -3, Color.parseColor("#cc00ffff"));
		c.drawPath(mGraphPath, this.mPntGraph);
		mPntGraph.clearShadowLayer();

		
		// Simple Line Graph
		mGraphPath.rewind();
		mPointPath.rewind();
		prev = null;
		int itemIndex = 0;
		for ( Coord pt : mLst ) {
			itemIndex++;
			cr = translateXY(LN,pt);
			if ( cr.x < 0 || cr.x > mClipRect.right )
				continue;
			
			if ( prev == null ) {
				mGraphPath.moveTo(cr.x, cr.y);
				prev = cr;
			} else 
				mGraphPath.quadTo(prev.x, prev.y, cr.x, cr.y);

			mPointPath.addCircle(cr.x, cr.y, 4,Path.Direction.CW);
			
			prev = cr;
			this.drawLabel(c, cr.x,itemIndex-1);
		}
		
		
		Coord tr;
		// Target Value
		if ( mTargetValue != Float.MAX_VALUE ) {
			tr = translateXY(LN,1,mTargetValue);
			this.mPntLimits.setColor(this.mClrLimitsTargetBg);
			c.drawLine(mRect.left+40, tr.y, mRect.right, tr.y, this.mPntLimits);
			this.mPntLimits.setColor(this.mClrLimitsTargetText);
			mRcIcon.set(mRect.left+40, tr.y-16, mRect.left+56, tr.y);
			c.drawBitmap(this.mBmpTarget,null,mRcIcon, this.mPntLimits);
			c.drawText(String.format("%.2f",mTargetValue), mRcIcon.right+2, tr.y-2, this.mPntLimits);
		}
		
		// Start Value
		tr = translateXY(LN,1,mStartValue);
		this.mPntLimits.setColor(this.mClrLimitsStartBg);
		c.drawLine(mRect.left+40, tr.y, mRect.right, tr.y, this.mPntLimits);
		this.mPntLimits.setColor(this.mClrLimitsStartText);
		mRcIcon.set(mRect.left+40, tr.y-16, mRect.left+56, tr.y);
		c.drawBitmap(this.mBmpStart,null,mRcIcon, this.mPntLimits);
		c.drawText(String.format("%.2f",mStartValue), mRcIcon.right+2, tr.y-2, this.mPntLimits);
		

		//Line
		mPntGraph.setStyle(Style.STROKE);
		mPntGraph.setColor(this.mClrGraphLine);
		mPntGraph.setShadowLayer(4, 5, 5, Color.BLUE);
		c.drawPath(mGraphPath, this.mPntGraph);
		
		// Circle
		mPntGraph.setStyle(Style.FILL);
		this.mPntGraph.setColor(this.mClrGraphPoint);
		c.drawPath(mPointPath, mPntGraph);
		mPntGraph.clearShadowLayer();
		
	}
	
	public void repaint() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	public float mTouchX = 0;
	public float mMoveX = 0;
	public float mOffsetX = 0;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {		
		float x = event.getX();
		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			this.mTouchX = x;
			this.mMoveX  = x;
	} else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
		boolean fLeft = this.mMoveX > x;
		if ( fLeft ) {
			if ( getDrawWidth() + mOffsetX > mRect.width() - 40)
			mOffsetX-=this.mXLabelWidth/2;
		} else {
			if ( mOffsetX <= 0 )
				mOffsetX+=this.mXLabelWidth/2;
		}
		//NetLog.v("Ofs %f, %d,%d",mOffsetX,getDrawWidth(),mRect.width());
		this.repaint();
		this.mMoveX = x;
	} 
		return true;
	}
	
}
