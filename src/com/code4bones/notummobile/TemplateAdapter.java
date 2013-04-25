package com.code4bones.notummobile;

import java.util.ArrayList;

import com.code4bones.notummobile.ProfileListAdapter.ProfileHolder;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class TemplateAdapter extends ArrayAdapter<ParamEntry> {

	public ArrayList<ParamEntry> mParam = null;
	public Context mContext;
	
	public TemplateAdapter(Context context,ArrayList<ParamEntry> params) {
		super(context, R.layout.template_item);
		mContext = context;
		mParam = params;
		
	}

	@Override 
	public int getCount() {
		return mParam.size();
	}
	
	@Override
	public ParamEntry getItem(int pos) {
		return mParam.get(pos);
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if ( row == null ) {
			LayoutInflater inf = ((Activity)mContext).getLayoutInflater();
			row = inf.inflate(R.layout.template_item,parent,false);
		}
		
		ParamEntry e = getItem(position);
		TextView tv = (TextView)row.findViewById(R.id.tvTemplateName);
		tv.setText(e.name);
		
		return row;
	}
}
