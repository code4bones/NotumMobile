package com.code4bones.notummobile;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.code4bones.utils.NetLog;
import com.code4bones.utils.Utils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ProfileActivity extends Activity {

	public static final int SELECT_PHOTO  = 10;
	public static final int EDIT_PROFILE = 1;
	public static final int NEW_PROFILE = 2;
	
	public EditText profileName;
	public ImageButton profileIcon;
	private String imagePath;
	
	public ProfileEntry mProfile; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		this.setTitle("Изменение Профиля");
		
		Intent data = this.getIntent();
		mProfile = data.getParcelableExtra(ProfileEntry.PROFILE_ENTRY);
		
		this.profileName = (EditText)this.findViewById(R.id.etParamName);
		this.profileIcon = (ImageButton)this.findViewById(R.id.ibParamIcon);
		
		if ( mProfile != null  ) {
			NetLog.v("Edit %d",mProfile.profileId);
			this.profileIcon.setImageBitmap(mProfile.profileIcon);
			this.profileName.setText(mProfile.profileName);
		} else {
			this.profileIcon.setImageResource(R.drawable.camera);
			this.profileName.setText("Новый профиль...");
		}
		
		// PICK A PHOTO
		this.profileIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(intent,SELECT_PHOTO);
			}
		});
		
		// SAVE A PROFILE
		this.findViewById(R.id.bnProfileCancel).setOnClickListener(mOnClick);
		this.findViewById(R.id.bnProfileSave).setOnClickListener(mOnClick);
		Button bnDelete = (Button)this.findViewById(R.id.bnProfileDelete);
		bnDelete.setOnClickListener( mOnClick );
		bnDelete.setEnabled(this.mProfile != null);
	}

	private OnClickListener mOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent();
			if ( v.getId() == R.id.bnProfileDelete ) {
				mProfile.Delete(ProfileList.getInstance().getDB());
				setResult(RESULT_OK,i);
			} else if ( v.getId() == R.id.bnProfileSave ) {
				if ( ProfileActivity.this.mProfile == null )
					ProfileActivity.this.mProfile = new ProfileEntry();
				
				ProfileActivity.this.mProfile.collectData(ProfileActivity.this);
				i.putExtra(ProfileEntry.PROFILE_ENTRY, ProfileActivity.this.mProfile);
				setResult(RESULT_OK,i);
			}
			finish();
		}
		
	}; 
	
	@Override
	public void onActivityResult(int reqCode,int resCode,Intent data) {
		if ( reqCode != SELECT_PHOTO || resCode != RESULT_OK )
			return;
		Uri imageUri = data.getData();
		imagePath = imageUri.toString();
		try {
			NetLog.v("Somethig selected %s",imagePath);
			InputStream is = getContentResolver().openInputStream(imageUri);
			Bitmap image = BitmapFactory.decodeStream(is);
			Bitmap scaled = Bitmap.createScaledBitmap(image, 64, 64, false);
			profileIcon.setImageBitmap(Utils.getRoundedCornerBitmap(scaled));
		} catch (FileNotFoundException e) {
			NetLog.v("File Not Found");
			e.printStackTrace();
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

}
