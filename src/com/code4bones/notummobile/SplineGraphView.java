package com.code4bones.notummobile;


import java.util.ArrayList;
import java.util.List;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.content.res.Configuration;
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
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;

public class SplineGraphView extends View implements View.OnTouchListener {

	interface SplineGraphAdapter {
		public String getXLabel(int item,Object data);
		public ArrayList<Coord> getItems();
	}

	public Context mContext = null;
	
	public SplineGraphView(Context context) {
		super(context);
		mContext = context;
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
		dataChanged();
		super.onSizeChanged(w, h, oldw, oldh);
    }	
	
	public void dataChanged() {
		this.setPoints(mAdapter.getItems());
		mOffsetX = 0;
		getMinMax(SP,mPts);
		getMinMax(LN,mLst);
	}
	
	// X-Y Axis
	public int mClrLabelY = 0;
	public int mClrLabelBgY = 0;
	public int mClrLabelX = 0;
	public int mClrLabelBgX = 0;
	public int mClrTappedValueBg = 0;
	public int mClrTappedValueText = 0;
	
	public Paint mPntLabel = PaintEx.create(true);
	public Paint mPntTappedValue = PaintEx.create(true);
	
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
		this.mClrTappedValueText = Color.parseColor("#FF0000");
		this.mClrTappedValueBg = Color.parseColor("#ffe600");
		
