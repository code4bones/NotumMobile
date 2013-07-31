package com.code4bones.notummobile;

import android.graphics.PointF;

public class Coord extends PointF {

	Object mData;
	
	public Coord() {
		// TODO Auto-generated constructor stub
	}

	public Coord(float x,float y,Object data) {
		this.x = x;
		this.y = y;
		this.mData = data;
	}
	
	public Coord(float x,float y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return String.format("Coord[%f,%f]",x,y);
	}
}
