package com.code4bones.notummobile;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.view.MotionEvent;
import android.view.View;

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
		public Paint mPaint = new Paint();
		public Paint mDayPaint = new Paint();
		public Paint mDayBgPaint = new Paint();
		
		public Paint mTextChangePaint = new Paint();
		public Paint mTextValuePaint = new Paint();
		public Paint mBgChangePaint = new Paint();
		public Paint mBgValuePaint = new Paint();
		public Paint mFrameInPaint = new Paint();
		public Paint mFrameOutPaint = new Paint();
		public Paint mSelectionPaint = new Paint();
		public Object mObject;
		public String mChangeValue;
		public String mValue;
		public boolean mShowSign = true;
		public boolean mToday = false;
		public boolean mSelected = false;
		
		
		public NumberFormat mValueFormat = NumberFormat.getInstance();
		
		public Calendar mCal = null;
		
		public DayCell(RectF rc,Calendar cal) {
			mCal = (Calendar) cal.clone();
			
			mToday = mCal.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH); 
			mRect = new RectF(rc);
			mPaint.setColor(Color.BLUE);
			mPaint.setStyle(Style.FILL);
			mDayBgPaint.setAntiAlias(true);
			if ( mToday ) {
				mDayPaint.setColor(Color.RED);
				mDayBgPaint.setStyle(Style.FILL);
				mDayBgPaint.setColor(Color.YELLOW);
			} else {
				mDayBgPaint.setStyle(Style.STROKE);
				mDayPaint.setColor(Color.YELLOW);
			}
			mDayPaint.setAntiAlias(true);
			mDayPaint.setTypeface(Typeface.DEFAULT_BOLD);
			mTextValuePaint.setAntiAlias(true);
			mTextValuePaint.setTextAlign(Align.CENTER);
			mTextValuePaint.setTextSize(15);
			mTextValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
			mTextChangePaint.setAntiAlias(true);
			mTextChangePaint.setTextAlign(Align.CENTER);
			
			mFrameOutPaint.setStrokeWidth(1);
			mFrameOutPaint.setStyle(Style.STROKE);
			mFrameOutPaint.setColor(Color.YELLOW);

			mValueFormat.setMinimumFractionDigits(2);
			mValueFormat.setMaximumFractionDigits(2);
			
			mSelectionPaint.setColor(Color.RED);
			mSelectionPaint.setStyle(Style.STROKE);
			mSelectionPaint.setStrokeWidth(2);
		}
		
		public void Draw(Canvas c) {
			c.drawRect(mRect, mPaint);
			
			//Path f;
			
		
			//draw delta
			RectF rcDelta = new RectF(mRect.left+15,mRect.top+2,mRect.right-2,mRect.top+20);
			drawInfo(c,mChangeValue,rcDelta,mBgChangePaint,mTextChangePaint);
			
			// draw value
			//c.save();
			RectF rcValue = new RectF(mRect.left+2,mRect.top+21,mRect.right-2,mRect.bottom-2);
			//c.rotate(-35,rcValue.left,rcValue.top);
			drawInfo(c,mValue,rcValue,null,mTextValuePaint);
			//c.restore();

			if ( mToday ) {
				c.drawCircle(mRect.left+7, mRect.top+6, 8, mDayBgPaint);
			}
			String day = String.format("%d",mCal.get(Calendar.DAY_OF_MONTH));
			c.drawText(day, mRect.left, mRect.top+10, mDayPaint);
			
		}
		
		public void drawSelection(Canvas c) {
			c.drawRect(mRect, mSelectionPaint);
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
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
	private DayCell mSelectedCell = null;
	
	@Override
	public void onDraw(Canvas c) {
		c.getClipBounds(mRect);
		adjustCellSize(mRect);
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
			c.drawRect(x+mCellSpace, 0, x+mCellWidth, mMarginTop, mTitlePaint);
			c.drawText(name,x+mCellWidth/2-dy/2,((mMarginTop)/2+dy/2)-3, mDayPaint);
			x+= (mCellWidth+mCellSpace);
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}
		// Cells
		DayCell prevDay = null;
		for ( DayCell cell : mCells ) {
			if ( mStyler != null )
				mStyler.StyleTheDay(prevDay,cell);

			if ( mSelectedCell != null ) {
			if ( cell == mSelectedCell )
				cell.drawSelection(c);
			} else  if ( cell.mToday )
				cell.drawSelection(c);
				
			cell.Draw(c);

			
			prevDay = cell;
			//NetLog.v("Draw %s",cell);
		}
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
	
	public void setOnWeekDayClicked(WeekDayClicked handler) {
		this.mOnWeekDayClicked = handler;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
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
				mSelectedCell.mSelected = true;
				if ( this.mOnWeekDayClicked != null ) {
					this.mOnWeekDayClicked.onWeekDayClicked(this,mSelectedCell);
					this.repaint();
				}
				
			}
		}
		return true;
	}

}
