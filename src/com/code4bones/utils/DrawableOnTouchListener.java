package com.code4bones.utils;

import com.code4bones.notummobile.R;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public abstract class DrawableOnTouchListener implements View.OnTouchListener {
    Drawable drawable;
    int mDrawableNum;
    private int fuzz = 5;

    /**
     * @param keyword
     */
    public DrawableOnTouchListener(EditText view,int nDrawable) {
        super();
        this.mDrawableNum = nDrawable;
        final Drawable[] drawables = view.getCompoundDrawables();
        if (drawables != null ) {
        	this.drawable = drawables[nDrawable];
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
            final int x = (int) event.getRawX();
            final int y = (int) event.getY();
            final Rect bounds = drawable.getBounds();
            if (x >= (v.getRight() - bounds.width() - fuzz) && x <= (v.getRight() - v.getPaddingRight() + fuzz)
                    && y >= (v.getPaddingTop() - fuzz) && y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
                return onDrawableTouch(v,event);
            }
        }
        return false;
    }

    public abstract boolean onDrawableTouch(final View view,final MotionEvent event);

    
	public static void addDeleteButton(EditText edits[]) {
		
		for ( EditText et : edits ) {
			et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.delete, 0);
			
			DrawableOnTouchListener l = new DrawableOnTouchListener(et, 2) {
				@Override
				public boolean onDrawableTouch(View v,MotionEvent event) {
					EditText et = (EditText)v;
					et.setText("");
					return false;
				}
			};
			et.setOnTouchListener(l);
		}
	}
    
}