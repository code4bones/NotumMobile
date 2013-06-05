package com.code4bones.notummobile;

import java.util.ArrayList;
import java.util.Timer;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



public class RibbonView extends View implements View.OnTouchListener {

	class Icon extends Object {
		
		public int mWidth = 32;
		public int mHeight = 32;
		public Bitmap mImage = null;
		public String mText = null;
		//public RectF mRect = new RectF();
		public boolean mSelected = false;
		public RectF mFullRect = new RectF();
		public RectF mLabelRect = null;//new RectF();
		public boolean mHasBorder = true;
		public boolean mHasIcon = true;
		public boolean mHasLabel = true;
		public RectF mImageRect = new RectF();
		public Object mObject = null;
		
		private final static int TEXT  = 0; 
		private final static int TEXT_SELECTED = 1;
		private final static int LABEL = 2;
		private final static int LABEL_SELECTED = 3;
		private final static int ICON  = 4;
		private final static int ICON_SELECTED  = 5;
		
		public RectF[] mRects  = new RectF[6];
		public Paint[] mPaints = new Paint[6];
		
		private RectF getRect(int what) {
			return mRects[what];
		}
		
		private void setRect(int what,RectF rc) {
			//if ( mRects[what] == null )
				mRects[what] = new RectF(rc);
			//else
			//	mRects[what] = rc;
		}
		
		private Paint getPaint(int what) {
			return mPaints[mSelected?what+1:what];
		}
		
		private void setPaint(int what,Paint paint) {
			mPaints[what] = new Paint(paint);
		}
		
		private void initPaints() {
			// Icon
			Paint p = new Paint();
			p.setARGB(255, 255, 255, 255);
			//p.setShadowLayer(3, 3, 3, Color.BLACK);
			p.setStyle(Style.STROKE);
			p.setStrokeWidth(2);
			setPaint(Icon.ICON,p);
			p.reset();
			// Icon Selected
			p.setARGB(255, 255, 255, 255);
			p.setShadowLayer(7, 3, 3, Color.BLACK);
			p.setStyle(Style.STROKE);
			p.setStrokeWidth(2);
			setPaint(Icon.ICON_SELECTED,p);
			p.reset();
			
			// Text ( Normal )
			p.setColor(Color.parseColor("#ff4d54ff"));//setARGB(220, 0, 0, 255);
			p.setTextAlign(Align.LEFT);
			p.setAntiAlias(true);
			p.setShadowLayer(1, 2, 2, Color.parseColor("#ffffffff"));

			//p.setTextSize(10);
			Typeface tf = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
			p.setTypeface(tf);
			setPaint(Icon.TEXT,p);
			p.reset();
			// Text ( Selected )
			tf = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
			p.setColor(Color.parseColor("#ffffea3d")); //setARGB(255, 255, 255, 255);
			p.setShadowLayer(1, 2, 2, Color.parseColor("#cc000000"));
			p.setAntiAlias(true);
			p.setTextSize(10);
			p.setTypeface(tf);
			setPaint(Icon.TEXT_SELECTED,p);
			p.reset();
			
			// Label Back ( Normal )
			p.setARGB(200, 255, 255, 255);
			//p.setAlpha(25);
			//p.setShadowLayer(5, 1, 1, Color.CYAN);
			p.setStyle(Style.FILL_AND_STROKE);
			p.setStrokeWidth(2);
			setPaint(Icon.LABEL,p);
			p.reset();
			// Label Back ( Selected )
			p.setARGB(20, 255, 255, 255);
			//p.setAlpha(25);
			//p.setShadowLayer(5, 4, 4, Color.YELLOW);
			p.setStyle(Style.FILL_AND_STROKE);
			p.setStrokeWidth(2);
			setPaint(Icon.LABEL_SELECTED,p);
		}
		
		private boolean mScaled = false;
		
		public Icon(Bitmap image,String text) {
			mWidth  = image.getWidth();
			mHeight = image.getHeight();
			mImage = image;
			mText   = text;
			mScaled = false;
			initPaints();
		}
		
		public void initRect(RectF rc,int parentHeight) {
			//TODO:
			mWidth = (int)rc.width();
			mHeight = (int)rc.height();
			setRect(Icon.ICON,new RectF(2,2,mWidth+2,mHeight+2));
			if ( !mScaled ) {
				mImage = Bitmap.createScaledBitmap(mImage, mWidth, mWidth, true);
				mScaled = true;
			}
			this.mViewHeight = parentHeight - (int)rc.height();
			this.mViewHeight -= 5;
		}
		
