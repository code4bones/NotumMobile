package com.code4bones.notummobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.code4bones.utils.NetLog;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class GraphActivity extends Activity implements SplineGraphView.SplineGraphAdapter {

	public SplineGraphView mGraph;
	public final ProfileList mProfiles = ProfileList.getInstance();
	public ParamEntry mParamEntry;
	public Spinner mSpinner = null;
	public GraphSpinnerAdapter mAdapt = null;
	public GraphSpinnerEntry mPeriod = null;
	public BarChartView mBarChart = null;
	public boolean mIsLineChart = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		mParamEntry = mProfiles.getCurrentProfile().currentParam();
		
		setTitle(mParamEntry.name);
		
		this.findViewById(R.id.layoutSpinner).getBackground().setAlpha(100);
		
		mSpinner = (Spinner)this.findViewById(R.id.spinner1);
		mSpinner.getBackground().setAlpha(100);
		mSpinner.setPrompt("Выбор периода");
		
		GraphSpinnerEntry[] data = new GraphSpinnerEntry[]{
				new GraphSpinnerEntry("Вcе данные",GraphSpinnerEntry.ALL),
				new GraphSpinnerEntry("Неделя",GraphSpinnerEntry.WEEK),
				new GraphSpinnerEntry("Месяц",GraphSpinnerEntry.MONTH),
				new GraphSpinnerEntry("Год",GraphSpinnerEntry.YEAR),
				new GraphSpinnerEntry("Выбрать...",GraphSpinnerEntry.PERIOD)
				};
		
		ArrayList<GraphSpinnerEntry> arr = new ArrayList<GraphSpinnerEntry>();
		for ( GraphSpinnerEntry e : data )
			arr.add(e);
		
		mAdapt = new GraphSpinnerAdapter(this,android.R.layout.simple_spinner_item,arr); 
		mSpinner.setAdapter(mAdapt);
		mSpinner.setOnItemSelectedListener(this.mOnItemSelected);
		
		
		Intent intent = this.getIntent();
		this.mIsLineChart = intent.getIntExtra("type", 0) == R.id.ibLineButton; 
		if ( !this.mIsLineChart )
			createBarChart();
		else
			createLineChart();
		
	}

	
	public void createLineChart() {
		mGraph = new SplineGraphView(this);
		mGraph.setAdapter(this);
		mGraph.setLimitsValue((float)mParamEntry.startVal,(float)(mParamEntry.targetVal == Double.MIN_VALUE?Float.MAX_VALUE:mParamEntry.targetVal));
		
		FrameLayout ll = (FrameLayout)this.findViewById(R.id.graphLayout);
		ll.getBackground().setAlpha(100);
		ll.addView(mGraph, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		mGraph.dataChanged();
		mGraph.repaint();
		mGraph.moveToPoint(null);
	}
	
	
	
	public void createBarChart() {
		mBarChart = new BarChartView(this);
		FrameLayout ll = (FrameLayout)this.findViewById(R.id.graphLayout);
		ll.getBackground().setAlpha(100);
		ll.addView(mBarChart,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
//		mChart.setBackgroundResource(R.drawable.trans_bgr);
//		mChart.setTouchHandler(mChartHandler);
		updateBarChart();
	}
	
	public void updateBarChart() {
		mBarChart.reset();
		for ( HistEntry e : mParamEntry.mList ) {
			if ( !acceptEntry(e) )
				continue;
			BarChartView.ChartItem item = mBarChart.addItem((float)e.value,(float)e.changed.getDate());
			item.obj = e;
		}
		mBarChart.SelectItem(null);
	}
	
	public OnItemSelectedListener mOnItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos,
				long id) {
			GraphSpinnerEntry entry = (GraphSpinnerEntry) adapterView.getAdapter().getItem(pos);
			
			if ( entry.mType == GraphSpinnerEntry.PERIOD )
				selectPeriod(pos,entry);
			else {
				if ( entry.mType == GraphSpinnerEntry.ALL )
					mPeriod = null;
				else
					mPeriod = entry;
				if ( mIsLineChart ) {
				 mGraph.dataChanged();
				 mGraph.repaint();
				 mGraph.moveToPoint(null);
				} else {
					updateBarChart();
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	
	public void selectPeriod(final int pos,final GraphSpinnerEntry entry) {
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.dlg_date_period, null);
		final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setView(view);
		dlg.setCancelable(true);
		dlg.setTitle("Период");
	
		final DatePicker fromDate = (DatePicker)view.findViewById(R.id.datePicker1);
		final DatePicker toDate = (DatePicker)view.findViewById(R.id.datePicker2);
		Button btn = (Button)view.findViewById(R.id.button1);
		final AlertDialog passDlg = dlg.create();
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GraphSpinnerEntry e = new GraphSpinnerEntry("",GraphSpinnerEntry.DYNA);
				e.mFrom = Calendar.getInstance();
				e.mFrom.set(fromDate.getYear(), fromDate.getMonth(),fromDate.getDayOfMonth(),0,0,0);
				e.mTo = Calendar.getInstance();
				e.mTo.set(toDate.getYear(), toDate.getMonth(),toDate.getDayOfMonth(),0,0,0);
				int idx = mAdapt.Add(e);
				mPeriod = mAdapt.getItem(idx-1);
				mAdapt.notifyDataSetChanged();
				mSpinner.setSelection(idx,true);
				passDlg.dismiss();
				mGraph.dataChanged();
				mGraph.repaint();
			}
			
		});
		
		passDlg.show();
	}
	
	public void initGraph() {
	}
	

	@Override
	public String getXLabel(int item,Object data) {
		
		final SimpleDateFormat df = new SimpleDateFormat("d.MM");
		
		HistEntry e = (HistEntry)data;
		return df.format(e.changed);
	}

	@Override
	public ArrayList<Coord> getItems() {
		ArrayList<Coord> res = new ArrayList<Coord>();
		NetLog.v("Reloading %s",this.mPeriod);
		int idx = 0;
		for ( HistEntry e : mParamEntry.mList ) {
			if ( acceptEntry(e) )
				res.add(new Coord(idx++,(float)e.value,e));
		}
		return res;
	}

	public boolean acceptEntry(HistEntry e) {
		if ( this.mPeriod == null || this.mPeriod.mType == GraphSpinnerEntry.ALL )
			return true;
		
		return this.mPeriod.compare(e.changed);
	}
}
