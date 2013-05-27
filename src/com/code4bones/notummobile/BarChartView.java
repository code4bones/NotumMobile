package com.code4bones.notummobile;

import java.text.NumberFormat;
import java.util.ArrayList;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.LinearGradient;


public class BarChartView extends View implements View.OnTouchListener {

	public static final int kBAR_SELECTED = 1;
	
	
	public class ChartItem {
		
		private float mValue = 0;
		private float mLoginValue = 0;
		//private Rect   mRect  = new Rect();
		private RectF  mRectf = new RectF();
		private boolean mLast = false;
		private boolean mSelected = false;
		public  Object  obj;
		
		public ChartItem(float dblVal) {
			this.mValue = dblVal;
		}
		
		public void drawItem(Canvas c) {
			Paint p = new Paint();
			p.setColor(mLast?Color.RED:Color.GREEN);
			
			
			Paint ps = new Paint();
			if ( this.mSelected == false ) {
				Shader shader = new LinearGradient(mRectf.left,0, mRectf.left + mRectf.width()/2,0, 
						mLast?Color.RED:Color.BLUE, Color.WHITE, TileMode.MIRROR); 
				ps.setShader(shader); 			
			} else {
				Shader shader = new LinearGradient(mRectf.left,0, mRectf.left + mRectf.width()/2,0, 
						mLast?Color.RED:Color.BLUE, Color.YELLOW, TileMode.MIRROR); 
				ps.setShader(shader); 			
			}
			c.drawRect(mRectf,ps);
			
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			String msg = nf.format(this.mValue);
			
			p.setAntiAlias(true);
			float x = mRectf.centerX()+5;
			float y = mRectf.top;
			if ( y < 15 ) {
				y = mRectf.centerY();
				p.setColor(Color.BLACK);
			} else {
				p.setColor(Color.YELLOW);
			}
			c.save();
			c.rotate(-90,x,y);
			c.drawText(msg, x, y, p);
			c.restore();
		}
		
		public String toString() {
			return String.format("ChartItem[Value:%f,Logic:%f] [%s]", mValue,mLoginValue,mRectf);
		}
		
	};
	
	private float mItemWidth = 25;
	private float mItemSpace = 1;
	
	private ArrayList<ChartItem> mItems = new ArrayList<ChartItem>();
	private Rect mRect = new Rect();
	private Handler mHandler = new Handler();
	private float mMaxValue  = 0;
	private float mMinValue  = Float.MAX_VALUE;
	private float mRangeValue = 0;
	private float mMoveX    = 0;
	private float mTouchX   = 0;
	private float mReleaseX = 0;
	private ChartItem mLastItem = null;
	private float mOffsetX = 0;
	private Handler mTouchHandler = null;
	private ChartItem mSelectedItem = null;
	private NumberFormat mFormat = NumberFormat.getInstance();
	
	
	public BarChartView(Context context) {
		super(context);
		init();
	}	
	
