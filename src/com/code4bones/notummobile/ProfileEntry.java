package com.code4bones.notummobile;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.code4bones.utils.NetLog;

public class ProfileEntry extends Object implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String PROFILE_NAME = "profileName";
	public static final String PROFILE_ICON = "profileIcon";
	public static final String PROFILE_ID   = "profileId";
	
	public long   profileId;
	public String profileName;
	public Bitmap profileIcon;
	public ParamEntry mCurrentParam;

	public ArrayList<ParamEntry> mParams = new ArrayList<ParamEntry>();
	
	public String toString() {
		return String.format("ProfileEntry { id %d,name %s }", profileId,profileName);
	}
	
	public ProfileEntry(Cursor curs) {
		this.profileId = curs.getLong(0);
		this.profileName = curs.getString(1);
		byte[] blob = curs.getBlob(2);
		this.profileIcon = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		NetLog.v("Got %s bytes %d",this.profileName,blob.length);
	}
	
	public ProfileEntry(Context context,Intent data) {
		super();
		// new record
		this.profileId = -1;
		this.profileName = data.getStringExtra(PROFILE_NAME);
		String iconPath = data.getStringExtra(PROFILE_ICON);
		Uri uri = Uri.parse(iconPath);
		InputStream is;
		try {
			is = context.getContentResolver().openInputStream(uri);
			Bitmap image = BitmapFactory.decodeStream(is);
			this.profileIcon = Bitmap.createScaledBitmap(image, 64, 64, false);
			NetLog.v("New ProfileEntry(%s,%s)",this.profileName,this.profileIcon);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int populateParams(SQLiteDatabase db) {
		String sProfile = String.valueOf(this.profileId);
		Cursor curs = db.rawQuery("select * from params where profileId = ?",new String[]{sProfile});
		mParams.clear();
		if ( curs.moveToFirst()) {
			do {
				ParamEntry entry = new ParamEntry(curs);
				if ( mCurrentParam == null )
					this.setCurrentParam(entry);
				//NetLog.v("Selected %s",entry);
				mParams.add(entry);
			} while ( curs.moveToNext() );
			if ( curs != null && !curs.isClosed())
				curs.close();
		}
		NetLog.v("Loaded %d Params", mParams.size());
		return mParams.size();
	}
	
	public void setCurrentParam(ParamEntry entry) {
		this.mCurrentParam = entry;
		//if ( this.mCurrentParam != null )
		//	this.mCurrentParam.populateHist();
			
	}
	
	public ParamEntry currentParam() {
		return mCurrentParam;
	}
	
	ParamEntry[] toArray() {
		return mParams.toArray(new ParamEntry[]{});
	}
	
	public void Save(Context context,SQLiteDatabase db) throws FileNotFoundException {
		SQLiteStatement ins = null;
		long rows = 0;
		
		if ( this.profileId == -1 )
			ins = db.compileStatement("insert into profiles(name,image) values(?,?)");
		else
			ins = db.compileStatement("update profiles name = ?,image = ? where _id = ?");
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		profileIcon.compress(Bitmap.CompressFormat.PNG, 100, bos);
		byte[] blob = bos.toByteArray();
		
		ins.clearBindings();
		ins.bindString(1, profileName);
		ins.bindBlob(2, blob);
		if ( this.profileId == -1 ) {
			this.profileId = ins.executeInsert();
			NetLog.v("New Profile Added %s ( ID = %d )", this.profileName,this.profileId);
		} else {
			
			//rows = ins.executeUpdateDelete();
			ins.execute();
			NetLog.v("Profile Updated %s ( affected %d ),ID = %d",this.profileName,rows,this.profileId);
		}
	}
}
