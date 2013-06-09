package com.code4bones.notummobile;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import com.code4bones.utils.NetLog;

public class ProfileList {
	
	private static ProfileList INSTANCE = null;
	
	private Context mContext = null;
	private ArrayList<ProfileEntry> mList = new ArrayList<ProfileEntry>();
	private ProfileListDBHelper dbHelper = null;
	
	private ProfileEntry mCurrentProfile;
	
	public String toString() {
		return String.format("ProfileList { CurrentProfile %s }", mCurrentProfile);
	}
	
	public ProfileList() {
	}
	
	public static ProfileList getInstance(Context context) {
		if ( INSTANCE == null )
			INSTANCE = new ProfileList();
		return INSTANCE.Init(context);
	}
	
	
	public static ProfileList getInstance() {
		if ( INSTANCE == null )
			INSTANCE = new ProfileList();
		return INSTANCE;
	}
	
	public ProfileList Init(Context context) {
		mContext = context;
		dbHelper = new ProfileListDBHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		//NetLog.v("Dropping...");
		//db.execSQL("drop table if exists params");
		
		db.execSQL(ProfileListDBHelper.DB_PARAMS);
		db.execSQL(ProfileListDBHelper.DB_PROFILES);
		db.execSQL(ProfileListDBHelper.DB_HIST);
		
		db.close();
		return this;
	}
	
	public static String dateStr(Date dt) {
		SimpleDateFormat df = new SimpleDateFormat("dd, MMMM");
		return df.format(dt);
	}
	
	public SQLiteDatabase getDB() {
		return dbHelper.getWritableDatabase();
	}

	public int populateProfiles() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		mList.clear();
		Cursor curs = db.rawQuery("select * from profiles",new String[]{});
		if ( curs.moveToFirst()) {
			do {
				ProfileEntry entry = new ProfileEntry(curs);
				mList.add(entry);
			} while ( curs.moveToNext() );
			if ( curs != null && !curs.isClosed())
				curs.close();
		}
		ProfileEntry entry = new ProfileEntry();
		mList.add(entry);
		//NetLog.Toast(mContext, "Loaded %d Profiles", mList.size());
		db.close();
		return mList.size();
	}
	
	public ProfileEntry[] toArray() {
		return mList.toArray(new ProfileEntry[]{});
	}
	
	public void DeleteProfileParam(ParamEntry entry) {
		entry.Delete(dbHelper);
	}
	
	public void SaveProfileParam(ParamEntry entry) {
		entry.Save(dbHelper);
	}
	
	public ProfileEntry add(ProfileEntry entry) {
		mList.add(entry);
		try {
			entry.Save(dbHelper.DB());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entry;
	}
	
	public void setProfile(ProfileEntry entry) {
		mCurrentProfile = entry;
	}
	
	public ProfileEntry getCurrentProfile() {
		return mCurrentProfile;
	}
	
	public ProfileEntry getAt(long index) {
		return mList.get((int) index);
	}
	
	public void Save() {
		for ( ProfileEntry entry : mList ) {
			try {
				entry.Save(dbHelper.DB());
			} catch (FileNotFoundException e) {
				NetLog.v("Cannot save Profile for %s", entry.profileName);
			}
		}
	}
	
	
}
