package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;




public class BadgeView extends android.view.View {
	
	public ProfileEntry profile;
	
	public BadgeView(Context context,AttributeSet attrs) {
		super(context,attrs);
	}

	public BadgeView(Context context) {
		super(context);
	}
	
	public BadgeView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	}	
	
	public void setProfile(ProfileEntry entry) {
		this.profile = entry;
	}
	
	@Override
	public void onDraw(Canvas c) {
		if ( profile == null ) {
			return;
		}
		/*
		Path rect = new  Path();
        rect.addRect(0, 0,250, 150,Direction.CW);
        c.drawPath(rect, cpaint);		
	   */
		//if ( profile != null ) {
			NetLog.v("Drawing Profile %s",profile.profileName);
			Paint cpaint = new Paint();
			cpaint.setColor(Color.GREEN); 
			c.drawRoundRect(new RectF(0,0,300,150), 5, 5, cpaint);
			//Matrix m = new Matrix();
			//c.drawBitmap(profile.profileIcon, m, cpaint);
		//}
	}
	
}