		public void onDraw(Canvas c,Paint paint) {
			
			RectF rc = getRect(Icon.ICON);
			//NetLog.v("Drawing RC %s",rc);
			this.mFullRect = new RectF(rc.left,rc.top,rc.left+mWidth+2,rc.top+mHeight+this.mViewHeight);
			
			if ( mHasBorder || mHasIcon ) {
				BadgeDrawer b = new BadgeDrawer(rc, c,
						this.mSelected?new String[]{"#fffc1600","#ff9cf9ff","#cc0000cc"}:new String[]{"#ffffffff","#ff0000ff","#cc0000cc"});//"#ffc80000","#ffdc2d1a","#ffff2d1a"});
			    b.setRadius(12);
				if ( mHasIcon ) {
					if ( mSelected )
						b.drawOutter();
					c.drawBitmap(mImage, rc.left, rc.top, paint);
					if ( mHasBorder ) {
						b.drawBorder();
						//b.addBubble(2,50);
					}
				} else {
					
				}
			} 
			
			if ( mHasLabel ) {
				drawLabelText(c);
			}
		}
		
		
		public void drawLabelText(Canvas c) {
			float left = 0;
			//Rect  b = new Rect();
			float dy = 0;
			this.adjustTextSize();
			this.adjustTextScale();
			Paint p = getPaint(Icon.TEXT);
			float fs = p.getFontSpacing();
			RectF rc = getRect(Icon.ICON);
			float top = rc.top+mHeight+p.getStrokeWidth();
			//RectF rcl = new RectF(rc.left,top,rc.left + rc.width(),top+this.mViewHeight/2);
			/*
			BadgeDrawer b = new BadgeDrawer(rcl, c,
					mSelected?new String[]{"#ff57f9ff","#ffff630f","#ff8530"}:new String[]{"#ff57f9ff","#ff9cf9ff","#cc0000cc"});
			b.inset(-5, -3);
			b.drawOutter();
			b.drawBorder();
			b.addBubble(4,30);
			*/
			c.drawText(mText, rc.left, rc.top+mHeight+p.getStrokeWidth()+(fs/2)+7, p);
		}
		
		
		
		public void offset(float dx) {
			mRects[Icon.ICON].offset(dx, 0);
		}
		
		public String toString() {
			return String.format("Icon(%s [%s]", mText,getRect(Icon.ICON));
		}
		
		int mViewHeight = 40;
		int mTextBaseline = 0;
		
		void adjustTextSize() {
		    Paint mTextPaint = getPaint(Icon.TEXT); 
			mTextPaint.setTextSize(100);
		    mTextPaint.setTextScaleX(1.0f);
		    Rect bounds = new Rect();
		    // ask the paint for the bounding rect if it were to draw this
		    // text
		    mTextPaint.getTextBounds(mText, 0, mText.length(), bounds);
		 
		    // get the height that would have been produced
		    int h = bounds.bottom - bounds.top;
		 
		    // make the text text up 70% of the height
		    float target = (float)mViewHeight*.7f;
		 
		    // figure out what textSize setting would create that height
		    // of text
		    float size  = ((target/h)*100f);
		 
		    // and set it into the paint
		    mTextPaint.setTextSize(size);
		}		
		void adjustTextScale() {
		    // do calculation with scale of 1.0 (no scale)
		    Paint mTextPaint = getPaint(Icon.TEXT); 
		    mTextPaint.setTextScaleX(1.0f);
		    Rect bounds = new Rect();
		    // ask the paint for the bounding rect if it were to draw this
		    // text.
		    mTextPaint.getTextBounds(mText, 0, mText.length(), bounds);
		 
		    // determine the width
		    int w = bounds.right - bounds.left;
		 
		    // calculate the baseline to use so that the
		    // entire text is visible including the descenders
		    int text_h = bounds.bottom-bounds.top;
		    mTextBaseline=bounds.bottom+((mViewHeight-text_h)/2);
		 
		    // determine how much to scale the width to fit the view
		    float xscale = ((float) (mWidth-getPaddingLeft()-getPaddingRight())) / w;
		 
		    // set the scale for the text paint
		    mTextPaint.setTextScaleX(xscale);
		}		
		
	}; // Icon
	
	public RibbonView(Context context) {
		super(context);
		this.setOnTouchListener(this);
	}
	
	public RibbonView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public RibbonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public Handler mHandler = new Handler();
	public Rect mRectF = new Rect();
	public ArrayList<Icon> mItems = new ArrayList<Icon>();
	public Paint mBgrPaint = new Paint();
	public Paint mIcoPaint = new Paint();
	public int mFixedWidth = 0;
	public int mFixedHeight = 0;
	public boolean mNeedAdjust = true;
	
	
	
	public void onDraw(Canvas c) {
		c.getClipBounds(mRectF);
		if ( mNeedAdjust ) {
			adjust();
		}
		makeVisible();
		c.save();
		c.translate(this.mOffsetX, 0);
		for ( Icon ico : mItems ) {
			ico.onDraw(c, mIcoPaint);
		}
		c.restore();
	}
	
	private void drawBackground(Canvas c) {
		mBgrPaint.setARGB(170, 255, 255, 255);
		c.drawRect(mRectF, mBgrPaint);
	}
	
	private float mLeftX = 0;
	private float mLength = 0;
	
	public void setSize(int w,int space,int rad) {
		mFixedWidth = w;
		mItemSpace = space;
		mItemWidth = mFixedWidth;
		mRadius = rad;
	}
	
