package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;
import com.code4bones.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileListAdapter extends ArrayAdapter<ProfileEntry> {

	static class ProfileHolder {
		ImageView icon;
		TextView  name;
		//TagsView  tags;
		FlowLayout tags;
		View	  view;
		
		//BadgeView badge;
	
		ProfileHolder(View row,ProfileEntry profile) {
			view = row;
			icon = (ImageView)row.findViewById(R.id.ivProfileIcon);
			name = (TextView)row.findViewById(R.id.tvParamName);
			tags = (FlowLayout)row.findViewById(R.id.cvTagsView);
			icon.setBackgroundResource(R.drawable.image_border);
			row.setTag(this);
		}
		
		public TextView createLabel(String msg,int shape) {
			TextView t = new TextView(view.getContext());
            t.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			t.setText(msg);
            t.setShadowLayer(6, 6, 6, Color.BLACK);
            t.setTextColor(Color.WHITE);
            t.setBackgroundResource(shape);
            t.setSingleLine(true);
            return t;
		}
		
		public void update(ProfileEntry entry) {
			icon.setImageBitmap(entry.profileIcon);
			
			name.setText(entry.profileName);
			tags.removeAllViews();
			
			if ( entry.populateParams(ProfileList.getInstance().getDB()) > 0 ) {
				for ( ParamEntry param : entry.mParams ) {
					
					int shape = R.drawable.profile_badge_shape;
					if ( param.isAlerted() )
						shape = R.drawable.profile_badge_shape_notdata;
					TextView t = createLabel(param.name,shape);
		            tags.addView(t, new TagsView.LayoutParams(2, 2));			
		            }
			} else {
				TextView t = createLabel("Нет данных",R.drawable.profile_badge_shape_notdata);
				tags.addView(t,new TagsView.LayoutParams(2,2));
			}
		}
	}
	
	private Context mContext = null;
	private ProfileEntry mEntry[] = null;
	
	public ProfileListAdapter(Context context,ProfileEntry[] data) {
		super(context,R.layout.profile_item_row,data);
		this.mContext = context;
		this.mEntry = data;
		
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ProfileHolder holder = null;
		ProfileEntry entry = this.mEntry[position];
		if ( row == null ) {
			LayoutInflater inf = ((Activity)mContext).getLayoutInflater();
			row = inf.inflate(R.layout.profile_item_row,parent,false);
			holder = new ProfileHolder(row,entry);
		} else { // convertView is alerady assigned
			holder = (ProfileHolder)row.getTag();
		}
		
		holder.update(entry);
		
		return row;
	}
}
