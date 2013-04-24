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
	public static ProfileList mProfiles = null;
	
	/* Controls */
	private ListView lvProfiles;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mProfiles = ProfileList.getInstance(this);
		NetLog.v("We are started");
		
		lvProfiles = (ListView)this.findViewById(R.id.mainListView);

		lvProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> aview, View arg1, int itemPos,
					long index) {
				ProfileEntry entry = mProfiles.getAt(index);
				showProfileParams(entry);
			}
		});

		lvProfiles.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long index) {
				ProfileEntry entry = mProfiles.getAt(index);
				editProfile(entry,ProfileActivity.EDIT_PROFILE);
				return false;
			}
		
		});
		
		updateList();
	}
     
	public void updateList() {
		if ( mProfiles.populateProfiles() == 0 ) {
			editProfile(null,ProfileActivity.NEW_PROFILE);
		} else {
			ProfileEntry mItems[] = mProfiles.toArray();
			NetLog.v("List view items %d", mItems.length);
			ProfileListAdapter plAdapter = new ProfileListAdapter(this,mItems); 
			lvProfiles.setAdapter(plAdapter);
		}
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

	public void editProfile(ProfileEntry entry,int code) {
		Intent in = new Intent(this,ProfileActivity.class);
		in.putExtra(ProfileEntry.PROFILE_ENTRY, entry);
		this.startActivityForResult(in, code);
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.add_profile:
			this.editProfile(null, ProfileActivity.NEW_PROFILE);
			break;
		}
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data) {
		if ( resultCode != RESULT_OK ) {
			updateList();
			return;
		}
		switch ( requestCode ) {
		case ProfileActivity.NEW_PROFILE: // New Profile
				saveProfile(data);
			break;
		case ProfileActivity.EDIT_PROFILE: // Edit Profile
			NetLog.v("Profile updated");
				saveProfile(data);
			break;
		}
	}

	private void saveProfile(Intent data) {
		ProfileEntry entry = data.getParcelableExtra(ProfileEntry.PROFILE_ENTRY);
		if ( entry != null ) 
			mProfiles.add(entry);
		else
			NetLog.v("DELETE");
		updateList();
	}
	
	private void showProfileParams(ProfileEntry entry) {
		NetLog.v("Starting profile params editing ( %s )",entry.profileName);
		Intent i = new Intent(this,ParamListActivity.class);
		i.putExtra(ProfileEntry.PROFILE_ENTRY, entry);
		mProfiles.setProfile(entry);
		this.startActivityForResult(i, 22);
	}
	


}
