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
		public void StyleTheDay(DayCell day);
	};
	
	public class DayCell extends Object {
		
		public RectF mRect = null;
		public Paint mPaint = new Paint();
		public Paint mDayPaint = new Paint();
		public Paint mTextChangePaint = new Paint();
		public Paint mTextValuePaint = new Paint();
		public Paint mBgChangePaint = new Paint();
		public Paint mBgValuePaint = new Paint();
		public Paint mFrameInPaint = new Paint();
		public Paint mFrameOutPaint = new Paint();
		
		public double mChangeValue;
		public double mValue;
		public boolean mShowSign = true;
		
		public NumberFormat mValueFormat = NumberFormat.getInstance();
		
		public Calendar mCal = null;
		
		public DayCell(RectF rc,Calendar cal) {
			mCal = (Calendar) cal.clone();
			mRect = new RectF(rc);
			mPaint.setColor(Color.BLUE);
			mPaint.setStyle(Style.FILL);
			mDayPaint.setColor(Color.YELLOW);
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
		}
		
		public void Draw(Canvas c) {
			c.drawRect(mRect, mPaint);
			String day = String.format("%d",mCal.get(Calendar.DAY_OF_MONTH));
			c.drawText(day, mRect.left, mRect.top+10, mDayPaint);
		
			//draw delta
			RectF rcDelta = new RectF(mRect.left+15,mRect.top+2,mRect.right-2,mRect.top+20);
			DrawInfo(c,(mChangeValue>0?"+":"")+mValueFormat.format(mChangeValue),rcDelta,mBgChangePaint,mTextChangePaint);
			
			// draw value
			RectF rcValue = new RectF(mRect.left+2,mRect.top+21,mRect.right-2,mRect.bottom-2);
			DrawInfo(c,mValueFormat.format(mValue),rcValue,null,mTextValuePaint);
			
		}
		
		public void DrawInfo(Canvas c,String str,RectF rcBounds,Paint bg,Paint fg) {

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
		//this.setOnTouchListener(this);
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
			if ( cur.get(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK))
				mTitlePaint.setColor(Color.YELLOW);
			else
				mTitlePaint.setColor(Color.GREEN);
			String name = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
			float dy = mDayPaint.getTextSize();// .getFontSpacing();
			c.drawRect(x+mCellSpace, 0, x+mCellWidth, mMarginTop, mTitlePaint);
			c.drawText(name,x+mCellWidth/2-dy/2,(mMarginTop)/2+dy/2, mDayPaint);
			x+= (mCellWidth+mCellSpace);
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}
		// Cells
		for ( DayCell cell : mCells ) {
			if ( mStyler != null )
				mStyler.StyleTheDay(cell);
			cell.Draw(c);
			NetLog.v("Draw %s",cell);
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
