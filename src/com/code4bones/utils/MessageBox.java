package com.code4bones.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;



public class MessageBox extends AlertDialog.Builder implements DialogInterface {

	public static final int MB_OK     = 1;
	public static final int MB_OKCANCEL = 2;
	public static final int MB_YESNO = 3;
	public static final int MB_YESNOCANCEL = 4;
	
	public DialogInterface.OnClickListener mOnClick = null;
	
	public MessageBox(Context context,int flsgs) {
		super(context);
	    //this.setTitle(sTitle); 
	    //this.setMessage(msg); 
	    //dlgAlert.setPositiveButton("OK", onClick);
	    //dlgAlert.setNegativeButton("Отмена", onClick);
	    //dlgAlert.setCancelable(true);
	    //dlgAlert.create().show();
	}

	
	public static void Show(String sTitle,String sMessage,Context ctx) {
		MessageBox.Show(sTitle,sMessage,ctx,MessageBox.MB_OK,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}
	
	public static void Show(String sTitle,String sMessage,Context ctx,int flags,DialogInterface.OnClickListener onClick) {
		MessageBox msg = new MessageBox(ctx,flags);
		msg.mOnClick = onClick;
		msg.setTitle(sTitle);
		msg.setMessage(sMessage);
		msg.setCancelable(true);
		switch ( flags ) {
		case MessageBox.MB_OK:
			msg.setPositiveButton("Ok", msg.mOnClick);
			break;
		case MessageBox.MB_YESNO:
		case MessageBox.MB_OKCANCEL:
			msg.setPositiveButton(flags == MessageBox.MB_OKCANCEL?"Ok":"Да", msg.mOnClick);
			msg.setNegativeButton(flags == MessageBox.MB_OKCANCEL?"Отмена":"Нет", msg.mOnClick);
			break;
		case MessageBox.MB_YESNOCANCEL:
			msg.setPositiveButton("Да",msg.mOnClick);
			msg.setNegativeButton("Нет", msg.mOnClick);
			msg.setNeutralButton("Отмена", msg.mOnClick);
			break;
		}
		msg.create().show();
	}
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		
	}

}
