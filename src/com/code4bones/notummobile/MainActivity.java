package com.code4bones.notummobile;

// http://atlant-inform.dyndns.org/medservice/dev/desc.php

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.code4bones.utils.NetLog;

// local

public class MainActivity extends Activity {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
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
	
	//test
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppConfig appCfg = new AppConfig(this);
		
		setContentView(R.layout.activity_main);
		
		mProfiles = ProfileList.getInstance(this);
		NetLog.v("We are started");
		
		
		lvProfiles = (ListView)this.findViewById(R.id.mainListView);

		lvProfiles.setClickable(true);
		lvProfiles.setLongClickable(true);
		lvProfiles.setFocusable(false);
		lvProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> aview, View arg1, int itemPos,
					long index) {
				ProfileEntry entry = mProfiles.getAt(index);
				if ( entry.profileId != -1 )
					showProfileParams(entry);
				else
				{
					editProfile(null, ProfileActivity.NEW_PROFILE);
				}
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
		
		if ( appCfg.isFirstRun() ) {
			NetLog.v("FIRST RUN!");
			appCfg.generateMasterPassword();
			NetLog.MsgBox(this, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					updateList();
				}
			},"Первый запуск/Обновление", 
			  "ЗАПИШИТЕ МАСТЕР-ПАРОЛЬ:%s\nВы можете защитить Ваши данные установив свой пароль в \"Меню->Настройки\"",appCfg.getPassword(AppConfig.PASSWD_MASTER));
		} else if ( appCfg.needPassword() ) {
			appCfg.showDialog(new Handler() {
				public void handleMessage(Message msg) {
					if ( msg.what == R.id.btnPasswordCancel )
						finish();
					else
						updateList();
				}
			}); 
			} else
				updateList();
	}
     
	public void updateList() {
		if ( mProfiles.populateProfiles() == 1 ) {
			editProfile(null,ProfileActivity.NEW_PROFILE);
		} else {
			ProfileEntry mItems[] = mProfiles.toArray();
			//NetLog.v("List view items %d", mItems.length);
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
		case R.id.mi_about:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://lk.notum.pro/category/13"));
			startActivity(browserIntent);
			break;
		case R.id.mi_settings:
			Intent set = new Intent(this,SettingsActivity.class);
			this.startActivity(set);
			//this.startActivityForResult(set, ProfileActivity.SETTINGS);
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
				saveProfile(data,true);
			break;
		case ProfileActivity.EDIT_PROFILE: // Edit Profile
				//NetLog.v("Profile updated");
				saveProfile(data,false);
			break;
		case ProfileActivity.SETTINGS:
			NetLog.v("Settings done");
			break;
		}
	}

	private void saveProfile(Intent data,boolean showParams) {
		ProfileEntry entry = data.getParcelableExtra(ProfileEntry.PROFILE_ENTRY);
		if ( entry != null ) {
			mProfiles.add(entry);
			if ( showParams )
				showProfileParams(entry);
		}
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
