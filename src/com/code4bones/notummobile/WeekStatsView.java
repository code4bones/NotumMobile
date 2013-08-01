package com.code4bones.notummobile;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

public class WeekStatsView extends View implements View.OnTouchListener {

	//XXX Styler
	public interface WeekDayStyler {
		public void StyleTheDay(DayCell prevDay,DayCell day);
	};
	
	public interface WeekDayClicked {
		public void onWeekDayClicked(WeekStatsView view,DayCell cell);
	}
	
	
	public class DayCell extends Object {
		
		public RectF mRect = null;
		public Paint mPaint = PaintEx.create(true);
		public Paint mDayPaint = PaintEx.create(true);
		public Paint mDayBgPaint = PaintEx.create(true);
		public Paint mMarkerPaint = PaintEx.create(true);
		public Paint mMarkerBgPaint = PaintEx.create(true);
		
		
		public Paint mTextChangePaint = PaintEx.create(false);
		public Paint mTextValuePaint = PaintEx.create(false);
		public Paint mBgChangePaint = PaintEx.create(true);
		public Paint mBgValuePaint = PaintEx.create(true);
		public Paint mFrameInPaint = PaintEx.create(true);
		public Paint mFrameOutPaint = PaintEx.create(false);
		public Paint mSelectionPaint = PaintEx.create(false);
		public Object mObject;
		public String mChangeValue;
		public String mValue;
		public boolean mShowSign = true;
		public boolean mToday = false;
		public boolean mFirstDay = false;
		public boolean mSelected = false;
		
		
		public NumberFormat mValueFormat = NumberFormat.getInstance();
		
		public Calendar mCal = null;
		
		public DayCell(RectF rc,Calendar cal) {
			mCal = (Calendar) cal.clone();
			mCal.set(Calendar.HOUR, 0);
			mCal.set(Calendar.MINUTE, 0);
			mCal.set(Calendar.SECOND, 0);
			
			mToday = mCal.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH); 
			mRect = new RectF(rc);
			mPaint.setColor(Color.BLUE);
			if ( mToday ) {
				mDayPaint.setColor(Color.RED);
				mDayBgPaint.setStyle(Style.FILL);
				mDayBgPaint.setColor(Color.YELLOW);
			} else {
				mDayBgPaint.setStyle(Style.STROKE);
				mDayPaint.setColor(Color.YELLOW);
			}
			
			mMarkerPaint.setColor(Color.YELLOW);
			mMarkerBgPaint.setColor(Color.GREEN);
			
			mDayPaint.setTypeface(Typeface.DEFAULT_BOLD);
			mTextValuePaint.setTextAlign(Align.CENTER);
			mTextValuePaint.setTextSize(15);
			mTextValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
			mTextChangePaint.setTextAlign(Align.CENTER);
			
			mFrameOutPaint.setStrokeWidth(1);
			mFrameOutPaint.setColor(Color.YELLOW);

			mValueFormat.setMinimumFractionDigits(2);
			mValueFormat.setMaximumFractionDigits(2);
			
			mSelectionPaint.setColor(Color.parseColor("#aaffff00"));
			mSelectionPaint.setStyle(Style.STROKE);
			mSelectionPaint.setStrokeWidth(3);
		}
		
		public void Draw(Canvas c,boolean fSelected) {
			RectF oldRect = new RectF(mRect);
			
			c.drawRect(mRect, mPaint);

			if ( fSelected )
				this.drawSelection(c);
			
			//draw delta
			RectF rcDelta = new RectF(mRect.left+15,mRect.top+2,mRect.right-2,mRect.top+20);
			mBgChangePaint.setShadowLayer(4, 3, 3, Color.BLACK);
			drawInfo(c,mChangeValue,rcDelta,mBgChangePaint,mTextChangePaint);
			mBgChangePaint.clearShadowLayer();
			// draw value
			RectF rcValue = new RectF(mRect.left+2,mRect.top+21,mRect.right-2,mRect.bottom-2);
			drawInfo(c,mValue,rcValue,null,mTextValuePaint);
			
			
			if ( mToday && !mFirstDay ) {
				mDayBgPaint.setShadowLayer(4, 3, 3, Color.BLACK);
				c.drawCircle(mRect.left+7, mRect.top+6, 10, mDayBgPaint);
				mDayBgPaint.clearShadowLayer();
			}
			
			if ( mFirstDay ) {
				mMarkerBgPaint.setShadowLayer(4, 3, 3, Color.BLACK);
				c.drawCircle(mRect.left+7, mRect.top+6, 10, this.mMarkerBgPaint);
				mMarkerBgPaint.clearShadowLayer();
			}
			
			String day = String.format("%d",mCal.get(Calendar.DAY_OF_MONTH));
			if ( !mToday ) {
				if ( mValue != null )
					mDayPaint.setColor(Color.YELLOW);
				else
					mDayPaint.setColor(Color.DKGRAY);
			}
			if ( mFirstDay )
				mDayPaint.setColor(Color.RED)
				;
			c.drawText(day, mRect.left+(day.length()==1?3:0), mRect.top+10, mDayPaint);
			mRect.set(oldRect);
		}
		
