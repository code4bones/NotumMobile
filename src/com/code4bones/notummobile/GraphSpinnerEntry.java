package com.code4bones.notummobile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.code4bones.utils.NetLog;

public class GraphSpinnerEntry {
	
	public static int ALL = 1;
	public static int WEEK = 2;
	public static int MONTH = 3;
	public static int YEAR = 4;
	public static int PERIOD = 5;
	public static int DYNA = 6;
	
	
	//public Calendar mCal = Calendar.getInstance();
	public Calendar mFrom  = Calendar.getInstance();
	public Calendar mTo    = Calendar.getInstance();
	final SimpleDateFormat mFmtWeek = new SimpleDateFormat("E,dd MMMM");
	final SimpleDateFormat mFmtMonth = new SimpleDateFormat("dd MMMM");
	final SimpleDateFormat mFmtYear = new SimpleDateFormat("dd.MM.yyyy");
	public String   mName;
	public int      mType = ALL;
	
	public GraphSpinnerEntry() {
		// TODO Auto-generated constructor stub
	
	}

	
	public GraphSpinnerEntry(String name,int type) {
		mName = name;
		mType = type;
	}

	public GraphSpinnerEntry(int type) {
		
	}
	
	public String toString() {
		String str = "";
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR,0);
		now.set(Calendar.MINUTE,0);
		now.set(Calendar.SECOND,0);
		
		if ( mType == WEEK ) {
			now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			mFrom.setTime(now.getTime());
			str = "Неделя | ";
			str += mFmtWeek.format(now.getTime());
			now.add(Calendar.DATE, 6);
			mTo.setTime(now.getTime());
			str += " ... ";
			str += mFmtWeek.format(now.getTime());
		} else if ( mType == MONTH ){
			now.set(Calendar.DAY_OF_MONTH, 1);
			mFrom.setTime(now.getTime());
			str = "Месяц | ";
			str += mFmtMonth.format(now.getTime());
			now.add(Calendar.MONTH, 1);
			now.add(Calendar.DAY_OF_MONTH, -1);
			mTo.setTime(now.getTime());
			str += " ... ";
			str += mFmtMonth.format(now.getTime());
		} else if ( mType == YEAR ) {
			now.set(Calendar.DAY_OF_MONTH, 1);
			now.set(Calendar.MONTH, Calendar.JANUARY);
			mFrom.setTime(now.getTime());
			str = "Год | ";
			str += mFmtYear.format(now.getTime());
			now.add(Calendar.YEAR, 1);
			now.add(Calendar.DATE, -1);
			mTo.setTime(now.getTime());
			str += " ... ";
			str += mFmtYear.format(now.getTime());
		} else if ( mType == PERIOD ){ // Period
			str = "Выбрать...";
		} else if ( mType == DYNA ){
			str += mFmtYear.format(mFrom.getTime()) + "..." + mFmtYear.format(mTo.getTime());
		}  else {
			str = "Показать все";
		}
			
		return str;
	}
	
	
	public boolean compare(Date dt) {
		long from = mFrom.getTimeInMillis() / 1000;
		long to = mTo.getTimeInMillis() / 1000;
		dt.setMinutes(0);dt.setHours(0);dt.setSeconds(0);
		long e = dt.getTime() / 1000;
		boolean fOk = (from <= e && e <= to);
		return fOk;
	}
	
}
