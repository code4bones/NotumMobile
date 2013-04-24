package com.code4bones.notummobile;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.code4bones.utils.NetLog;

import android.net.Uri;
import android.os.Bundle;
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
	
	private EditText profileName;
	private ImageButton profileIcon;
	private String imagePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		this.profileName = (EditText)this.findViewById(R.id.etParamName);
		this.profileIcon = (ImageButton)this.findViewById(R.id.ibParamIcon);
		
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
		Button save = (Button)this.findViewById(R.id.bnStepDec);
		save.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent();
				i.putExtra(ProfileEntry.PROFILE_NAME, profileName.getText().toString());
				i.putExtra(ProfileEntry.PROFILE_ICON, imagePath);
				setResult(RESULT_OK,i);
				finish();
			}
			
		});
	}

	@Override
	public void onActivityResult(int reqCode,int resCode,Intent data) {
		if ( reqCode != SELECT_PHOTO )
			return;
		Uri imageUri = data.getData();
		imagePath = imageUri.toString();
		try {
			NetLog.v("Somethig selected %s",imagePath);

			InputStream is = getContentResolver().openInputStream(imageUri);
			Bitmap image = BitmapFactory.decodeStream(is);
			Bitmap scaled = Bitmap.createScaledBitmap(image, 64, 64, false);
			profileIcon.setImageBitmap(scaled);
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
