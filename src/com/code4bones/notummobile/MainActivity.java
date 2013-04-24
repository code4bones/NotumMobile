package com.code4bones.notummobile;

import java.io.PrintStream;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

// local
import com.code4bones.utils.*;

public class MainActivity extends Activity {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final NetLog mLog = NetLog.getInstance(); //PrintStream NETLOG = NetLog.Init("clinch", "notumMobile.log.txt", true);
	
	static class LogInit {
		public LogInit() {
			mLog.Init("clinch", "notumMobile.log.txt", true);
		}
	}
	
	public static final LogInit _logInit = new LogInit();
	/* Profile List */
	public static ProfileList mProfiles = null;//ProfileList.getInstance();
	
	/* Controls */
	private ListView lvProfiles;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		mProfiles = ProfileList.getInstance(this);
		NetLog.v("We are started");
		
		// Setup profiles from Database
		
		mProfiles.populateProfiles();
		
		
		/* Setup Controls */
		
		// LIST VIEW
		lvProfiles = (ListView)this.findViewById(R.id.mainListView);
		ProfileEntry mItems[] = mProfiles.toArray();
		NetLog.v("List view items %d", mItems.length);
		ProfileListAdapter plAdapter = new ProfileListAdapter(this,mItems); 
		// header
		//View view = (View)this.getLayoutInflater().inflate(R.layout.profile_header_row, null);
		//lvProfiles.addHeaderView(view);
		lvProfiles.setAdapter(plAdapter);
		
		lvProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> aview, View arg1, int itemPos,
					long index) {
				ProfileEntry entry = mProfiles.getAt(index);
				showProfileParams(entry);
			}
		});
		
	}
     

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.add_profile:
			Intent _int = new Intent(this,ProfileActivity.class);
			this.startActivityForResult(_int, ProfileActivity.NEW_PROFILE);
			break;
		}
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data) {
		switch ( requestCode ) {
		case ProfileActivity.NEW_PROFILE: // New Profile
			if ( resultCode == RESULT_OK )
				saveProfile(data);
			break;
		}
	}
	
	private void showProfileParams(ProfileEntry entry) {
		NetLog.v("Starting profile params editing ( %s )",entry.profileName);
		Intent i = new Intent(this,ParamListActivity.class);
		i.putExtra(ProfileEntry.PROFILE_NAME, entry.profileName);
		i.putExtra(ProfileEntry.PROFILE_ID,entry.profileId);
		mProfiles.setProfile(entry);
		this.startActivityForResult(i, 22);
	}
	
	private void saveProfile(Intent data) {
		if ( data == null ) {
			NetLog.v("saveProfile(Intent == nill");
			return;
		}
		ProfileEntry entry = new ProfileEntry(this,data);
		mProfiles.add(entry);
	}


}
