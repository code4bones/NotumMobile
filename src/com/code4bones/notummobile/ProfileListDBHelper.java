package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;



public class ProfileListDBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "notumMobile";
	public static final String DB_PROFILES = "create table if not exists profiles ( _id integer primary key autoincrement,"
										   +"name text not null,"
										   +"image blob);";
	public static final String  DB_HIST  = "create table if not exists hist ( _id integer primary key autoincrement,"
										  +"paramId integer not null,"
										  +"value double not null,"
										  +"changed datetime not null);";
	public static final String DB_PARAMS = "create table if not exists params ( _id integer primary key autoincrement,"
										  +"name text not null,"
										  +"profileId integer," 
										  +"measure text not null,"
										  +"startDate datetime not null,"
										  +"endDate datetime," 
										  +"step integer,"
										  +"startVal double not null,"
										  +"endVal double not null,"
										  +"incVal double not null,"
										  +"image blob)";
	
	
	public ProfileListDBHelper(Context context) {
		super(context, DB_NAME, null, 1);
		// TODO Auto-generated constructor stub
	}

	SQLiteDatabase DB() {
		return this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		NetLog.v("Database created");
		try {
		db.execSQL(DB_PROFILES);
		db.execSQL(DB_PARAMS);
		db.execSQL(DB_HIST);
		} catch ( Exception e ) {
			NetLog.v("DB ERROR: %s",e.getMessage());
		}
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		NetLog.v("NEED TO UPDATE DB !");
		
	}

}
