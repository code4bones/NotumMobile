/**
 * 
 */
package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * @author code4bones
 *
 */
public class ImageTextView extends TextView {

	public ImageTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ImageTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public ImageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void localInit(int nWidth) {
		if ( mImg == null || mWidth != nWidth ) {
			if ( mImg != null )
				mImg.recycle();
			mWidth = nWidth;
			Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.header_center);
			mImg = Bitmap.createScaledBitmap( bmp, mWidth+mRight, this.getHeight(), true );	
			bmp.recycle();
		}
	}
	
	private Bitmap mImg = null;
	private int    mWidth = 0;
	private Paint  mPaint = new Paint();
	private int    mLeft = 0;//10;
	private int    mRight = 0;//25;
	
	public void setCenterPadding(int left,int right) {
		mLeft = left;
		mRight = right;
	}
	
	public void onDraw(Canvas c) {
		int paddingLeft = this.getCompoundPaddingLeft();
		int paddingRight = this.getCompoundPaddingRight();
		int width = this.getWidth();
		int textWidth = width - paddingLeft - paddingRight;
		if ( !this.isInEditMode() ) {
			localInit(textWidth);
			c.drawBitmap(mImg, paddingLeft-mLeft, 0, mPaint);
		}
		super.onDraw(c);
	}
}
