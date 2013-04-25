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
import android.view.MenuItem;
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
		this.profileName.setBackgroundResource(R.drawable.edit_text_shape);
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
				SelectPhoto();
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
			if ( v.getId() == R.id.bnProfileDelete ) {
				DeleteProfile();
			} else if ( v.getId() == R.id.bnProfileSave ) {
				SaveProfile();
			} else
				finish();
		}
		
	}; 

	public void DeleteProfile() {
		Intent i = new Intent();
		mProfile.Delete(ProfileList.getInstance().getDB());
		setResult(RESULT_OK,i);
		finish();
	}
	
	public void SaveProfile() {
		Intent i = new Intent();
		if ( mProfile == null )
			mProfile = new ProfileEntry();
		if ( mProfile.collectData(ProfileActivity.this) ) {
			i.putExtra(ProfileEntry.PROFILE_ENTRY, ProfileActivity.this.mProfile);
			setResult(RESULT_OK,i);
			finish();
		} else {
			NetLog.Toast(this,"Наименование не указанно");
		}
	}
	
	public void SelectPhoto() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent,SELECT_PHOTO);
	}
	
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
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		MenuItem item = menu.findItem(R.id.itemProfileDelete); 
		item.setVisible(mProfile != null);
	    return true;
	}	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.itemProfileCancel:
			finish();
			break;
		case R.id.itemProfileSave:
			SaveProfile();
			break;
		case R.id.itemProfileDelete:
			DeleteProfile();
			break;
		case R.id.itemProfileSelectPhoto:
			SelectPhoto();
			break;
		}
		return false;
	}
	
}
