package com.code4bones.notummobile;

import java.util.Date;

import com.code4bones.utils.NetLog;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.view.View;

public class HistEntry {

	public long   id;
	public long   paramId;
	public double value;
	public Date   changed;
	public boolean checked;
	public View row;
	
	public HistEntry(ParamEntry e) {
		this.value = e.startVal;
		this.changed = e.startDate;
		this.paramId = e.paramId;
		this.id = -1;
		this.checked = false;
	}
	
	
	public HistEntry() {
		this.id = -1;
		this.paramId = -1;
		this.value = 0;
		this.changed = new Date();
		this.checked = false;
	}

	public HistEntry(Cursor c ) {
		this.id = c.getLong(0);
		this.paramId = c.getLong(1);
		this.value = c.getDouble(2);
		this.changed = new Date(c.getLong(3));
		this.checked = false;
	}

	public HistEntry(HistEntry e) {
		this.value = e.value;
		this.changed = new Date();
		this.paramId = e.paramId;
		this.id = -1;
		this.checked = false;
	}
	
	public void Delete() {
		SQLiteStatement stm = ProfileList.getInstance().getDB().compileStatement("delete from hist where _id = ?");
		stm.clearBindings();
		stm.bindLong(1,this.id);
		stm.execute();
	}
	
	public void Save() {
		SQLiteStatement stm;
		SQLiteDatabase db = ProfileList.getInstance().getDB();
		if ( id == -1 )
			stm = db.compileStatement("insert into hist ( paramId,value,changed ) values(?,?,?)");
		else
			stm = db.compileStatement("update hist set value = ?,changed = ? where _id = ?");
	
		stm.clearBindings();
		if ( id == -1 ) {
			stm.bindLong(1, paramId);
			stm.bindDouble(2, value);
			stm.bindLong(3, changed.getTime());
		} else {
			stm.bindDouble(1, value);
			stm.bindLong(2, changed.getTime());
			stm.bindLong(3, id);
		}
	//7360
		if ( id == -1 ) {
			id = stm.executeInsert();
			NetLog.v("Hist for %d inserted - %d ( %f,%s )",this.paramId,id,value,changed.toGMTString());
		} else {
			//int row = stm.executeUpdateDelete();
			stm.execute();
			NetLog.v("Hist %d for %d updated to %f,%s",id,this.paramId,value,changed.toGMTString());
		}
	} // Save
	
	public String toString() {
		return String.format("HistEntry { id: %d,  paramId: %d,value %f,changed %s",id,paramId,value,changed.toGMTString());
	}
}


