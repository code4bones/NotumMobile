package com.code4bones.notummobile;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.code4bones.utils.NetLog;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ParamEntry extends Object implements Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6172506447065668460L;
	
	// history entries
	transient List<HistEntry> mList = new ArrayList<HistEntry>();
	transient NewParamActivity mActivity;
	transient Context mContext;
	transient HistEntry mLastHist = null;
	transient HistEntry mActiveHist = null;
	
	public Bitmap image;
	public long   paramId;
	public long profileId;
	public String name;
	public String measure;
	public Date   startDate;
	public Date	  endDate;
	public long   stepVal;
	public double  incVal;
	public double  startVal;
	public double  targetVal;
	
	
	public ParamEntry(Context context,long profileId) {
		super();
		
		this.image = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
		this.paramId = -1;
		this.profileId = profileId;
		this.name = "...";
		this.measure = "?";
		this.startDate = new Date();
		this.endDate = new Date();
		this.stepVal = 1;
		this.incVal = 1;
		this.startVal = 10;
		this.targetVal = 100;
		// TODO Auto-generated constructor stub
	}

	public static final Parcelable.Creator<ParamEntry> CREATOR = new Parcelable.Creator<ParamEntry>() {
		public ParamEntry createFromParcel(Parcel in) {
		    return new ParamEntry(in);
		}
	
		public ParamEntry[] newArray(int size) {
		    return new ParamEntry[size];
		}
	};
	
	public ParamEntry(long profileId) {
		this.paramId = -1;
		this.profileId = profileId;
		this.name = "??";
		this.incVal = 1;
		this.stepVal = 0;
		this.startVal = 0;
		this.targetVal = 0;
		this.measure = "?";
		this.startDate = new Date();
		this.endDate = new Date();
		this.image = null;
	}
	
	private	ParamEntry(Parcel in) {
		this.paramId = in.readLong();
		this.profileId = in.readLong();
		this.name = in.readString();
		this.incVal = in.readDouble();
		this.stepVal = in.readLong();
		this.startVal = in.readDouble();
		this.targetVal = in.readDouble();
		this.measure = in.readString();
		this.startDate = new Date(in.readLong());
		this.endDate = new Date(in.readLong());
		this.image = in.readParcelable(Bitmap.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel o, int flags) {
		o.writeLong(this.paramId);
		o.writeLong(this.profileId);
		o.writeString(this.name);
		o.writeDouble(this.incVal);
		o.writeLong(this.stepVal);
		o.writeDouble(this.startVal);
		o.writeDouble(this.targetVal);
		o.writeString(this.measure);
		o.writeLong(this.startDate.getTime());
		o.writeLong(this.endDate.getTime());
		o.writeParcelable(this.image, flags);
	}
	
	public String toString() {
		String s = String.format("ParamEntry[name %s,date: %s,start:%f, end %f,inc %f", name,startDate,startVal,targetVal,incVal);
		return s;
	}
	
	public void collectData(Activity v) {
		
		
		this.mActivity = (NewParamActivity)v;
		this.mContext  = mActivity.getBaseContext();
		
		this.name = getText(R.id.etParamName);
		this.measure = getText(R.id.etMeasure);
		
		this.startVal = getNumber(R.id.etParamCurrentValue);
		this.targetVal = getNumber(R.id.etTargetValue);
		this.incVal = getNumber(R.id.etIncVal);
		
		this.image = ((BitmapDrawable)mActivity.ibIcon.getDrawable()).getBitmap();
	}
	
	public ParamEntry ( Cursor c )  {
		super();
		this.paramId = c.getLong(0);
		this.name = c.getString(1);
		this.profileId = c.getLong(2);
		this.measure = c.getString(3);
		this.startDate = new Date(c.getLong(4));
		this.endDate = new Date(c.getLong(5));
		this.stepVal = c.getLong(6);
		this.startVal = c.getDouble(7);
		this.targetVal = c.getDouble(8);
		this.incVal = c.getDouble(9);
		byte[] blob = c.getBlob(10);
		this.image = BitmapFactory.decodeByteArray(blob, 0, blob.length);
	}
	
	public void setImage(String iconPath) {
		if ( iconPath == null ) {
			NetLog.v("Image is unchanged");
			return;
		}
		Uri uri = Uri.parse(iconPath);
		InputStream is;
		try {
			is = mContext.getContentResolver().openInputStream(uri);
			Bitmap image = BitmapFactory.decodeStream(is);
			this.image = Bitmap.createScaledBitmap(image, 64, 64, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Double getNumber(int id) {
		try {
			return Double.parseDouble(this.getText(id));
		} catch ( NumberFormatException e ) {
		}
		return 0.0;
	}
	
	private String getText(int id) {
		EditText et = (EditText)mActivity.findViewById(id);
		if ( et == null )
			return "???";
		return et.getText().toString();
	}
	
	

	public void Delete(ProfileListDBHelper dbHelper) {
		SQLiteStatement ins = null;
		long rows = 0;
		if ( this.paramId == -1 )
			return;
		ins = dbHelper.DB().compileStatement("delete from params where _id = ?");
		ins.clearBindings();
		ins.bindLong(1, this.paramId);
		//rows = ins.executeUpdateDelete();
		ins.execute();
		NetLog.v("Param with id %d,deleted ( %d )",this.paramId,rows);
	}
	
	public void Save(ProfileListDBHelper dbHelper) {
		SQLiteStatement ins = null;
		long rows = 0;
			
		if ( this.paramId == -1 )
			ins = dbHelper.DB().compileStatement("insert into params(name,profileId,measure,startDate,endDate,step,startVal,endVal,incVal,image) "
					+"values(?,?,?,?,?,?,?,?,?,?)");
		else
			ins = dbHelper.DB().compileStatement("update params set name = ?,profileId = ?,measure = ?,startDate= ?,endDate  = ?,step  = ?,startVal = ?,endVal = ?,incVal = ?,image  = ? where _id = ?");
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, bos);
		byte[] blob = bos.toByteArray();
		
		ins.clearBindings();
		ins.bindString(1, this.name);
		ins.bindLong(2,this.profileId);
		ins.bindString(3, this.measure);
		ins.bindLong(4, this.startDate.getTime());
		ins.bindLong(5, this.endDate.getTime());
		ins.bindLong(6,this.stepVal);
		ins.bindDouble(7, this.startVal);
		ins.bindDouble(8, this.targetVal);
		ins.bindDouble(9, this.incVal);
		ins.bindBlob(10, blob);
		
		if ( this.paramId == -1 ) {
			this.paramId = ins.executeInsert();
			NetLog.v("New Profile Added %s ( ID = %d )", this.name,this.profileId);
		} else {
			ins.bindLong(11,this.paramId);
			//rows = ins.executeUpdateDelete();
			ins.execute();
			NetLog.v("Profile Updated %s ( affected %d ),ID = %d",this.name,rows,this.paramId);
		}
	}

	public HistEntry mMaxHist = null;
	public HistEntry mMinHist = null;
	public HistEntry mFirstHist = null;
	
	public boolean getMinMaxHist() {
		mMaxHist = null;
		mMinHist = null;
		for ( HistEntry e : mList ) {
			if ( mMaxHist == null || mMaxHist.value < e.value )
				mMaxHist = e;
		}
		for ( ListIterator<HistEntry> it = mList.listIterator(mList.size());it.hasPrevious();) {
			HistEntry e = it.previous();
			if ( mMinHist == null || mMinHist.value > e.value )
				mMinHist = e;
		}
		NetLog.v("Min: %s", mMinHist);
		NetLog.v("Max: %s", mMaxHist);
		return false;
	}
	
	public int populateHist(boolean fAsc) {
		String sid = String.valueOf(this.paramId);
		Cursor curs = ProfileList.getInstance().getDB().rawQuery("select * from hist where paramId = ? order by changed "+(fAsc?"asc":"desc"),new String[]{sid});
		this.mLastHist = null;
		this.mActiveHist = null;
		this.mFirstHist = null;
		mList.clear();
		if ( curs.moveToFirst()) {
			do {
				HistEntry entry = new HistEntry(curs);
				mList.add(entry);
			} while ( curs.moveToNext() );
		}
		if ( curs != null && !curs.isClosed())
			curs.close();
		
		NetLog.v("Hist %d for param %s",mList.size(),this.name);
		for ( HistEntry e : mList ) {
			NetLog.v("-> %s",e);
		}
		
		if ( mList.isEmpty() ) {
			this.mActiveHist = new HistEntry(this);
			this.mActiveHist.Save(ProfileList.getInstance().getDB());
			this.populateHist(fAsc);
			//mList.add(this.mActiveHist);
		}
		if ( mList.isEmpty() == false ) {
			this.mFirstHist = mList.get(0);
			this.mLastHist = mList.get(mList.size()-1);
			this.mActiveHist = new HistEntry(this.mLastHist);
		} 
		
		this.getMinMaxHist();
		
		return mList.size();
	}
	
	HistEntry[] toArray() {
		return mList.toArray(new HistEntry[]{});
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}


}