	public BarChartView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}	
	
	public BarChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void setTouchHandler(Handler handler) {
		this.mTouchHandler = handler;
	}
	
	public void SelectItem(ChartItem item) {
		if ( item == null )
			item = this.mLastItem;
		
		int page = (int) (mRect.width() / (this.mItemWidth + this.mItemSpace));
		page--;
		int idx = mItems.indexOf(item);
		NetLog.v("IDX %d, Page = %d",idx,page);
		if ( idx < page || page < 0) {
			mOffsetX = 0;
			this.repaint();
			return;
		}
		mOffsetX = 0;
		mOffsetX -= ((idx - page) * (this.mItemWidth+this.mItemSpace));
		this.repaint();
	}
	
	
	
	@Override
	public void onDraw(Canvas c) {
		
		c.getClipBounds(mRect);
		if ( mItems.size() == 0 )
			return;
		
		int nHeight = mRect.height()-10;
		float barX = 15;    
		c.save();
		c.translate(this.mOffsetX, 0);
		
		for ( ChartItem item : mItems ) {
			item.mLoginValue = item.mValue - this.mMinValue;
			float barHeight = ( item.mLoginValue / mRangeValue ) * nHeight;
			float barY      = nHeight - barHeight;
			item.mRectf.set(barX,barY+5,barX+this.mItemWidth,mRect.bottom);
			barX += this.mItemWidth + this.mItemSpace;
			item.drawItem(c);
		}
		c.restore();
		this.drawYLabels(c);
	}
	
	public void SetLast(float val) {
		this.mLastItem.mValue = val;
		Adjust();
		this.SelectItem(null);
	}
	
	public void ChangeLast(float val) {
		this.mLastItem.mValue += val;
		Adjust();
		this.SelectItem(null);
	}
	
	private int[] mLabelColors = new int[2];
	private final RectF mLabelRect = new RectF();
	private final Rect  mLabelBounds = new Rect();
	private final Paint mLabelPaint = new Paint();
	private final Paint mLabelBgrPaint = new Paint();
	private LinearGradient mLabelShader = null;
	
	private void init() {
		this.setOnTouchListener(this);

		mFormat.setMinimumFractionDigits(3);
		mFormat.setMaximumFractionDigits(3);
		
		mLabelColors[0] = Color.rgb(34, 181, 27);
		mLabelColors[1] = Color.BLACK;

	    mLabelPaint.setAntiAlias(true);
	    mLabelPaint.setTextSize(12);
	    mLabelPaint.setColor(Color.YELLOW);
		
	}
	
	private void drawYLabels(Canvas c) {
		
	    String strValue;
		float x = mRect.left+10;
		float y = mRect.bottom;
		
		/*
	    mLabelRect.set(2, mRect.top+5, x+3, mRect.bottom-5);
		if ( mLabelShader == null ) {
		  mLabelShader = new LinearGradient(0,0,0,mRect.bottom/2,mLabelColors[1], mLabelColors[0], TileMode.MIRROR); 
		  mLabelBgrPaint.setShader(mLabelShader); 
		  mLabelBgrPaint.setAntiAlias(true);
		}
	    c.drawRect(0, mRect.top, x, mRect.bottom, mLabelBgrPaint);
	    */
	    // MIN
		c.save();
	    strValue = mFormat.format(this.mMinValue);
		c.rotate(-90,x,y);
	    c.drawText(strValue, x, y, mLabelPaint);
	    c.restore();
	    
	    // MAX
	    c.save();
	    strValue = mFormat.format(this.mMaxValue);
	    mLabelPaint.getTextBounds(strValue, 0,strValue.length(), mLabelBounds);
	    y = mRect.top + mLabelBounds.width();
		c.rotate(-90,x,y);
	    c.drawText(strValue, x, y, mLabelPaint);
	    c.restore();
	    
	}
	
	
	private void Adjust() {
		mMaxValue = 0;
		mMinValue = Float.MAX_VALUE;
		for ( ChartItem item : mItems ) {
			item.mLast = false;
			mMaxValue = Math.max(mMaxValue, item.mValue);
			mMinValue = Math.min(mMinValue, item.mValue);
		}
		this.mLastItem = mItems.get(mItems.size()-1);
		this.mLastItem.mLast = true;
		mRangeValue = mMaxValue - mMinValue;
	}
	
	public ChartItem addItem(float dblValue) {
		ChartItem item = new ChartItem(dblValue); 
		mItems.add(item);
		Adjust();
		return item;
	}
	
	
	public void reset() {
		mItems.clear();
	}
	
    public void repaint() {
	    mHandler.post(new Runnable() {
	      public void run() {
	        invalidate();
	      }
	    });
    }

    public ChartItem itemAtPos(float x,float y) {
    	for ( ChartItem item : mItems) {
    		float tx = item.mRectf.left; 
    		tx += mOffsetX;
    		if ( tx < x && x < (tx + item.mRectf.width()) ) 
    			return item;
    	}
    	return null;
    }
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getX();
		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			this.mTouchX = x;
			this.mMoveX  = x;
		} else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
			//this.mMoveX = x;
			boolean fLeft = this.mMoveX > x;//this.mMoveX;
			if ( fLeft ) {
				int page = (int) (mRect.width() / (this.mItemWidth + this.mItemSpace));
				int count = (int) ((Math.abs(this.mOffsetX)) / this.mItems.size());
				if ( count < page )
				 this.mOffsetX -= (this.mItemWidth);
			} else {
				if ( this.mOffsetX < 0 )
					this.mOffsetX += (this.mItemWidth);
			}
			this.repaint();
			this.mMoveX = x;//this.mMoveX;
		} else if ( event.getAction() == MotionEvent.ACTION_UP ){
			this.mReleaseX = x;
			if ( this.mTouchX != this.mReleaseX )
				return true;
			ChartItem item = itemAtPos(event.getX(),event.getY());
			if ( item != null ) {
				if ( this.mTouchHandler != null ) {
					Message msg = this.mTouchHandler.obtainMessage(BarChartView.kBAR_SELECTED, item);
					msg.sendToTarget();
				}
				if ( this.mSelectedItem != null ) {
					this.mSelectedItem.mSelected = false;
				}
				this.mSelectedItem = item;
				this.mSelectedItem.mSelected = true;
				this.repaint();
			}
		}
		return true;
	}
	
}
