package com.code4bones.notummobile;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;

public class PaintEx extends Paint {

	public PaintEx() {
	}
	
	public static Paint create(boolean solid) {
		Paint src = new Paint();
		src.setAntiAlias(true);
		src.setStyle(solid?Style.FILL:Style.STROKE);
		src.setStrokeJoin(Join.ROUND);
		src.setStrokeCap(Cap.ROUND);
		src.setDither(true);
		return src;
	}
	
	public static Paint withARGB(String argb,boolean solid) {
		Paint p = create(solid);
		p.setColor(Color.parseColor(argb));
		return p;
	}
}