		public void drawSelection(Canvas c) {
			mSelectionPaint.setShadowLayer(3, 3, 3, Color.BLACK);
			c.drawRect(mRect, mSelectionPaint);
			mSelectionPaint.clearShadowLayer();
		}
		
		public void drawInfo(Canvas c,String str,RectF rcBounds,Paint bg,Paint fg) {

			if ( str == null )
				return;

			Rect  rc = new Rect();
			fg.getTextBounds(str, 0, str.length(), rc);
			
			if (bg != null ) {
				rc.right+=5;
				rcBounds.left = (rcBounds.right-(rc.width()));
				c.drawRect(rcBounds,bg);
				c.drawRect(rcBounds,mFrameOutPaint);
			}

			
			RectF rcText = new RectF(rcBounds);
			//rcText.left += rc.width();
			//rcText.inset(3, 3);
			float width = fg.measureText(str);
			float height = fg.getTextSize();
			c.drawText(str, 0, str.length(),rcText.centerX(),rcText.centerY()-2+height/2, fg);
		}
		
		public boolean compare(long millis) {
			return mCal.getTimeInMillis() / 1000 == millis;
		}
		
		public boolean before(Date dt) {
			dt.setHours(0);
			dt.setMinutes(0);
			dt.setSeconds(0);
			return mCal.getTimeInMillis() / 1000 <= dt.getTime() / 1000;
		}

		public boolean after(Date dt) {
			dt.setHours(0);
			dt.setMinutes(0);
			dt.setSeconds(0);
			return mCal.getTimeInMillis() / 1000 >= dt.getTime() / 1000;
		}
		
		public String toString() {
			return String.format("DayCell[date %s]: ",mCal.getTime().toLocaleString());
		}
	};

	public WeekStatsView(Context context,WeekDayStyler styler) {
		super(context);
		mStyler = styler;
		mDayPaint.setAntiAlias(true);
		mDayPaint.setTypeface(Typeface.DEFAULT_BOLD);
		this.setOnTouchListener(this);
	}	
	
	public WeekStatsView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}	
	
	public WeekStatsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mRect.set(0, 0, w, h);
