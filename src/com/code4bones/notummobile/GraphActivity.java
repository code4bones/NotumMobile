package com.code4bones.notummobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class GraphActivity extends Activity implements SplineGraphView.SplineGraphAdapter {

	public SplineGraphView mGraph;
	public final ProfileList mProfiles = ProfileList.getInstance();
	public ParamEntry mParamEntry;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		mParamEntry = mProfiles.getCurrentProfile().currentParam();
		
		mGraph = new SplineGraphView(this);
		mGraph.setAdapter(this);
		mGraph.setLimitsValue((float)mParamEntry.startVal,(float)(mParamEntry.targetVal == Double.MIN_VALUE?Float.MAX_VALUE:mParamEntry.targetVal));
		
		initGraph();
		FrameLayout ll = (FrameLayout)this.findViewById(R.id.graphLayout);
		ll.getBackground().setAlpha(100);
		ll.addView(mGraph, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		
		mGraph.repaint();
		
	}

	public void initGraph() {
	}
	

	@Override
	public String getXLabel(int item) {
		
		final SimpleDateFormat df = new SimpleDateFormat("d.MM");
		
		HistEntry e = mParamEntry.mList.get(item);
		Calendar cal = Calendar.getInstance();
		cal.setTime(e.changed);
		String str = String.format("%d.%d",cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
		return df.format(e.changed);
	}

	@Override
	public ArrayList<Coord> getItems() {
		ArrayList<Coord> res = new ArrayList<Coord>();
		int idx = 0;
		for ( HistEntry e : mParamEntry.mList ) {
			res.add(new Coord(idx++,(float)e.value));
		}
		return res;
	}

}
