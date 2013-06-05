package com.code4bones.notummobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;


public class TipsView extends View {

	public class Tip extends Object implements Comparable<Tip> {
		public RectF mRect =  new RectF();
		public String mText = new String();
		public int    mPriority = 0;
		public Paint  mTextPaint = new Paint();
		public Paint  mFramePaint = new Paint();
		public int    mFontSize = 20;
		
		public Tip(String sText,int nPriority) {
			localInit();
			mText 	  = sText;
			mPriority = nPriority;
			float width = mTextPaint.measureText(mText);
			float height = mTextPaint.getFontSpacing();
			mRect.set(2, 2, width+10, height+10);
		}
		
		public Tip(String sText) {
			this(sText,0);
		}
		
		public void setFontSize(int fontSize) {
			this.mFontSize = fontSize;
			mTextPaint.setTextSize(this.mFontSize);
			float width = mTextPaint.measureText(mText);
			float height = mTextPaint.getFontSpacing();
			mRect.set(2, 2, width+10, height+10);
		}
		
		public void localInit() {
			mTextPaint.setTextSize(this.mFontSize);
			mTextPaint.setAntiAlias(true);
			mTextPaint.setTypeface(Typeface.SERIF);
		}
		
		private String mOutterColor = "#cf0500";
		private String mInnerColor  = "#ff3338";
		private String mBorderColor = "#ffffff";
		private String mTextColor   = "#ffffff";
		private String mBubbleColor = "#90ffffff";
		
		private Rect textBounds = new Rect();
		private RectF rcDraw = new RectF();
		private boolean mHasShadow = false;
		private boolean mHasBubble = true;
		
		
		public void setTextColor(String clr) {
			mTextColor = clr;
		}
		
		public void setColors(String border,String outter,String inner) {
			mOutterColor = outter;
			mInnerColor = inner;
			mBorderColor = border;
		}
		
		public void setBadgeColors(String[] clrs) {
		   setColors(clrs[0],clrs[1],clrs[2]);	
		   setTextColor(clrs[3]);
		}
		
		public void onDraw(Canvas c) {
			rcDraw.set(mRect);
			mTextPaint.getTextBounds(mText, 0, mText.length(),textBounds);
			mTextPaint.setTextAlign(Align.CENTER);

			mFramePaint.setShadowLayer(0, 0, 0, Color.WHITE);
			mFramePaint.setAntiAlias(true);
			mFramePaint.setStyle(Style.FILL_AND_STROKE);
			mFramePaint.setColor(Color.parseColor(mOutterColor/*"#cf0500"*/));
			rcDraw.inset(1.3f, 1.3f);
			c.drawRoundRect(rcDraw, 12, 12, mFramePaint);

			mFramePaint.setShadowLayer(3, -1, -1, Color.WHITE);
			mFramePaint.setColor(Color.parseColor(mInnerColor/*"#ff3338"*/));
			rcDraw.inset(1.5f, 1.5f);
			c.drawRoundRect(rcDraw, 12, 12, mFramePaint);
			
			mFramePaint.setShadowLayer(0, 0, 0, Color.WHITE);
			mFramePaint.setStyle(Style.STROKE);
			mFramePaint.setColor(Color.parseColor(mBorderColor/*"#FFFFFF"*/));
			mFramePaint.setStrokeWidth(3);
			mFramePaint.setShadowLayer(3, 1, 1, Color.BLACK);
			c.drawRoundRect(mRect, 12, 12, mFramePaint);

			mFramePaint.setShadowLayer(0, 0, 0, Color.WHITE);
	
			if ( mHasBubble ) {
				mFramePaint.setStyle(Style.FILL_AND_STROKE);
				mFramePaint.setColor(Color.parseColor(mBubbleColor/*"#90FFFFFF"*/));
				rcDraw.top -= 3;
				rcDraw.bottom = rcDraw.top + rcDraw.height()/3;
				c.drawRoundRect(rcDraw, 12, 12, mFramePaint);
			}
			mTextPaint.setColor(Color.parseColor(mTextColor/*"#FFFFFF"*/));
			if ( mHasShadow )
				mTextPaint.setShadowLayer(1, 1, 0, Color.BLACK);
			c.drawText(mText, mRect.left + mRect.width()/2, (mRect.top+(mRect.height() - mTextPaint.ascent())/2)-3, mTextPaint);
		}

		@Override
		public int compareTo(Tip o) {
			return this.mPriority - o.mPriority;
		}
		
		public String toString() {
			return String.format("Tip {%d : %s} %s",mPriority, mText,mRect);
		}
		
		public void offset(float dx,float dy) {
			mRect.offsetTo(7, 0);
			mRect.offset(dx, dy);
	//		mBadge.setRect(mRect);
		}
		
		public float getWidth() {
			return mRect.width()+5;
		}
		
		public float getHeight() {
			return mRect.height()+6;
		}
		
		
	} // class Tip
	
	
	// class TipsView
	
	public TipsView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public TipsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public TipsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public void localInit() {
		
	}
	
	
	
