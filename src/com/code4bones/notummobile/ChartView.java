/**
 * 
 */
package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;



/**
 * @author code4bones
 *
 */
public class ChartView extends HorizontalScrollView {

	
	private class BarView extends View {

		public HistEntry entry = null;
		public double mHeight = 0;
		
		public BarView(Context context,double height) {
			super(context);
			mHeight = height;
			//NetLog.v("Height %f", mHeight);
		}
		
		public void update(double height) {
			this.mHeight = height;
			NetLog.v("Update to %f",mHeight);
			//this.refreshDrawableState();
			this.invalidate();
		}
		
		@Override
		public void onDraw(Canvas c) {
			Paint p = new Paint();
			p.setColor(Color.YELLOW);
			Rect r = new Rect();
			entry = (HistEntry)this.getTag();
			int Y = (int) (this.getHeight() - this.mHeight);
			r.set(0, Y, 50, (int)this.mHeight);
			NetLog.v("%f = %s",entry.value,r);
			//NetLog.v("DrawRect %f",mHeight);
			c.drawRect(r, p);
		}
		
	};
	
	HistEntry mData[] = null;
	double mMaxValue = 0;
	double mMinValue = 0;
	
	public ChartView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public ChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override 
	public void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		NetLog.v("changed %b,On LAY t = %d,b =  %d",changed,t,b);
		setData(b-t);
	}
	
	
	public void setData(int H) {
		boolean fUpdate = mData != null; 
		if ( !fUpdate ) {
			ParamEntry pe = new ParamEntry(null,1);
			mData = pe.toArray();
		}
		NetLog.v("Chart data len %d",mData.length);
		//LinearLayout lay = (LinearLayout)this.findViewById(R.id.chartLayout);
		
		//lay.removeAllViewsInLayout();
		int col[] = {Color.BLUE,Color.RED,Color.GREEN};
		int idx = 0;
		Calculate();
		//NetLog.v("Height %d",this.getBottom());
		for ( HistEntry e: mData ) {
			
			double val = e.value - this.mMinValue;
			double height = (val / this.mMaxValue) * H;
			if ( !fUpdate ) {
					BarView v = new BarView(this.getContext(),height);
					v.setTag(e);
					v.setBackgroundColor(col[idx++]);
					v.setMinimumWidth(50);
					
					
					//lay.addView(v);
				} else {
					//BarView v = (BarView)lay.findViewWithTag(e);
					//v.update(height);
				}
			idx %=2;
			}
	}
	
	public void Calculate() {
		this.mMaxValue = 0;
		this.mMinValue = 99999999999999.9;
		for ( HistEntry e : mData ) {
			if ( mMaxValue < e.value )
				mMaxValue = e.value;
			if ( mMinValue > e.value )
				mMinValue = e.value;
		}
		NetLog.v("MinMax(%f,%f)",mMinValue,mMaxValue);
	}
	/*
	@Override
	public void onDraw(Canvas c) {
		
	}*/
	
}
