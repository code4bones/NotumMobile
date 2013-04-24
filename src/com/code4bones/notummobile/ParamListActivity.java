package com.code4bones.notummobile;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.code4bones.utils.NetLog;
//import com.iguanaui.columnseries.R;
import com.iguanaui.controls.DataChart;
import com.iguanaui.controls.axes.CategoryAxis;
import com.iguanaui.controls.axes.CategoryXAxis;
import com.iguanaui.controls.axes.NumericAxis;
import com.iguanaui.controls.axes.NumericYAxis;
import com.iguanaui.controls.valuecategory.ColumnSeries;
import com.iguanaui.controls.valuecategory.ValueCategorySeries;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ParamListActivity extends Activity implements OnDateSetListener {

	public final ProfileList mProfiles = ProfileList.getInstance();
	public ProfileEntry mProfile;
	private List<String> categories=new ArrayList<String>();
	private List<Float> column1=new ArrayList<Float>();
//	private List<Float> column2=new ArrayList<Float>();
	
	TextView mTvParamName;
	TextView mTvParamDate;
	ProgressBar mProgress;
	TextView mTvStartValue;
	TextView mTvTargetValue;
	HorizontalListView mParamList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_param_list);
		mProfile = mProfiles.getCurrentProfile();
		
		this.setTitle(mProfile.profileName);
		
		mTvParamName = (TextView)this.findViewById(R.id.tvProfileName);
		mTvParamDate = (TextView)this.findViewById(R.id.tvParamDate);
		mTvStartValue = (TextView)this.findViewById(R.id.tvStartValue);
		mTvTargetValue = (TextView)this.findViewById(R.id.tvEndValue);
		mProgress = (ProgressBar)this.findViewById(R.id.pbProgress);
		
		mParamList = (HorizontalListView) findViewById(R.id.vwParamList);  
		mParamList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapt, View arg1,
					int position, long arg3) {
				ParamEntry entry = (ParamEntry)adapt.getItemAtPosition(position);
				editParam(entry);
				return false;
			}
		});
		mParamList.setOnItemClickListener( new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapt, View arg1, int position,
					long arg3) {
				ParamEntry entry = (ParamEntry)adapt.getItemAtPosition(position);
				showParam(entry);
			}
		});
		
		setParamValueListener(new int[] {R.id.ibValueDec,R.id.ibValueInc,R.id.ibValueApply,R.id.ibSelectDate});
		this.updateParamList();
		
	} // onCreate

	public void updateParamList() {
		if ( mProfile.populateParams(mProfiles.getDB() ) > 0) {
			mParamList.setAdapter(new ParamListAdapter(mProfile.toArray()));
			this.showParam(mProfile.currentParam());
		} else {
			Intent i = new Intent(this,NewParamActivity.class);
			i.putExtra(NewParamActivity.NEW_LIST, true);
			this.startActivityForResult(i, NewParamActivity.ADD);
		}
	}
	
	public void setParamValueListener(int ids[]) {
		for ( int id : ids ) {
			ImageButton btn = (ImageButton)this.findViewById(id);
			btn.setOnClickListener(mOnParamValueClicked);
		}
	}
	
	private OnClickListener mOnParamValueClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if ( v.getId() == R.id.ibValueApply )
				applyParamValue();
			else if ( v.getId() == R.id.ibSelectDate )
				selectParamValueDate();
			else {
				boolean fInc = v.getId() == R.id.ibValueInc;
				changeParam(fInc);
			}
		}
	
	};
	
	public void changeParam(boolean fInc) {
		ParamEntry paramEntry = mProfile.currentParam();
		HistEntry entry = paramEntry.mActiveHist;
		if ( fInc )
			entry.value += paramEntry.incVal;
		else
			entry.value -= paramEntry.incVal;
		
		
		double nMin = this.mRender.getYAxisMin();
		double nMax = this.mRender.getYAxisMax();
		
		
		int idx = mSers.getItemCount()-1;
		mSers.remove(idx);
		mSers.add(entry.value);
		mDataset.removeSeries(0);
		mDataset.addSeries(mSers.toXYSeries());
		
		if ( entry.value < this.mProfile.currentParam().mMinHist.value )
			nMin = entry.value - (entry.value / 2);
		else if ( entry.value > this.mProfile.currentParam().mMaxHist.value )
			nMax = entry.value + (entry.value /2 );
		
	    this.mRender.setYAxisMin(nMin);
	    this.mRender.setYAxisMax(nMax);

		mChart.repaint();
	    
		NetLog.v("changeParam: %s",entry);
		updateProgress(paramEntry);
		
	}
	
	public void updateProgress(ParamEntry entry) {
		double min = entry.startVal; // 2
		double max = entry.targetVal - min; // 10
		double cur  = entry.mActiveHist.value - min; // 2
		int val = (int)(100 / (max / cur));
		this.mProgress.setProgress(val);
	}
	
	private void selectParamValueDate() {
		Dialog dlg = new DatePickerDialog(this,this,2013,05,21);
		dlg.show();
	}
	
	private void applyParamValue() {
		ParamEntry paramEntry = this.mProfile.currentParam();
		HistEntry entry = paramEntry.mActiveHist;
		entry.Save(ProfileList.getInstance().getDB());
		NetLog.v("Apply %s",entry);
		this.showParam(paramEntry);
	}
	

	public XYMultipleSeriesRenderer getBarDemoRenderer() {
	   
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setMargins(new int[] {20, 30, 15, 0});
	    renderer.setBarSpacing(0.1);
	    

	    
	    SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	    
	    // saved hist
	    
	    r.setColor(Color.parseColor("#D3DAFE"));
	    renderer.setInScroll(true);
	    renderer.addSeriesRenderer(r);
	    renderer.setDisplayChartValues(true);
	    renderer.setLabelsColor(Color.YELLOW);
	    renderer.setAntialiasing(true);
	    return renderer;
	  }	
	
	
	public CategorySeries mSers; 
	
	private void createChart() {
		FrameLayout item = (FrameLayout)this.findViewById(R.id.chartFrame);
		//item.setBackgroundResource(R.drawable.gradient_param_list);
		

		mDataset = new XYMultipleSeriesDataset();
		ParamEntry paramEntry = mProfile.currentParam();
	    HistEntry list[] = paramEntry.toArray();

	    CategorySeries series = new CategorySeries("История");
	    for ( int idx =0; idx < list.length;idx++ ) {
	    	series.add(list[idx].value);
	    	
	     }
	    
	     series.add(paramEntry.mActiveHist.value);
	     mDataset.addSeries(series.toXYSeries());
	     
	    mSers = series;
		mRender = this.getBarDemoRenderer();
	    
	    NetLog.v("Series %d",mDataset.getSeriesCount());
	    
		mChart = ChartFactory.getBarChartView(this, mDataset, mRender, BarChart.Type.DEFAULT);
		item.addView(mChart,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		mChart.setBackgroundResource(R.drawable.gradient_barchart);
	}
	
	
	public void showParam(ParamEntry entry) {
		if ( entry == null ) {
			NetLog.Toast(this, "There are no parameters for that profile");
			return;
		}
		this.mProfile.setCurrentParam(entry);
		entry.populateHist(true);
		this.createChart();
		
		String sTitle = String.format("%s / %s", this.mProfile.profileName,entry.name);
		this.mTvParamName.setText(sTitle);
		this.mTvParamDate.setText(entry.startDate.toGMTString());
		this.mTvStartValue.setText(String.valueOf(entry.startVal));
		this.mTvTargetValue.setText(String.valueOf(entry.targetVal));
		
		if ( this.mChart != null ) {
			HistEntry eMax = entry.mMaxHist;
			HistEntry eMin = entry.mMinHist;
			HistEntry eFirst = entry.mFirstHist;
			
			updateProgress(entry);
			//this.mProgress.setMax((int)(entry.targetVal - entry.startVal));
			//this.mProgress.setProgress((int)entry.mActiveHist.value);

			this.mRender.setChartTitle(String.format("%f", entry.startVal));
		    // dates
			this.mRender.setXTitle("Дни");
		    this.mRender.setXAxisMin(0);
		    this.mRender.setXAxisMax(entry.mList.size()+2);
		    // values
			this.mRender.setYTitle(entry.measure);
		    this.mRender.setYAxisMin(eMin.value - (eMin.value / 2));
		    this.mRender.setYAxisMax(eMax.value + (eMax.value / 2));
		    
		    SimpleSeriesRenderer r = this.mRender.getSeriesRendererAt(0);
		    r.setGradientEnabled(true);
		    r.setGradientStart(eMin.value, Color.parseColor("#5662A3"));
		    r.setGradientStop(eMax.value,Color.parseColor("#D3DAFE"));
		    
		    mChart.repaint();
		}
	
	}
	
	public XYMultipleSeriesDataset  mDataset;
	public XYMultipleSeriesRenderer mRender;
	public GraphicalView mChart;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.param_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent res;
		switch ( item.getItemId() ) {
		case R.id.itemParamListAdd:
			res = new Intent(this,NewParamActivity.class);
			this.startActivityForResult(res, NewParamActivity.ADD);
			break;
		case R.id.itemParamListHist:
			res = new Intent(this,HistActivity.class);
			res.putExtra("paramEntry", this.mProfile.currentParam());
			this.startActivityForResult(res, NewParamActivity.HIST);
			break;
		case R.id.itemParamListEdit:
			break;
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data) {
		switch ( requestCode ) {
		case NewParamActivity.ADD: // New Profile
			if ( resultCode == RESULT_OK )
				addNewParam(data);
			else {
				finish();
			}
			break;
		case NewParamActivity.EDIT: // Edit Existing
				if ( resultCode != RESULT_OK )
					return;
			
				ParamEntry entry = (ParamEntry) data.getParcelableExtra(NewParamActivity.PARAM_ENTRY);
				this.mProfile.setCurrentParam(entry);
				this.updateParamList();
			break;
		}
	}
	
	
	
	public void editParam(ParamEntry entry) {
		this.mProfile.setCurrentParam(entry);
		Intent i = new Intent(this,NewParamActivity.class);
		i.putExtra(NewParamActivity.PARAM_ENTRY, entry);
		this.startActivityForResult(i, NewParamActivity.EDIT);
	}
	
	public void addNewParam(Intent data) {
		ParamEntry entry = (ParamEntry) data.getSerializableExtra(NewParamActivity.PARAM_ENTRY);
		mProfile.setCurrentParam(entry);
		this.updateParamList();
	}


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar c = Calendar.getInstance();
		c.set(year, monthOfYear, dayOfMonth);
		Date selDate = c.getTime();
		this.mTvParamDate.setText(selDate.toGMTString());
		this.mTvParamDate.setTag(selDate);
		this.mProfile.currentParam().mActiveHist.changed = selDate;
	}
	
	
	
}
