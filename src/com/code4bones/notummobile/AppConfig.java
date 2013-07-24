package com.code4bones.notummobile;

import java.util.Map;
import java.util.Random;

import com.code4bones.utils.NetLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AppConfig {

	private Context mContext = null;
	SharedPreferences mPrefs = null;
	public static final String PASSWD_MASTER = "master";
	public static final String PASSWD_USER   = "user";
	public static final String FIRST_RUN     = "firstrun";
	
	
	public AppConfig(Context ctx) {
		mContext = ctx;
		mPrefs = getConfig();
	}

	public void clearPassword() {
		SharedPreferences.Editor edit = mPrefs.edit();
		edit.remove(PASSWD_USER);
		edit.commit();
	}
	
	public String getPassword(String type) {
		return mPrefs.getString(type, null);
	}
	
	public void setPasswords(String master,String user) {
		SharedPreferences.Editor edit = mPrefs.edit();
		if ( master != null )
			edit.putString(PASSWD_MASTER, master);
		if ( user != null ) {
			edit.putString(PASSWD_USER, user);
		}
		edit.commit();
	}
	
	public void generateMasterPassword() {
		Random rnd = new Random();
		int val = 1000+rnd.nextInt(8000);
		String str = String.format("%d", val);
		setPasswords(str,null);
	}
	
	public int validatePassword(String type,String passwd) {
		return mPrefs.getString(type, "").equals(passwd)?1:0;
	}
	
	public boolean isFirstRun() {
		boolean first = !mPrefs.contains(PASSWD_MASTER); 
		return first;
	}
	
	
	public boolean needPassword() {
		return mPrefs.contains(PASSWD_USER);
	}
	
	public boolean showDialog(final Handler handler) {
		LayoutInflater li = LayoutInflater.from(mContext);
		View view = li.inflate(R.layout.dlg_password, null);
		
		final AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
		dlg.setView(view);
		dlg.setCancelable(false);
		//dlg.setTitle("Введите пароль");
		
		
		final EditText tvPassword = (EditText)view.findViewById(R.id.etPasswordValue);
		final AlertDialog passDlg = dlg.create();
		passDlg.show();

		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( v.getId() == R.id.btnPasswordCancel ) {
					handler.sendEmptyMessage(v.getId());
					passDlg.cancel();
			} else { // check
					String passwd = tvPassword.getText().toString();
					int res = validatePassword(PASSWD_USER,passwd);// + 
						      //validatePassword(PASSWD_USER,passwd);
					if ( res > 0 ) {
						handler.sendEmptyMessage(v.getId());
						passDlg.cancel();
					} else { // Wrong passwd
						tvPassword.setText("");
					}
				}
			}
		};

		view.findViewById(R.id.btnPasswordOk).setOnClickListener(listener);
		view.findViewById(R.id.btnPasswordCancel).setOnClickListener(listener);
		
		return false;
	}
	
	
	
	
	public SharedPreferences getConfig() {
		return mContext.getSharedPreferences("prefs", 1);
	}
}
