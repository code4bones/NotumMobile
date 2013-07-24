package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		this.setTitle("Установка Пароля");
		
		final CheckBox checkBox = (CheckBox)this.findViewById(R.id.cbUsePassword);
		final EditText editPassword1 = (EditText)this.findViewById(R.id.etSettingsPassword1);
		final EditText editPassword2 = (EditText)this.findViewById(R.id.etSettingsPassword2);
		
		final Button btnSave = (Button)this.findViewById(R.id.bnSave);
		final AppConfig cfg = new AppConfig(this);
		
		
		if (cfg.needPassword() ) {
			checkBox.setChecked(true);		
			editPassword1.setText(cfg.getPassword(AppConfig.PASSWD_USER));
			editPassword2.setText(cfg.getPassword(AppConfig.PASSWD_USER));
		}
		
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String password1 = editPassword1.getText().toString();
				String password2 = editPassword2.getText().toString();
				
				if ( checkBox.isChecked() ) {
					if ( !validate(password1,password2) ) {
						NetLog.MsgBox(SettingsActivity.this, "Ошибка", "Длина должна быть минимум 4-е символа и пароли должны совпадать...");
						editPassword1.requestFocus();
					} else {
						cfg.setPasswords(null, password1);
						finish();
					}
				} else { // not checked
						cfg.clearPassword();
						finish();
				}
			}
		});
		
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if ( !isChecked ) {
					editPassword1.setText("");
					editPassword2.setText("");
				}
			}
		});
	}

	public boolean validate(String val1,String val2) {
		return val1.length() >= 4 && val1.equals(val2);
	}
	

}
