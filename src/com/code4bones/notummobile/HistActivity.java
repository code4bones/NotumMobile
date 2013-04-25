package com.code4bones.notummobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.code4bones.utils.NetLog;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;

public class HistActivity extends Activity implements OnDateSetListener {

	public ParamEntry mParamEntry;
	public ListView mListView;
	public HistEntry mHistEntry = null;
	public HistListAdapter mAdapter;
	public ArrayList<HistEntry> mChange = new ArrayList<HistEntry>();
	public ArrayList<HistEntry> mRemove = new ArrayList<HistEntry>();
	public ArrayList<HistEntry> mChangeDate = new ArrayList<HistEntry>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hist);
	
		Intent res = this.getIntent();
		mParamEntry = res.getParcelableExtra("paramEntry");
		NetLog.v("%s",mParamEntry);
		
		this.setTitle("Детализация \"" + mParamEntry.name+"\"");
		
		mListView = (ListView)this.findViewById(R.id.lvHist);
		reloadList();
	}

	public void reloadList() {
		mParamEntry.populateHist(false);
		mAdapter = new HistListAdapter(this,mParamEntry,mParamEntry.toArray(),mOnClick);
		mListView.setAdapter(mAdapter);
	}
	
	public OnClickListener mOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			HistEntry entry = (HistEntry)v.getTag();
			if ( v.getId() == R.id.ibHistSelectDate )
				selectDate(entry);
			else if ( v.getId() == R.id.chkDelete )
				toggleCheck(v);
			else
				changeValue(v);
		}
	};
	
	public void toggleCheck(View v) {
		CheckBox box = (CheckBox)v;
		HistEntry entry = (HistEntry)box.getTag();
		entry.checked = box.isChecked();
		if ( entry.checked ) {
			if ( this.mRemove.indexOf(entry) == -1 )
				this.mRemove.add(entry);
		} else
			this.mRemove.remove(entry);
		
		NetLog.v("[%d] Toggled on %s [ %s ]",mRemove.size(),entry,entry.checked);
	}
	
	public void selectDate(HistEntry e) {
		this.mHistEntry = e;
		Dialog dlg = new DatePickerDialog(this,this,1900+e.changed.getYear(),e.changed.getMonth(),e.changed.getDate());
		dlg.show();
	}
	
	public void changeValue(View v) {
		this.mHistEntry = (HistEntry)v.getTag();
		int idx = mChange.indexOf(this.mHistEntry);
		if ( idx == -1 )
			mChange.add(this.mHistEntry);
		if ( v.getId() == R.id.ibHistDecValue ) {
			this.mHistEntry.value -= mParamEntry.incVal; 
		} else {
			this.mHistEntry.value += mParamEntry.incVal;
		}
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.itemHistSave:
			Apply();
			this.setResult(RESULT_OK);
			finish();
			break;
		case R.id.itemHistClose:
			this.setResult(RESULT_CANCELED);
			finish();
			break;
		}
		return true;
	}
	
	public void Apply() {
		for ( HistEntry e : mRemove ) {
			e.Delete();
			mChange.remove(e);
			mChangeDate.remove(e);
		}
		for ( HistEntry e : mChange ) { 
			e.Save();
			mChangeDate.remove(e);
		}
		for ( HistEntry e : mChangeDate )
			e.Save();
	}
	
	@Override
	public void onBackPressed() {
		if ( mChange.size() == 0 && mRemove.size() == 0 && this.mChangeDate.size() == 0 ) {
			super.onBackPressed();
		} else {
			this.openOptionsMenu();
		}
		return;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
		Calendar c = Calendar.getInstance();
		c.set(year, monthOfYear, dayOfMonth);
		Date selDate = c.getTime();
		
		this.mHistEntry.changed = selDate;
		this.mAdapter.notifyDataSetChanged();
		int idx = mChangeDate.indexOf(this.mHistEntry);
		if ( idx == -1 )
			mChangeDate.add(this.mHistEntry);
	}	
}
