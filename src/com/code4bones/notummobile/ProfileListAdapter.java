package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;
import com.code4bones.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileListAdapter extends ArrayAdapter<ProfileEntry> {

	static class ProfileHolder {
		TextView  name;
		View	  view;
		TipsView  tipsView;
		public static Bitmap imgPlus = null;
		
		ProfileHolder(Context c,View row,ProfileEntry profile) {
			view = row;
			name = (TextView)row.findViewById(R.id.tvParamName);
			tipsView = new TipsView(c);
			FrameLayout fl = (FrameLayout)view.findViewById(R.id.layoutTipsView);
			fl.addView(tipsView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
			tipsView.setFontSize(15);
			
			if ( imgPlus == null ) {
				imgPlus = BitmapFactory.decodeResource(c.getResources(), R.drawable.circle_plus);
			}
 			
			row.setTag(this);
		}
		
		
		public void update(ProfileEntry entry) {
			name.setText(entry.profileName);

			tipsView.reset();
			
			if ( entry.profileId == -1 ) {
				tipsView.setFontSize(20);
				tipsView.setImageFrame(false);
				tipsView.setImage(ProfileHolder.imgPlus);
				TipsView.Tip tip = tipsView.addTip("Создайте новый профиль", 0);
				tip.setBadgeColors(TipsView.defaultBlue());
				tipsView.Adjust();
				return;
			}
			tipsView.setImageFrame(true);
			tipsView.setImage(entry.profileIcon);
			tipsView.setFontSize(17);
			
			if ( entry.populateParams(ProfileList.getInstance().getDB()) > 0 ) {
				for ( ParamEntry param : entry.mParams ) {
					int pr = param.isAlerted()?0:1;
					TipsView.Tip tip = tipsView.addTip(param.name, pr);
					if ( pr == 0 ) {
						tip.setBadgeColors(TipsView.defaultRed());
					} else
						tip.setBadgeColors(TipsView.defaultGreen());
				}
				tipsView.Adjust();
			} else {
				tipsView.setFontSize(23);
				TipsView.Tip tip = tipsView.addTip("Нет данных", 0);
				tip.setBadgeColors(TipsView.defaultRed());
				tipsView.Adjust();
			}
		} // update
	} // HOLDER
	
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
			holder = new ProfileHolder(mContext,row,entry);
			row.setFocusable(false);
		} else { // convertView is alerady assigned
			holder = (ProfileHolder)row.getTag();
		}
		
		holder.update(entry);
		
		return row;
	}
}
