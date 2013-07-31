package com.code4bones.notummobile;

import java.util.ArrayList;
import java.util.Calendar;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.widget.ArrayAdapter;

public class GraphSpinnerAdapter extends ArrayAdapter<GraphSpinnerEntry> {

	public GraphSpinnerEntry[] mEntry;
	public ArrayList<GraphSpinnerEntry> mList;
	
	public GraphSpinnerAdapter(Context context, int textViewResourceId,GraphSpinnerEntry[] values) {
		super(context,textViewResourceId,values);
		// TODO Auto-generated constructor stub
		mEntry = values;
	}

	public GraphSpinnerAdapter(Context context, int textViewResourceId,ArrayList<GraphSpinnerEntry> values) {
		super(context,textViewResourceId,values);
		// TODO Auto-generated constructor stub
		mList= values;
	}
	
	public int Add(GraphSpinnerEntry item) {
		GraphSpinnerEntry check = find(item);
		NetLog.v("CHECK: %s",check);
		if ( check != null )
			return mList.indexOf(check);

		mList.add(item);
		return getCount();
	}
	
	public int getCount(){
		return mList.size();//mEntry.length;
	}

	public GraphSpinnerEntry find(GraphSpinnerEntry item) {
		for ( GraphSpinnerEntry e : mList ) {
			if ( e.mType != GraphSpinnerEntry.DYNA )
				continue;
			if ( compare(item.mTo,e.mTo) && compare(item.mFrom,e.mFrom) )
				return e;
		}
		return null;
	}
	
	public boolean compare(Calendar c1,Calendar c2) {
		return c1.getTime().getTime()/1000 == c2.getTime().getTime()/1000;
	}
	
    public GraphSpinnerEntry getItem(int position){
       return mList.get(position);//mEntry[position];
    }

    public long getItemId(int position){
       return position;
    }	

}
