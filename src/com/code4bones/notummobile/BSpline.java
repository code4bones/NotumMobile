package com.code4bones.notummobile;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;

public class BSpline {

	private ArrayList<Coord> pts = new ArrayList<Coord>();
	
	
    // the basis function for a cubic B spline
    double b( int i, double t ) {
        switch( i ) {
        case -2:
            return (((-t + 3) * t - 3) * t + 1) / 6;
        case -1:
            return (((3 * t - 6) * t) * t + 4) / 6;
        case 0:
            return (((-3 * t + 3) * t + 3) * t + 1) / 6;
        case 1:
            return (t * t * t) / 6;
        }
        return 0; // we only get here if an invalid i is specified
    }

    // evaluate a point on the B spline
    private Coord p( int i, double t ) {
        double px = 0;
        double py = 0;
        for( int j = -2; j <= 1; j++ ) {
            PointF coordinate = pts.get(i + j);
            px += b(j, t) * coordinate.x;
            py += b(j, t) * coordinate.y;
        }
        return new Coord((float)px, (float)py);
    }

    final int STEPS = 12;

    public BSpline(ArrayList<Coord> pts) {
    	this.pts.addAll(pts);
    }
    
    public List<Coord> getInterpolated() {
        List<Coord> interpolatedCoordinates = new ArrayList<Coord>();
        Coord q = p(2, 0);
        interpolatedCoordinates.add(q);
        for( int i = 2; i < pts.size() - 1; i++ ) {
            for( int j = 1; j <= STEPS; j++ ) {
                q = p(i, j / (double) STEPS);
                interpolatedCoordinates.add(q);
            }
        }
        return interpolatedCoordinates;
    }

}

