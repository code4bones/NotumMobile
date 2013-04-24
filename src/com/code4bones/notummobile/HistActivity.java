package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.ListView;

public class HistActivity extends Activity {

	public ParamEntry mParamEntry;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hist);
	
		Intent res = this.getIntent();
		mParamEntry = res.getParcelableExtra("paramEntry");
		NetLog.v("%s",mParamEntry);
		
		this.setTitle(mParamEntry.name);
		
		mParamEntry.populateHist(false);
		
		ListView listView = (ListView)this.findViewById(R.id.lvHist);
		listView.setAdapter(new HistListAdapter(this,mParamEntry));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hist, menu);
		return true;
	}

}
