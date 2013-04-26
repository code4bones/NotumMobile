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
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.code4bones.utils.MessageBox;
import com.code4bones.utils.NetLog;
//import com.iguanaui.columnseries.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
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
	
	TextView mTvParamName;
	TextView mTvParamDate;
	ProgressBar mProgress;
	TextView mTvStartValue;
	TextView mTvTargetValue;
	TextView mTvCurrentValue;
	HorizontalListView mParamList;
	View mListViewItem = null;
	View mListViewItemSelected = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_param_list);
		mProfile = mProfiles.getCurrentProfile();
		
		this.setTitle("Просмотр профиля \"" + mProfile.profileName+"\"");
		
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
				toggleItem(arg1);
				ParamEntry entry = (ParamEntry)adapt.getItemAtPosition(position);
				editParam(entry);
				return false;
			}
		});
		mParamList.setOnItemClickListener( new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapt, View item, int position,
					long arg3) {
				mListViewItemSelected = item;
				toggleItem(item);
				ParamEntry entry = (ParamEntry)adapt.getItemAtPosition(position);
				entry.resetDate(null);
				showParam(entry);
			}
		});
		
		setParamValueListener(new int[] {R.id.ibValueDec,R.id.ibValueInc,R.id.ibValueApply,R.id.ibSelectDate});
		this.updateParamList();
		
	} // onCreate

	public void toggleItem(View view) {
		if ( this.mListViewItem != null ) {
			mListViewItem.setBackgroundResource(0);
		} 
		view = view.findViewById(R.id.paramName);
		view.setBackgroundResource(R.drawable.list_view_selection);
		mListViewItem = view;
		
	}
	
	
	public void updateParamList() {
		if ( mProfile.populateParams(mProfiles.getDB() ) > 0) {
			mParamList.setAdapter(new ParamListAdapter(mProfile.toArray()));
			ParamEntry pe = mProfile.currentParam();
			pe.resetDate(null);
			this.showParam(pe);
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
			nMin = entry.value- (entry.value / 3);
		else if ( entry.value > this.mProfile.currentParam().mMaxHist.value )
			nMax = entry.value + (entry.value / 3);
		
	    this.mRender.setYAxisMin(nMin);
	    this.mRender.setYAxisMax(nMax);
		this.mRender.setChartTitle(String.format("%f", entry.value));
	   
		mChart.repaint();
	    
		NetLog.v("changeParam: %s",entry);
		updateProgress(paramEntry);
		
	}
	
	public void updateRender(ParamEntry entry) {
		HistEntry eMin = entry.mMinHist;
		HistEntry eMax = entry.mMaxHist;
		double nMin = Math.min(eMax.value, eMin.value);
		double nMax = Math.max(eMax.value, eMax.value);
	    //nMax -= nMin;
	    //nMin  = 0;
		this.mRender.setYAxisMin(nMin - (nMin / 3));
	    this.mRender.setYAxisMax(nMax + (nMax / 3));
	}
	
	public void updateProgress(ParamEntry entry) {
		double min = entry.startVal; // 2
		double max = entry.targetVal - min; // 10
		double cur  = entry.mActiveHist.value - min; // 2
		int val = (int)(100 / (max / cur));
		this.mProgress.setProgress(val);
	}
	
	private void selectParamValueDate() {
		//TODO:
		ParamEntry pe = this.mProfile.currentParam();
		Dialog dlg = new DatePickerDialog(this,this,1900+pe.changed.getYear(),pe.changed.getMonth(),pe.changed.getDate());
		dlg.show();
	}
	
	private void applyParamValue() {
		final ParamEntry paramEntry = this.mProfile.currentParam();
		final HistEntry entry = paramEntry.mActiveHist;
		final HistEntry dupEntry = paramEntry.exists(entry);
		if ( dupEntry != null ) {
			String msg = String.format("На это число уже есть значение,заменить %f на %f ?", dupEntry.value,entry.value);
			MessageBox.Show(ProfileList.dateStr(entry.changed), msg, this,MessageBox.MB_YESNO,new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if ( which == DialogInterface.BUTTON_POSITIVE ) {
						entry.id = dupEntry.id;
						entry.Save();
						paramEntry.resetDate(null);
						showParam(paramEntry);
					} 
				}
			});
		} else {
			entry.Save();
			paramEntry.resetDate(null);
			showParam(paramEntry);
		}
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
	    NumberFormat nf = NumberFormat.getInstance();
	    nf.setMaximumFractionDigits(3);
	    r.setChartValuesFormat(nf);
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
	    HistEntry hMin = paramEntry.mMinHist;
	    
	    String msg = String.format("Кол-во замеров: %d.", list.length);
	    CategorySeries series = new CategorySeries(msg);
	    for ( int idx =0; idx < list.length;idx++ ) {
	    	series.add(list[idx].value);
	    	
	     }
	    
	     series.add(paramEntry.mActiveHist.value);
	     mDataset.addSeries(series.toXYSeries());
	     
	    mSers = series;
		mRender = this.getBarDemoRenderer();
		mRender.setZoomButtonsVisible(true);
		mChart = ChartFactory.getBarChartView(this, mDataset, mRender, BarChart.Type.DEFAULT);
		item.addView(mChart,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		mChart.setBackgroundResource(R.drawable.gradient_barchart);
		
	}
	
	
	public void showParam(ParamEntry entry) {
		if ( entry == null ) {
			NetLog.Toast(this, "There are no parameters for that profile");
			return;
		}
		//entry.resetDate(null);
		this.mProfile.setCurrentParam(entry);
		entry.populateHist(true);
		this.createChart();
		
		String sTitle = String.format("%s / %s", this.mProfile.profileName,entry.name);
		this.mTvParamName.setText(sTitle);
		this.mTvParamDate.setText(ProfileList.dateStr(entry.changed));
		this.mTvStartValue.setText(String.valueOf(entry.startVal));
		this.mTvTargetValue.setText(String.valueOf(entry.targetVal));
		
		if ( this.mChart != null ) {
			HistEntry eMax = entry.mMaxHist;
			HistEntry eMin = entry.mMinHist;
			
			updateProgress(entry);

			
			this.mRender.setChartTitle(String.format("%f", entry.mLastHist.value));
			this.mRender.setXAxisMin(0);
		    this.mRender.setXAxisMax(entry.mList.size()+2);
		    // values
			this.mRender.setYTitle(entry.measure);
			this.updateRender(entry);
		    
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
			this.editParam(this.mProfile.currentParam());
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
		case NewParamActivity.HIST:
			if ( resultCode == RESULT_OK )
				showParam(mProfile.currentParam());
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
		ParamEntry entry = (ParamEntry) data.getParcelableExtra(NewParamActivity.PARAM_ENTRY);
		mProfile.setCurrentParam(entry);
		this.updateParamList();
	}


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar c = Calendar.getInstance();
		c.set(year, monthOfYear, dayOfMonth);
		Date selDate = c.getTime();
		this.mTvParamDate.setText(ProfileList.dateStr(selDate));
		this.mTvParamDate.setTag(selDate);
		this.mProfile.currentParam().resetDate(selDate);
	}
	
	
	
}
