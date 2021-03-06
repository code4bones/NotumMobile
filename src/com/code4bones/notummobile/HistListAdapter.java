package com.code4bones.notummobile;

import java.util.ArrayList;

import com.code4bones.notummobile.ProfileListAdapter.ProfileHolder;
import com.code4bones.utils.NetLog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HistListAdapter extends ArrayAdapter<HistEntry> {

	HistEntry mList[] = null;
	Context mContext = null;
	ParamEntry mParamEntry = null;
	OnClickListener mOnClick = null;
	public ArrayList<HistEntry> mRemove = new ArrayList<HistEntry>();
	
	//OnCheckedChangeListener mOnCheck = null;
	
	static class Holder {
		
		TextView tvDate;
		ImageButton ibDate;
		TextView tvValue;
		TextView tvDiff;
		TextView tvStart;
		TextView tvEnd;
		ImageButton ibDec;
		ImageButton ibInc;
		ProgressBar pbProg;
		CheckBox    chkDelete;
	
		Holder(View row,HistEntry e) {
			row.setTag(this);
			
			tvDate = (TextView)row.findViewById(R.id.tvHistDate);
			ibDate = (ImageButton)row.findViewById(R.id.ibHistSelectDate);
			tvValue = (TextView)row.findViewById(R.id.tvHistValue);
			tvDiff  = (TextView)row.findViewById(R.id.tvHistDiff);
			tvStart = (TextView)row.findViewById(R.id.tvHistStartValue);
			tvEnd   = (TextView)row.findViewById(R.id.tvHistTargetValue);
			ibDec   = (ImageButton)row.findViewById(R.id.ibHistDecValue);
			ibInc   = (ImageButton)row.findViewById(R.id.ibHistIncValue);
			pbProg  = (ProgressBar)row.findViewById(R.id.pbHistProgress);
			chkDelete = (CheckBox)row.findViewById(R.id.chkDelete);
			
			tvDiff.setBackgroundResource(R.drawable.diff_label_shape);
		
			ibDate.setTag(e);
			ibDec.setTag(e);
			ibInc.setTag(e);
			chkDelete.setTag(e);
		}
		
		public void update(ParamEntry pe,HistEntry e,HistEntry p) {
			double diff = e.value - p.value;
			//NetLog.v("update(%s)",e);
			tvDate.setText(e.changed.toGMTString());
			tvValue.setText(String.valueOf(e.value));
			tvDiff.setText((diff>0?"+":"")+String.valueOf(diff));
			tvStart.setText(String.valueOf(pe.startVal));
			tvEnd.setText(String.valueOf(pe.targetVal));
			pbProg.setMax(100);
			chkDelete.setChecked(e.checked);
			
			double min = pe.startVal; // 2
			double max = pe.targetVal - min; // 10
			double cur  = e.value - min; // 2
			int val = (int)(100 / (max / cur));
			pbProg.setProgress(val);
			
			if ( diff < 0 ) {
				tvDiff.setTextColor(Color.YELLOW);
				tvDiff.setBackgroundResource(R.drawable.diff_label_shape_red);
			} else {
				tvDiff.setTextColor(Color.BLUE);
				tvDiff.setBackgroundResource(R.drawable.diff_label_shape);
			}
		}
	};
	
	
	
	public OnCheckedChangeListener mOnCheck = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			HistEntry entry = (HistEntry)buttonView.getTag();
			entry.checked = isChecked;
			if ( isChecked ) {
				mRemove.add(entry);
			} else {
				mRemove.remove(entry);
			}
		}
	};
	
	public HistListAdapter(Context context, ParamEntry paramEntry,OnClickListener onClick) {
		super(context, R.layout.hist_item_row, paramEntry.toArray());
		mContext = context;
		mParamEntry = paramEntry;
		mOnClick = onClick;
		mList = this.mParamEntry.toArray();
	}
	
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;
		HistEntry entry = this.mList[position];
		if ( row == null ) {
			LayoutInflater inf = ((Activity)mContext).getLayoutInflater();
			row = inf.inflate(R.layout.hist_item_row,parent,false);
			holder = new Holder(row,entry);
			holder.ibDate.setOnClickListener(mOnClick);
			holder.ibDec.setOnClickListener(mOnClick);
			holder.ibInc.setOnClickListener(mOnClick);
			holder.chkDelete.setOnCheckedChangeListener(mOnCheck);
		} else { // convertView is alerady assigned
			holder = (Holder)row.getTag();
		}
		
		HistEntry prevHist = null;
		if ( position < mList.length-1 )
			prevHist = mList[position+1];
		else
			prevHist = entry;
			
		holder.update(mParamEntry,entry,prevHist);
		
		return row;
	}
	

}