		this.mClrLimitsStartBg = Color.parseColor("#eebb0000");
		this.mClrLimitsStartText = Color.parseColor("#ffffff");
		this.mClrLimitsTargetBg = Color.parseColor("#eebb0000");
		this.mClrLimitsTargetText = Color.parseColor("#ffffff");
	
	
		this.mPntTappedValue.setTypeface(Typeface.DEFAULT_BOLD);
		this.mPntTappedValue.setTextSize(25);
		this.mPntTappedValue.setStrokeWidth(2);
		this.mPntLabel.setTypeface(Typeface.DEFAULT_BOLD);
	//	this.mPntLabel.setShadowLayer(6, 4, 4, Color.WHITE);
		this.mPntLimits.setTypeface(Typeface.DEFAULT_BOLD);
		this.mPntLimits.setStrokeWidth(3);
	//		this.mPntLimits.setShadowLayer(6, 4, 4, Color.WHITE);
	}
	

	public void setAdapter(SplineGraphAdapter a) {
		this.mAdapter = a;
	}
	
	public void setPoints(ArrayList<Coord> lst) {
		mLst = lst;
		//NetLog.v("SET POINTS %d", mLst.size());
		if ( mLst.size() >= 4) {
			BSpline bs = new BSpline(lst);
			mPts = bs.getInterpolated();
		} else
			mPts = null;
	}
	
	public void setLimitsValue(float start,float target) {
		mStartValue = start;
		mTargetValue = target;
	}
	
	
	public void getMinMax(int id,List<Coord> lst) {
		
		if ( lst == null )
			return;
		
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
		
		
		if ( mTargetValue != Float.MAX_VALUE  ) {
			if ( mTargetValue <= mMin[id].y )
				mMin[id].y = mTargetValue;
			else if ( mTargetValue >= mMax[id].y )
				mMax[id].y = mTargetValue;
		}

		if ( mStartValue <= mMin[id].y ) {
			mMin[id].y = mStartValue;
		} else if ( mStartValue > mMax[id].y) {
			mMax[id].y = mStartValue;
		} 
	
		
		mMin[id].y -=  (1+mMin[id].y * 0.02f);
		mMax[id].y +=  (1+mMax[id].y * 0.02f);
		
		mMin[id].y = Math.round(mMin[id].y-0.5f)+0.5f;
		mMax[id].y = Math.round(mMax[id].y+0.5f)-0.5f;
		
		mRangeY[id] = mMax[id].y - mMin[id].y;
		mRangeX[id] = mMax[id].x - mMin[id].x;

		
		mCrMin = translateXY(id,new Coord(mMin[id].x,mMin[id].y));
		mCrMax = translateXY(id,new Coord(mMax[id].x,mMax[id].y));
	//	NetLog.v("MinXY(%f,%f) -> MaxXy(%f,%f) [%f,%f]",mMin[id].x,mMin[id].y,mMax[id].x,mMax[id].y,mRangeX[id],mRangeY[id]);
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

	//public int mXLabelWidth = 40;
	
	public int getLabelWidth() {
		if ( mLst.size() * 40  < mRect.width() )
			return mRect.width() / (mLst.size()>0?mLst.size():1);
		return 40;
	}
	
	public int getDrawWidth() {
		
		return mLst.size() * getLabelWidth();
	}
	
	public void moveToPoint(Coord lc) {
		if ( lc == null && mLst.size() >= 1)
			lc = mLst.get(mLst.size()-1);
		else
			return;
		Coord c = translateXY(LN,lc);
		mOffsetX = -c.x;
		mOffsetX += mRect.width()-10;
	}
	
	public void drawLabel(Canvas c,float x,int idx,Object data) {
		if ( idx > mLst.size()-1 )
			return;
		RectF rc = new RectF(x,mRect.bottom-13,x+getLabelWidth(),mRect.bottom);

		this.mPntLabel.setColor(this.mClrLabelBgX);
		rc.left -= 15;
		rc.right -=4;
		this.mPntLabel.setColor(this.mClrLabelX);
		String str = this.mAdapter.getXLabel(idx,data);
		c.drawText(str,rc.left,rc.bottom-2,this.mPntLabel);
	}
	
	
	final public int LN = 0;
	final public int SP = 1;
	
	public void drawBackground(Canvas c) {
		
		boolean isLandscape = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		
		float rowCount = isLandscape?5:10;
		float step = Math.abs((Math.round((mRangeY[LN] / rowCount))-0.5f));
		if ( step == 0 )
			step = 0.5f;
		this.mPntGraph.setStrokeWidth(1);
		this.mPntGraph.setColor(this.mClrGraphGridX);
		this.mPntLabel.setColor(this.mClrLabelY);
		this.mPntLabel.setTextAlign(Align.LEFT);
		
		float y = mMax[LN].y;
		while ( y > mMin[LN].y ) {
			Coord  cr = translateXY(LN,1,y);
		
			c.drawLine(mRect.left+40, cr.y, mRect.right, cr.y, this.mPntGraph);
			c.drawText(String.format("%.1f",y), mRect.left, cr.y ,this.mPntLabel);
			y -= step;
		}
	}
	
	public Rect mClipRect = new Rect();
	public Path mPointPath = new Path();
	public RectF mRcTapped = new RectF();
	public Rect mTmpRect = new Rect();
	
	@Override
	public void onDraw(Canvas c) {
		
		c.getClipBounds(mRect);
		mClipRect.set(mRect);
	
		c.save();

		mClipRect.left = 40;
		c.clipRect(mClipRect);
		
		Coord prev = null;
		Coord cr = null;
		if ( mPts != null ) {
			mGraphPath.rewind();
			int sY = 2;
			for ( Coord pt : mPts) {
				cr = translateXY(SP,pt);
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
			cr = translateXY(SP,mPts.get(0));
			mGraphPath.lineTo(prev.x, mCrMin.y-15);
			mGraphPath.lineTo(cr.x, mCrMin.y-15);
			mGraphPath.lineTo(cr.x, cr.y);
			this.mPntGraph.setColor(this.mClrGraphFill);
			mPntGraph.setStyle(Style.FILL_AND_STROKE);
			this.mPntGraph.setStrokeWidth(3);
			
			mPntGraph.setShadowLayer(5, 2, -3, Color.parseColor("#cc00ffff"));
			c.drawPath(mGraphPath, this.mPntGraph);
			mPntGraph.clearShadowLayer();
		} // mPts != null
		
		c.restore();

		drawBackground(c);
		c.clipRect(mClipRect);

		
		// Simple Line Graph
		mGraphPath.rewind();
		mPointPath.rewind();
		prev = null;
		int itemIndex = 0;
		for ( Coord pt : mLst ) {
			itemIndex++;
			cr = translateXY(LN,pt);
			
			if ( cr.x < 0 || (prev!=null && prev.x > mClipRect.right) )
				continue;
			
			if ( prev == null ) {
				mGraphPath.moveTo(cr.x, cr.y);
				prev = cr;
			} else 
				mGraphPath.quadTo(prev.x, prev.y, cr.x, cr.y);

			mPointPath.addCircle(cr.x, cr.y, 4,Path.Direction.CW);
			
			prev = cr;
			this.drawLabel(c, cr.x,itemIndex-1,pt.mData);
		}
		
		// Target Value
		drawLimits(c,mTargetValue,mClrLimitsTargetText,mClrLimitsTargetBg,mBmpTarget);
		drawLimits(c,mStartValue,mClrLimitsStartText,mClrLimitsStartBg,mBmpStart);
		
		//Line
		mPntGraph.setStrokeWidth(3);
		mPntGraph.setStyle(Style.STROKE);
		mPntGraph.setColor(this.mClrGraphLine);
		mPntGraph.setShadowLayer(4, 5, 5, Color.BLUE);
		c.drawPath(mGraphPath, this.mPntGraph);
		

		// Tapped marker
		if ( mTouchPoint != null ) {
			cr = translateXY(LN,mTouchPoint);
			String str = String.format("%.2f",mTouchPoint.y);
			this.mPntTappedValue.setColor(this.mClrTappedValueBg);
			mPntTappedValue.getTextBounds(str, 0, str.length(), mTmpRect);
			mRcTapped.set(mClipRect.left+2, cr.y-4-mTmpRect.height()/2, mClipRect.left+mTmpRect.width()+4, cr.y+2+mTmpRect.height()/2);
			mPntTappedValue.setShadowLayer(4, 4, 4, Color.BLUE);
			c.drawRect(mRcTapped, this.mPntTappedValue);
			c.drawLine(mRcTapped.right, cr.y, cr.x, cr.y, mPntTappedValue);
			c.drawLine(cr.x, cr.y, cr.x, mCrMin.y-15, mPntTappedValue);
			
			mPntTappedValue.setColor(this.mClrTappedValueText);
			mPntTappedValue.clearShadowLayer();
			c.drawText(str,mRcTapped.left,mRcTapped.centerY()+8,mPntTappedValue);
		}

		// Circle
		mPntGraph.setStyle(Style.FILL);
		this.mPntGraph.setColor(this.mClrGraphPoint);
		c.drawPath(mPointPath, mPntGraph);
		mPntGraph.clearShadowLayer();
		
		//MinMax
		//drawLimits(c,mMin[LN].y,mClrLimitsTargetText,Color.GREEN,null);
		//drawLimits(c,mMax[LN].y,mClrLimitsStartText,Color.YELLOW,null);
		
	}
	
	public void drawLimits(Canvas c,float value,int textColor,int bgColor,Bitmap img) {
		if ( value == Float.MAX_VALUE )
			return;
		Coord tr = translateXY(LN,1,value);
		this.mPntLimits.setColor(bgColor);
		this.mPntLimits.setShadowLayer(5, 4, 6, Color.BLUE);
		c.drawLine(mRect.left+40, tr.y, mRect.right, tr.y, this.mPntLimits);
		this.mPntLimits.clearShadowLayer();
		this.mPntLimits.setColor(textColor);
		mRcIcon.set(mRect.left+40, tr.y-16, mRect.left+56, tr.y);
		c.drawText(String.format("%.2f",value), mRcIcon.right+2, tr.y-4, this.mPntLimits);
		if ( img != null )
			c.drawBitmap(img,null,mRcIcon, this.mPntLimits);
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
	public float mOffsetX = 20;
	
	public Coord itemAtPos(float x,float y) {
		for ( Coord c : mLst ) {
			Coord pt = translateXY(LN,c);
			if ( pt.x-10 <= x && x <= pt.x+10 )
				return c;
		}
		return null;
	}
	
	public Coord mTouchPoint = null;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {		
		float x = event.getX();
		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			this.mTouchX = x;
			this.mMoveX  = x;
			mTouchPoint = itemAtPos(x,event.getY());
			if ( mTouchPoint != null )
				repaint();
	} else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
		boolean fLeft = this.mMoveX > x;
		if ( fLeft ) {
			if ( getDrawWidth() + mOffsetX > mRect.width())
			mOffsetX-= 20;
		} else {
			if ( mOffsetX <= 20 )
				mOffsetX+= 20;
		}
		this.repaint();
		this.mMoveX = x;
	} else if ( event.getAction() == MotionEvent.ACTION_UP ) {
		if ( mTouchPoint != null ) {
			mTouchPoint = null;
			repaint();
		}
	}
		return true;
	}
	
}
