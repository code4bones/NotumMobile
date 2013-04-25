package com.code4bones.notummobile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.code4bones.utils.NetLog;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class NewParamActivity extends Activity implements OnDateSetListener {

	public static final int SELECT_PHOTO = 1;
	public static final int SELECT_STARTDATE = 2;
	public static final int SELECT_ENDDATE = 3;
	
	
	public static final int ADD = 0;
	public static final int EDIT = 1;
	public static final int DELETE = 2;
	public static final int HIST   = 3;
	
	public static final String PARAM_ENTRY = "paramEntry";
	public static final String NEW_LIST    = "newList";
	
	public NewParamActivity mActivity = null;
	public ParamEntry mParamEntry = null;
	
	public String imagePath;
	public ImageButton ibIcon;
	public EditText    etName;
	public TextView    tvStartDate;
	public TextView	   tvEndDate;
	public TextView    tvStep;
	public EditText    etMeasure;
	public EditText    etIncVal;
	public EditText	   etCurrentVal;
	public EditText    etTargetVal;
	public DatePickerDialog mDatePicker;
	public ProfileEntry mProfile;
	public boolean     mFirstParam = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_param);
	
		mActivity = this;
		Intent data = this.getIntent();
		mFirstParam = data.getBooleanExtra(NewParamActivity.NEW_LIST, false);
		mParamEntry = data.getParcelableExtra(NewParamActivity.PARAM_ENTRY);

		ProfileList pl = ProfileList.getInstance();
		mProfile = pl.getCurrentProfile();

		this.setTitle("Редактирование Параметра");
		
		this.findViewById(R.id.bnDelete).setEnabled(mParamEntry != null);
		if ( mParamEntry == null ) {
			//ProfileList pl = ProfileList.getInstance();
			mParamEntry = new ParamEntry(this,mProfile.profileId);
		}
		mParamEntry.mContext = this;
		
		this.findViewById(R.id.bnCancel).setOnClickListener(mOnClick);
		this.findViewById(R.id.bnSave).setOnClickListener(mOnClick);
		this.findViewById(R.id.bnDelete).setOnClickListener(mOnClick);
		this.findViewById(R.id.ibParamStartDate).setOnClickListener(mOnClick);
		this.findViewById(R.id.ibParamEndDate).setOnClickListener(mOnClick);
		this.findViewById(R.id.bnProfileSave).setOnClickListener(mOnClick);
		this.findViewById(R.id.bnStepInc).setOnClickListener(mOnClick);
		this.findViewById(R.id.ibParamTemplate).setOnClickListener(mOnClick);
		
		this.ibIcon = (ImageButton)this.findViewById(R.id.ibParamIcon); 
		this.etName = (EditText)this.findViewById(R.id.etParamName);
		
		this.tvStartDate = (TextView)this.findViewById(R.id.tvStartDate);
		this.tvEndDate = (TextView)this.findViewById(R.id.tvEndDate);
		this.tvStep = (TextView)this.findViewById(R.id.tvStep);
		this.etMeasure = (EditText)this.findViewById(R.id.etMeasure);
		this.etIncVal = ( EditText )this.findViewById(R.id.etIncVal);
		this.etCurrentVal = (EditText)this.findViewById(R.id.etParamCurrentValue);
		this.etTargetVal = (EditText)this.findViewById(R.id.etTargetValue);
		
		this.etName.setBackgroundResource(R.drawable.edit_text_shape);
		this.etName.setTag("Наименование наблюдения");
		this.etIncVal.setBackgroundResource(R.drawable.edit_text_shape);
		this.etIncVal.setTag("Единица Прироста");
		this.etCurrentVal.setBackgroundResource(R.drawable.edit_text_shape);
		this.etCurrentVal.setTag("Текущее значение");
		this.etTargetVal.setBackgroundResource(R.drawable.edit_text_shape);
		this.etTargetVal.setTag("Желаемое значение");
		this.etMeasure.setBackgroundResource(R.drawable.edit_text_shape);
		this.etMeasure.setTag("Единица измерения");
		//this.etMeasure.setLines(1);
		
		ibIcon.setOnClickListener(mOnClick);
	
		
		setupControlValues(mParamEntry);
	}

	public void setupControlValues(ParamEntry e) {
		if ( e.image != null )
			this.ibIcon.setImageBitmap(e.image);
			
		this.etName.setText(e.name);
		this.tvStartDate.setText(e.startDate.toGMTString());
		this.tvStartDate.setTag(e.startDate);
		this.tvEndDate.setTag(e.endDate);
		this.updateParamEndDate(e);
		this.updateParamStep(0);
		
		this.etMeasure.setText(e.measure);
		this.etIncVal.setText(String.valueOf(e.incVal));
		this.etCurrentVal.setText(String.valueOf(e.startVal));
		this.etTargetVal.setText(String.valueOf(e.targetVal));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.new_param, menu);
		return true;
	}

	
	@Override
	public void onActivityResult(int reqCode,int resCode,Intent data) {
		
		if ( resCode != RESULT_OK )
			return;
		
		if ( reqCode == SELECT_PHOTO ) {
			Uri imageUri = data.getData();
			imagePath = imageUri.toString();
			try {
				NetLog.v("Somethig selected %s",imagePath);
	
				InputStream is = getContentResolver().openInputStream(imageUri);
				Bitmap image = BitmapFactory.decodeStream(is);
				Bitmap scaled = Bitmap.createScaledBitmap(image, 64, 64, false);
				ibIcon.setImageBitmap(scaled);
				mParamEntry.setImage(imagePath);
			} catch (FileNotFoundException e) {
				NetLog.v("File Not Found");
				e.printStackTrace();
			}
		} 
	}
	
	
	public void deleteOrSaveParam(boolean doSave,boolean fNewFlag) {
		Intent res = new Intent();

		ProfileList pl = ProfileList.getInstance();
		if ( doSave )
			pl.SaveProfileParam(mParamEntry);
		else
			pl.DeleteProfileParam(mParamEntry);
		
		res.putExtra(NewParamActivity.NEW_LIST, fNewFlag);
		res.putExtra(NewParamActivity.PARAM_ENTRY, doSave?mParamEntry:null);
		NewParamActivity.this.setResult(RESULT_OK, res);
		finish();
	}
	
	public TextView mDateView = null;
	
	public void selectParamDate(int id) {
		mDateView = (TextView)this.findViewById(id == R.id.ibParamStartDate?R.id.tvStartDate:R.id.tvEndDate);
	    Date d = (Date)mDateView.getTag();
		mDatePicker = new DatePickerDialog(this,this,1900+d.getYear(),d.getMonth(),d.getDate());
		//TODO 
		mDateView = (TextView)this.findViewById(id == R.id.ibParamStartDate?R.id.tvStartDate:R.id.tvEndDate);
		mDatePicker.show();
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		
		Calendar c = Calendar.getInstance();
		c.set(year, monthOfYear, dayOfMonth);
		Date selDate = c.getTime();
	
		mDateView.setText(selDate.toGMTString());
		mDateView.setTag(selDate);
		
		if ( mDateView.getId() == R.id.tvStartDate )
			this.mParamEntry.startDate = selDate;
		else {
			this.mParamEntry.endDate = selDate;
			updateParamEndDate(this.mParamEntry);
		}
	}
	
	public void updateParamEndDate(ParamEntry e) {
		if ( e.endDate != null )
			this.tvEndDate.setText(e.endDate.toGMTString());
		else
			this.tvEndDate.setText("Не ограниченно");
	}
	
	public void updateParamStep(int id) {
		
		if ( id != -1 ) {
			boolean fInc = id == R.id.bnStepInc;
			if ( mParamEntry.stepVal > 0 && !fInc)
				mParamEntry.stepVal--;
			else if ( fInc )
				mParamEntry.stepVal++;
		}
		
		if ( mParamEntry.stepVal > 0 )
			this.tvStep.setText(String.valueOf(mParamEntry.stepVal));
		else
			this.tvStep.setText("Без периода");
		
	}
	
	public void selectTemplate() {
       
		final ArrayList<ParamEntry> params = new ArrayList<ParamEntry>();
		params.add(ParamEntry.makeTemplate(mProfile.profileId, "Рост", "См."));
		params.add(ParamEntry.makeTemplate(mProfile.profileId, "Вес", "Кг."));
		params.add(ParamEntry.makeTemplate(mProfile.profileId, "Обьем талии", "См."));
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setAdapter(new TemplateAdapter(this,params), new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setupControlValues(params.get(which));
			}
		});
		dlg.setTitle("Шаблоны");
		dlg.create().show();
	}
	
	// SAVE or CANCEL EVENTS
	private OnClickListener mOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch ( v.getId() ) {
			case R.id.bnSave:
				if ( mParamEntry.collectData(NewParamActivity.this) )
					deleteOrSaveParam(true,NewParamActivity.this.mFirstParam);
				break;
			case R.id.bnDelete:
				deleteOrSaveParam(false,true);
				break;
			case R.id.bnCancel:
				finish();
				break;
			case R.id.ibParamIcon:
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(intent,1);
				break;
			case R.id.ibParamStartDate:
			case R.id.ibParamEndDate:
				selectParamDate(v.getId());
				break;
			case R.id.bnProfileSave:
			case R.id.bnStepInc:
				updateParamStep(v.getId());
				break;
			case R.id.ibParamTemplate:
				selectTemplate();
				break;
			}
		}
	};

	
	
		
		
}