	public int mParentWidth = 0;
	public int mParentHeight = 0;
	public boolean mAdjust = false;
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//TODO:
		mParentWidth = MeasureSpec.getSize(widthMeasureSpec);
		mParentHeight = MeasureSpec.getSize(heightMeasureSpec);
		adjust();
		this.setMeasuredDimension(mParentWidth, mParentHeight);
	}

	public void adjust() {
		mItemWidth = mParentHeight - mParentHeight/4;
		mLeftX = 0;
		RectF rcInit = new RectF(mLeftX,0,mLeftX+mItemWidth,mItemWidth);
		for ( Icon ico : mItems ) {
			ico.initRect(rcInit,mParentHeight);
			ico.offset(mLeftX);
			mLeftX += this.mItemWidth + this.mItemSpace;
		}
		mLength = ((mItems.size()+1) * this.mItemWidth) + ( mItems.size() * mItemSpace);
		mNeedAdjust = false;
	}
	
	public Icon addItem(Bitmap bmp,String sText) {
		//TODO:
		Icon ico = new Icon(this.getRoundedCornerBitmap(bmp),sText);
		mItems.add(ico);
		return ico;
	}
	
	public Icon addItem(int resId,String sText) {
		Bitmap img = null;
		Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), resId);
		if ( mFixedWidth > 0 ) {
			img = Bitmap.createScaledBitmap(bmp, mFixedWidth, mFixedWidth, true);
			mItemWidth = mFixedWidth;
		} else 
			img = bmp;
		return addItem(img,sText);
	}
	
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	        bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);
	 
	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	    final RectF rectF = new RectF(rect);
	    final float roundPx = mRadius;
	 
	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	 
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	 
	    return output;
	  }
	
   public void repaint() {
	    mHandler.post(new Runnable() {
	      public void run() {
	        invalidate();
	      }
	    });
    }
	
    public void selectByObject(Object obj) {
    	for ( Icon ico: mItems) {
    		if ( obj.equals(ico.mObject) ) {
    			selectItem(mItems.indexOf(ico));
    			break;
    		}
    	}
    }
   
   	public void selectItem(int idx) {
   		if ( mSelectedItem != null )
   			mSelectedItem.mSelected = false;
   		mSelectedItem = mItems.get(idx);
		mSelectedItem.mSelected = true;
   	}
  
   	public Icon findObject(Object obj) {
   	   	for ( Icon ico: mItems) {
    		if ( obj.equals(ico.mObject) ) 
    			return ico;
   	   	}
   	   	return null;
   	}
   	
   	public int itemIndex(Icon ico) {
   		return mItems.indexOf(ico);
   	}
   	
   	private Icon mVisible = null;
   	
   	public void setVisible(int idx) {
   		Icon ico = mItems.get(idx);
   		mVisible = ico;
   	}
   	
   	public void makeVisible() {
   		if ( mVisible == null )
   			return;
//		mOffsetX = 0;
//		RectF rc = new RectF(mVisible.getRect(Icon.ICON));
//		RectF sc = new RectF(mRectF);
   		mVisible = null;
   	}
   	
   	public Icon getSelectedItem() {
   		return mSelectedItem;
   	}
   	
   	private float mRadius = 12;
    private float mTouchX = 0;
    private float mMoveX = 0;
    private float mOffsetX = 0;
    private int mItemWidth = 64;
    public int mItemSpace  = 1;
    private float mReleaseX = 0;
    private float mStep = 32;
    private Handler mTouchHandler = null;
    private Icon  mSelectedItem = null;
    
    public void setIconHandler(Handler h) {
    	mTouchHandler = h;
    }
    
    public void scrollLeft() {
		if ( this.mOffsetX + (this.mLength-mRectF.width()) > (this.mItemWidth))
			 this.mOffsetX -= this.mStep;
    }
    
    public void scrollRight() {
		if ( this.mOffsetX < 0 )
			this.mOffsetX += this.mStep;
    }
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getX();
		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			this.mTouchX = x;
			this.mMoveX  = x;
		} else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
			boolean fLeft = this.mMoveX > x;
			if ( fLeft ) 
				scrollLeft();
			 else 
				scrollRight();
			this.repaint();
			this.mMoveX = x;
		} else if ( event.getAction() == MotionEvent.ACTION_UP ){
			this.mReleaseX = x;
			if ( this.mTouchX != this.mReleaseX )
				return true;

			Icon item = itemAtPos(event.getX(),event.getY());
			if ( item != null ) {
				if ( this.mTouchHandler != null ) {
					Message msg = this.mTouchHandler.obtainMessage(1, item);
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
   
    public Icon itemAtPos(float x,float y) {
    	for ( Icon item : mItems) {
    		float tx = item.mFullRect.left; 
    		tx += mOffsetX;
    		if ( tx < x && x < (tx + item.mFullRect.width()) &&
    			 y > item.mFullRect.top && y < item.mFullRect.top + item.mFullRect.height()) 
    			return item;
    	}
    	return null;
    }
	
    public void reset() {
    	mItems.clear();
    	mOffsetX = 0;
    	mLeftX = 0;
    	mNeedAdjust = true;
    }
    
}
