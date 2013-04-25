package com.code4bones.notummobile;

import java.util.ArrayList;

import com.code4bones.utils.NetLog;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class HistActivity extends Activity {

	public ParamEntry mParamEntry;
	public ListView mListView;
	public HistListAdapter mAdapter;
	public ArrayList<HistEntry> mHist = new ArrayList<HistEntry>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hist);
	
		Intent res = this.getIntent();
		mParamEntry = res.getParcelableExtra("paramEntry");
		NetLog.v("%s",mParamEntry);
		
		this.setTitle(mParamEntry.name);
		
		mParamEntry.populateHist(false);
		
		mAdapter = new HistListAdapter(this,mParamEntry,mOnClick);
		mListView = (ListView)this.findViewById(R.id.lvHist);
		mListView.setAdapter(mAdapter);
	}

	public OnClickListener mOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			HistEntry entry = (HistEntry)v.getTag();
			if ( v.getId() == R.id.ibHistSelectDate )
				selectDate(entry);
			else
				changeValue(v);
		}
	};
	
	public void selectDate(HistEntry entry) {
		
	}
	
	public void changeValue(View v) {
		HistEntry entry = (HistEntry)v.getTag();
		int idx = mHist.indexOf(entry);
		if ( idx == -1 )
			mHist.add(entry);
		if ( v.getId() == R.id.ibHistDecValue ) {
			entry.value -= mParamEntry.incVal; 
		} else {
			entry.value += mParamEntry.incVal;
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
			Save();
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
	
	public void Save() {
		for ( HistEntry e : mHist ) {
			e.Save(ProfileList.getInstance().getDB());
		}
	}
	
	@Override
	public void onBackPressed() {
	    // This will be called either automatically for you on 2.0
	    // or later, or by the code above on earlier versions of the
	    // platform.
		if ( mHist.size() == 0 ) {
			super.onBackPressed();
		} else {
			this.openOptionsMenu();
		}
		return;
	}	
}
