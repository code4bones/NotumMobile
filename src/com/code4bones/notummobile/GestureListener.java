package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

class GestureListener extends SimpleOnGestureListener  {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    public DirectionalGestureListener mListener;
    
    public void setListener(DirectionalGestureListener l) {
    	this.mListener = l;
    }
    
    public GestureListener(DirectionalGestureListener l) {
    	setListener(l);
    }
    
    @Override
    public boolean onDown(MotionEvent e) {
        NetLog.v("onDown");
    	return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = true;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        mListener.onSwipeRight();
                    } else {
                        mListener.onSwipeLeft();
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        mListener.onSwipeBottom();
                    } else {
                        mListener.onSwipeTop();
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }
}
