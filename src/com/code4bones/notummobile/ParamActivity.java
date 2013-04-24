package com.code4bones.notummobile;

import com.code4bones.utils.NetLog;

import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ParamActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_param);
		
		//HorizontalListView listview = (HorizontalListView) findViewById(R.id.hzListView);  
        //listview.setAdapter(mAdapter);  		
		//ChartView chart = (ChartView)findViewById(R.id.horizontalScrollView1);
		//NetLog.v("Chart view %s",chart);
		//chart.setData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.param, menu);
		return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.paramAddNew:
			addNewParameter();
			break;
		}
		
		return true;
	}
	
	
	public void addNewParameter() {
		
	}
	
	private static String[] dataObjects = new String[]{ "Text #1",  
        "Text #2",  
        "Text #3", 
        "Text #2",  
        "Text #3", 
        "Text #2",  
        "Text #3", 
        "Text #2",  
        "Text #3", 
        "Text #2",  
        "Text #3", 
        "Text #2",  
        "Text #3" 
        
	
	};   
      
    private BaseAdapter mAdapter = new BaseAdapter() {  
  
        @Override  
        public int getCount() {  
            return dataObjects.length;  
        }  
  
        @Override  
        public Object getItem(int position) {  
            return null;  
        }  
  
        @Override  
        public long getItemId(int position) {  
            return 0;  
        }  
  
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
            View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.param_list_item, null);  
            TextView title = (TextView) retval.findViewById(R.id.paramName);  
            title.setText(dataObjects[position]);  
              
            return retval;  
        }
    };

}