	private Rect mDeadRect = null;
	private int  mDeadWidth = 0;
	private int  mDeadHeight = 0;
	private Tip  mLastTip  = null;
	private int  mFontSize = 25;
	private Rect mParentRect = new Rect();
	private ArrayList<Tip> mTips = new ArrayList<Tip>();
	
	public synchronized Tip addTip(String sText,int nPriority) {
		Tip tip = new Tip(sText,nPriority);
		tip.setFontSize(this.mFontSize);
		mTips.add(tip);
		return tip;
	}
	
	public synchronized void reset() {
		mTips.clear();
	}
	
	public void setFontSize(int fontSize) {
		this.mFontSize = fontSize;
	}
	
	public void setDeadRect(int x,int y,int w,int h) {
		mDeadWidth = w;
		mDeadHeight = h;
		mDeadRect = new Rect(x,y,x+mDeadWidth,y+mDeadHeight);
	}
	
	public void setImage(Bitmap img) {
		mImage = img;
		if ( mImage != null ) {
			setDeadRect(0,0,mImage.getWidth(),mImage.getHeight());
			if ( mHasImageFrame ) {
				mImageFramePaint.setAntiAlias(true);
				mImageFramePaint.setStrokeWidth(3);
				mImageFramePaint.setShadowLayer(2, 2,2, Color.parseColor("#000000"));
				mImageFramePaint.setStyle(Style.STROKE);
				mImageFramePaint.setColor(Color.parseColor("#CC97daff"));
			}
		}
	}
	
	public synchronized void Adjust() {
		Collections.sort(mTips);
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//TODO: onMeasure
		mParentRect.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		if ( mImage != null  ) {
			mDeadRect.bottom = mParentRect.height() - (mParentRect.height()/3);
			mDeadRect.right = mDeadRect.bottom;
			//mImage = Bitmap.createScaledBitmap(mImage, mDeadRect.width(), mDeadRect.height(), true);
		}
		this.setMeasuredDimension(mParentRect.width(), mParentRect.height());
	}
	
	private List<Tip> mDrawTips = null;
	private int mDrawHeight = 0;
	private int mDrawWidth = 0;
	private int mDrawY = 0;
	private int mDrawX = 0;
	private Bitmap mImage = null;
	private RectF mImageFrameRect = new RectF();
	private Paint mImageFramePaint = new Paint();
	
	
	public int getDrawableWidth() {
		return mParentRect.width() - 15;
	}
	
	public int getDrawableHeight() {
		return mParentRect.height() - 15;
	}
	
	Paint mImagePaint = new Paint();
	private boolean mHasImageFrame = false;

	
	
	@Override
	public void onDraw(Canvas c) {
		if ( mTips.isEmpty() )
			return;

		if ( mImage != null ) {
			mDeadRect.bottom = mParentRect.height() - (mParentRect.height()/3);
			mDeadRect.right = mDeadRect.bottom;
			c.drawBitmap(mImage, null, mDeadRect, mImagePaint);
			if ( mHasImageFrame ) {
				mImageFrameRect.set(mDeadRect);
				mImageFrameRect.inset(1, 1);
				c.drawRoundRect(mImageFrameRect, 12,12, mImageFramePaint);
			}
		}
		
		mDrawWidth = this.getDrawableWidth();
		mDrawHeight = this.getDrawableHeight();
		//int yy = mDrawHeight 
		mDrawX = 10;
		mDrawY = 5;
		boolean notDone = true;
		mDrawTips = new ArrayList<Tip>(mTips);
		while ( notDone ) {
			Tip tip = nextTip();
			if ( tip == null ) {
				mDrawWidth = this.getDrawableWidth();
				tip = nextTip();
				if ( tip == null )
					break;
				mDrawY += tip.getHeight();
				mDrawHeight -= tip.getHeight();
				mDrawX = 10;
			}
			if ( mDrawHeight <= tip.getHeight() ) {
				break;
			}
			while( mImage != null && mDeadRect.contains(mDrawX, mDrawY)) {
				mDrawX++;
				mDrawWidth--;
			}
			tip.offset(mDrawX, mDrawY);
			mDrawX += tip.getWidth();
			tip.onDraw(c);
		}
		
	}
	
	public Tip nextTip() {
		int idx = 0;
		Tip tip = null;
		for ( ; idx < mDrawTips.size(); idx++ ) {
			tip = mDrawTips.get(idx);
			if ( tip.getWidth() <= mDrawWidth ) {
				mDrawTips.remove(tip);
				mDrawWidth -= tip.getWidth();
				return tip;
			} 
		}
		return null;
	}
	
	public static String[] defaultBlue() {
		return new String[]{"#ffffff","#3f96ee","#82a6ee","#000000"};
	}
	
	
	public static String[] defaultGreen() {
		return new String[]{"#ffffff","#29c934","#44ee45","#000000"};
	}
	
	public static String[] defaultRed() {
		return new String[]{"#ffffff","#cf0500","#ff3338","#ffffff"};
	}
	
	public void setImageFrame(boolean val) {
		this.mHasImageFrame = val;
	}
	
   private Handler mHandler = new Handler();	
	
   public void repaint() {
	    mHandler.post(new Runnable() {
	      public void run() {
	        invalidate();
	      }
	    });
    }
	
}
