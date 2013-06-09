package com.code4bones.notummobile;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.code4bones.utils.MessageBox;
import com.code4bones.utils.NetLog;
import com.code4bones.utils.Utils;
//import com.iguanaui.columnseries.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ParamListActivity extends Activity implements OnDateSetListener {

	public final ProfileList mProfiles = ProfileList.getInstance();
	public ProfileEntry mProfile;
	
	TextView mTvParamName;
	TextView mTvParamDate;
	TextView mTvCurrentValue;
	RibbonView mParamList;
	BarChartView mChart = null;
	final NumberFormat mNfmt = NumberFormat.getInstance();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_param_list);
		mProfile = mProfiles.getCurrentProfile();
		mNfmt.setMaximumFractionDigits(2);
		mNfmt.setMinimumFractionDigits(2);
		
		this.setTitle("Просмотр профиля \"" + mProfile.profileName+"\"");
		
		mTvParamName = (TextView)this.findViewById(R.id.tvProfileName);
		mTvParamDate = (TextView)this.findViewById(R.id.tvParamDate);
		mTvCurrentValue = (TextView)this.findViewById(R.id.tvParamValue);
		//mProgress = (TextProgressBar)this.findViewById(R.id.pbProgress);
		//mProgress.setTextSize(18);
		
		mParamList = new RibbonView(this);
		LinearLayout ll = (LinearLayout)this.findViewById(R.id.paramListLayout);
		ll.addView(mParamList, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		mParamList.setSize(64, 10,12);
		mParamList.setIconHandler(new Handler() {
			public void handleMessage(Message msg) {
				RibbonView.Icon ico = (RibbonView.Icon)msg.obj;
				ParamEntry entry = (ParamEntry)ico.mObject;
				if ( entry.profileId != -1 ) {
					//toggleItem(item);
					entry.resetDate(null);
					showParam(entry);
				} else {
					Intent i = new Intent(ParamListActivity.this,NewParamActivity.class);
					startActivityForResult(i, NewParamActivity.ADD);
				}
			}
		});

		FrameLayout item = (FrameLayout)this.findViewById(R.id.chartFrame);
	    mChart = new BarChartView(this);
		item.addView(mChart,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		mChart.setBackgroundResource(R.drawable.trans_bgr);
		mChart.setTouchHandler(mChartHandler);
		setParamValueListener(new int[] {R.id.ibShowDetail,R.id.ibValueDec,R.id.ibValueInc,R.id.ibValueApply,R.id.ibSelectDate});
		this.updateParamList();
		
		Drawable d = item.getBackground();
		d.setAlpha(100);
		d = this.findViewById(R.id.paramControlsLayout).getBackground();
		d.setAlpha(100);
		d = this.findViewById(R.id.paramListLayout).getBackground();
		d.setAlpha(100);

		mTvCurrentValue.setOnClickListener(mOnValueClicked);
		
	} // onCreate

	private View.OnClickListener mOnValueClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showInputValueDialog();
		}
	};
	
	private Handler mChartHandler = new Handler() {
		public void handleMessage(Message msg) {
			BarChartView.ChartItem item = (BarChartView.ChartItem)msg.obj;
			HistEntry e = (HistEntry)item.obj;
			if ( e == null ) {
				mTvParamDate.setText(ProfileList.dateStr(mProfile.currentParam().changed));
				updateProgress(mProfile.currentParam(),null);
				return;
			}
			mTvParamDate.setText(ProfileList.dateStr(e.changed));
			updateProgress(mProfile.currentParam(),e);
		}
	};

	public void showInputValueDialog() {
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.current_value_dlg, null);
		
		final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setView(view);
		dlg.setCancelable(true);
		dlg.setTitle("Новое значние");
		ParamEntry pe = mProfile.currentParam();
		float val = (float)pe.mActiveHist.value;
		
		final Button okButton = (Button)view.findViewById(R.id.dlgSaveValue);
		final EditText editVal = (EditText)view.findViewById(R.id.dlgNewValue);
		final TextView tvVal = (TextView)view.findViewById(R.id.dlgTextView);
		editVal.setText(mNfmt.format(val));
		tvVal.setText(mNfmt.format(val));
		
		final AlertDialog inputDlg = dlg.create();
		inputDlg.show();
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO:
				try {
					ParamEntry pe = mProfile.currentParam();
					pe.mActiveHist.setValue(Float.parseFloat(editVal.getText().toString()));
					updateProgress(pe,null);
					inputDlg.cancel();
				} catch ( NumberFormatException e ) {
					NetLog.Toast(ParamListActivity.this, "Не верное значение");
				}
			}
		});
		
	}

	
	public void updateParamList() {
		if ( mProfile.populateParams(mProfiles.getDB() ) > 0) {
			mProfile.mParams.add(new ParamEntry(this,-1));
			mParamList.reset();
			for ( ParamEntry param : mProfile.mParams ) {
				RibbonView.Icon ico = mParamList.addItem(param.image, param.name);
				ico.mObject = param;
				if ( param.profileId == -1)
					ico.mHasBorder = false;
			}
			mParamList.repaint();
			ParamEntry pe = mProfile.currentParam();
			pe.resetDate(null);
			this.showParam(pe);
			//TODO:
			this.mParamList.selectByObject(mProfile.currentParam());
			
		} else {
			Intent i = new Intent(this,NewParamActivity.class);
			i.putExtra(NewParamActivity.NEW_LIST, true);
			this.startActivityForResult(i, NewParamActivity.ADD);
		}
	}
	
	public void setParamValueListener(int ids[]) {
		for ( int id : ids ) {
			View w = this.findViewById(id);
			w.setOnClickListener(mOnParamValueClicked);
		}
	}
	
	private OnClickListener mOnParamValueClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if ( v.getId() == R.id.ibValueApply )
				applyParamValue();
			else if ( v.getId() == R.id.ibShowDetail )
				showDetails();
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
		double val = entry.value;
		if ( fInc )
			val += paramEntry.incVal;
		else
			val -= paramEntry.incVal;
		
		entry.setValue(val);
		updateProgress(paramEntry,null);
	}
	
	
	public void updateProgress(ParamEntry entry,HistEntry he) {
		double hv  = he==null?entry.mActiveHist.value:he.value;
		boolean isChanged = he==null?entry.mActiveHist.isValueChanged():false;
		Button bn = (Button)this.findViewById(R.id.ibValueApply);
		bn.setEnabled(isChanged || entry.mActiveHist.isDateChanged());
		if ( isChanged ) {
			this.mTvCurrentValue.setTextColor(Color.RED);
			//this.mTvCurrentValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
		} else {
			//this.mTvCurrentValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			this.mTvCurrentValue.setTextColor(Color.BLACK);
		}
		this.mTvCurrentValue.setText(mNfmt.format((float)hv));
	}
	
	
	private void selectParamValueDate() {
		ParamEntry pe = this.mProfile.currentParam();
		Dialog dlg = new DatePickerDialog(this,this,1900+pe.changed.getYear(),pe.changed.getMonth(),pe.changed.getDate());
		dlg.show();
	}
	
	
	
	private void applyParamValue() {
		final ParamEntry paramEntry = this.mProfile.currentParam();
		final HistEntry entry = paramEntry.mActiveHist;
		final HistEntry dupEntry = paramEntry.exists(entry);
		if ( dupEntry != null ) {
			String msg = String.format("На это число уже есть значение,заменить %s на %s ?", mNfmt.format(dupEntry.value),mNfmt.format(entry.value));
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
	

	
	
	public void showParam(ParamEntry entry) {
		if ( entry == null ) {
			NetLog.Toast(this, "There are no parameters for that profile");
			return;
		}
		this.mProfile.setCurrentParam(entry);

		entry.populateHist(true);
		
		String sTitle = String.format("%s / %s", this.mProfile.profileName,entry.name);
		this.mTvParamName.setText(sTitle);
		this.mTvParamDate.setText(ProfileList.dateStr(entry.changed));

		mChart.reset();
		for ( HistEntry e : entry.mList ) {
			BarChartView.ChartItem item = mChart.addItem((float)e.value,(float)e.changed.getDate());
			item.obj = e;
		}
		mChart.SelectItem(null);
		updateProgress(entry,null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.param_list, menu);	
		return true;
	}
	
	public void showDetails() {
		Intent res = new Intent(this,HistActivity.class);
		res.putExtra("paramEntry", this.mProfile.currentParam());
		this.startActivityForResult(res, NewParamActivity.HIST);
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
			this.showDetails();
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
					if ( entry != null ) {
						RibbonView.Icon icon = this.mParamList.findObject(entry);
						if ( icon != null ) {
							this.mParamList.setVisible(this.mParamList.itemIndex(icon));
					}
				}
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
		
		boolean dc = this.mProfile.currentParam().mActiveHist.isDateChanged();
		Button bn = (Button)this.findViewById(R.id.ibValueApply);
		bn.setEnabled(dc);
		/*
		if ( dc ) 
			this.mTvParamDate.setTextColor(Color.RED);
		else
			this.mTvParamDate.setTextColor(Color.BLACK);
		 */
	}
	
	
	
}
