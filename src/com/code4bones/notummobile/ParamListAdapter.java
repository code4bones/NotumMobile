package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;
import com.code4bones.utils.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class ParamListAdapter extends BaseAdapter {  
	  
	public ParamEntry mData[];
	
	
	ParamListAdapter(ParamEntry[] data) {
		mData = data;
	}
	
    @Override  
    public int getCount() {  
        return mData.length;  
    }  

    @Override  
    public Object getItem(int position) {  
        return mData[position];  
    }  

    @Override  
    public long getItemId(int position) {  
        return 0;  
    }  

    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        
    	Holder holder = null;
    	View retval = convertView; 
    			
    	if ( retval == null ) {
    		retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.param_list_item, null);  
    		holder = new Holder(retval);
    	} else {
    		holder = (Holder)retval.getTag();
    	}

    	ParamEntry entry = mData[position];
        holder.update(entry);
       // entry.mListItem = retval;
          
        return retval;  
    }
    
    static class Holder {
    	ImageView image;
    	TextView  text;
    	
    	Holder(View row) {
            text = (TextView) row.findViewById(R.id.paramName);  
            image = (ImageView)row.findViewById(R.id.paramLogo);
            row.setTag(this);
    	}
    	
    	public void update(ParamEntry entry) {
   	       image.setImageBitmap(Utils.getRoundedCornerBitmap(entry.image));
   	       text.setText(entry.name);  
           image.setBackgroundResource(R.drawable.image_border);
           text.setTag(entry);
    	}
    	
    }
    
};