//		this.adjustCellSize(mRect);
		super.onSizeChanged(w, h, oldw, oldh);
    }	
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mRect.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		adjustCellSize(mRect);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private WeekDayStyler mStyler = null;
	private WeekDayClicked mOnWeekDayClicked = null;
	final private Paint mDayPaint = new Paint();
	final private Paint mTitlePaint = new Paint();
	final private Handler mHandler = new Handler(); 
	private Rect mRect = new Rect();
	private int  mRows = 3;
	private int  mCols = 7;
	private float  mCellWidth = 0;
	private float  mCellHeight = 0;
	private float mCellSpace = 1;
	private float mRowSpace = 1;
	private float mMarginTop = 15;
	private float mMarginLeft = 0;
	private ArrayList<DayCell> mCells = null;
	public DayCell mSelectedCell = null;
	public boolean mModified = true;
	
	@Override
	public void onDraw(Canvas c) {
		c.getClipBounds(mRect);
		// Days draw
		mTitlePaint.setStyle(Style.FILL);
		float x = 0;
		Calendar cur = Calendar.getInstance();		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		for ( int day = 0; day < 7;day++ ) {
			boolean today = cur.get(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK); 
			if ( today )
				mTitlePaint.setColor(Color.YELLOW);
			else
				mTitlePaint.setColor(Color.GREEN);
			
			String name = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
			float dy = mDayPaint.getTextSize();// .getFontSpacing();
			mTitlePaint.setShadowLayer(3, -3, -3, Color.BLACK);
			c.drawRect(x+mCellSpace, 0, x+mCellWidth, mMarginTop, mTitlePaint);
			mTitlePaint.clearShadowLayer();
			c.drawText(name,x+mCellWidth/2-dy/2,((mMarginTop)/2+dy/2)-3, mDayPaint);
			x+= (mCellWidth+mCellSpace);
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}
		// Cells
		DayCell prevDay = null;
		for ( DayCell cell : mCells ) {
			
			if ( mStyler != null && mModified )
				mStyler.StyleTheDay(prevDay,cell);

			boolean fSelected = false;
			
			if ( mSelectedCell != null ) {
				if ( cell == mSelectedCell )
					fSelected = true;
			} else  
				if ( cell.mToday && mSelectedCell == null ) {
					//mSelectedCell = cell;
					this.selectCell(cell);
					fSelected = true;
				}

			cell.Draw(c,fSelected);
			
			prevDay = cell;
		}
		this.mModified = false;
	}	

	
	private void adjustCellSize(Rect bounds) {
		mCellWidth = ((bounds.width() - mMarginLeft - (mCellSpace*mCols))/ mCols) ;
		mCellHeight = ((bounds.height()- mMarginTop -(mRowSpace*mRows)) / mRows);
		if ( mCells == null ) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)-(7*(mRows-1)));
			mCells = new ArrayList<DayCell>();
			float x = (mCellSpace / 2)+mMarginLeft;
			float y = (mRowSpace / 2)+mMarginTop;
			RectF rc = new RectF(x,y,x+mCellWidth,y+mCellHeight);
			for ( int row = 0; row < mRows;row++) {
				for ( int col = 0; col < mCols; col++) {
					DayCell cell = new DayCell(rc,cal);
					cal.add(Calendar.DAY_OF_WEEK, 1);
					rc.offset(mCellWidth+mCellSpace, 0);
					mCells.add(cell);
				}// cols
				rc.offset(-((mCols*mCellWidth)+(mCols*mCellSpace)), mCellHeight+mRowSpace);
			} // rows
		} // mCell == null
	}
	
	public void repaint() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	//XXX
	public DayCell findDate(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND,0);
		long time = cal.getTimeInMillis() / 1000;
		for ( DayCell cell : mCells) {
			if ( cell.compare(time) )
				return cell;
		}
		return null;
	}
	
	public DayCell findItem(float x,float y) {
		for ( DayCell cell: mCells ) {
			if ( cell.mRect.contains(x, y)) {
				return cell;
			}
		}
		return null;
	}
	
	private float mTouchDownX = 0;
	private float mTouchUpX = 0;
	public GestureDetector mGestureDetector = null;
	
	public void setSwipeHandler(DirectionalGestureListener l) {
		mGestureDetector = new GestureDetector(new GestureListener(l));
	}
	
	public void setOnWeekDayClicked(WeekDayClicked handler) {
		this.mOnWeekDayClicked = handler;
	}
	
	public void selectCell(DayCell cell) {
		if ( cell == null ) {
			cell = this.findDate(new Date());
		}
		mSelectedCell = cell;
		mSelectedCell.mSelected = true;
		if ( this.mOnWeekDayClicked != null ) 
			this.mOnWeekDayClicked.onWeekDayClicked(this,mSelectedCell);
		//repaint();
	}
	
	public DayCell selectToDay() {
		for ( DayCell cell : mCells ) 
			if ( cell.mToday ) {
				selectCell(cell);
				return cell;
			}
		return null;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		boolean res = this.mGestureDetector.onTouchEvent(event);
		float x = event.getX();
		float y = event.getY();
		
		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			mTouchDownX = x;
		} else
		if ( event.getAction() == MotionEvent.ACTION_UP ) {
			mTouchUpX = x;
			if ( mTouchDownX == mTouchUpX ) {
				mSelectedCell = findItem(x,y);
				if ( mSelectedCell == null )
					return true;
				selectCell(mSelectedCell);
				repaint();
			}
		}
		return res;
	}

}
