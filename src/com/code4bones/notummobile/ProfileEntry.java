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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.ImageButton;

import com.code4bones.utils.NetLog;

public class ProfileEntry extends Object implements Parcelable {

	/**
	 * 
	 */
	
	//public static final String PROFILE_NAME = "profileName";
	//public static final String PROFILE_ICON = "profileIcon";
	//public static final String PROFILE_ID   = "profileId";
	
	public static final String PROFILE_ENTRY = "profileEntry";
	
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
	
	public ProfileEntry() {
		this.profileId = -1;
		this.profileName = "Новый профиль";
	}
	
	public boolean collectData(ProfileActivity a) {
		this.profileName = a.profileName.getText().toString().trim();
		
		if ( this.profileName.length() == 0 ) {
			return false;
		}
		this.profileIcon = ((BitmapDrawable)a.profileIcon.getDrawable()).getBitmap();
		return true;
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
				entry.getLastDate();
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
	
	public void Delete(SQLiteDatabase db) {
		SQLiteStatement ins = null;
		long rows = 0;
		if ( this.profileId == -1 )
			return;
		ins = db.compileStatement("delete from profiles where _id = ?");
		ins.clearBindings();
		ins.bindLong(1, this.profileId);
		//rows = ins.executeUpdateDelete();
		ins.execute();
		NetLog.v("Param with id %d,deleted ( %d )",this.profileId,rows);
	}
	
	public void Save(SQLiteDatabase db) throws FileNotFoundException {
		SQLiteStatement ins = null;
		long rows = 0;
		
		if ( this.profileId == -1 )
			ins = db.compileStatement("insert into profiles(name,image) values(?,?)");
		else
			ins = db.compileStatement("update profiles set name = ?,image = ? where _id = ?");
		
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
			ins.bindLong(3, this.profileId);
			//rows = ins.executeUpdateDelete();
			ins.execute();
			NetLog.v("Profile Updated %s ( affected %d ),ID = %d",this.profileName,rows,this.profileId);
		}
	}

	public ProfileEntry(Parcel in) {
		this.profileId = in.readLong();
		this.profileName = in.readString();
		this.profileIcon = in.readParcelable(Bitmap.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<ProfileEntry> CREATOR = new Parcelable.Creator<ProfileEntry>() {
		public ProfileEntry createFromParcel(Parcel in) {
		    return new ProfileEntry(in);
		}
	
		public ProfileEntry[] newArray(int size) {
		    return new ProfileEntry[size];
		}
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeLong(this.profileId);
		out.writeString(this.profileName);
		out.writeParcelable(this.profileIcon, arg1);
	}
}
