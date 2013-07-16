package com.mytwitter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TimelineActivity extends BaseActivity {
  Cursor cursor;  				// Cursor to all status updates in db
  ListView listTimeline;  		// ListView that displays the data 
  SimpleCursorAdapter adapter;
  
  static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER, StatusData.C_TEXT }; // columns the cursor is binding from
  static final int[] TO = { R.id.textCreatedAt, R.id.textUser, R.id.textText }; // where we're binding data to. Must match columns in FROM
  
  TimelineReceiver receiver;
  IntentFilter filter;			// specifies which intent actions we want to be notified about

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timeline);
    
    // Check whether preferences have been set (myTwitter was received in the parent BaseActivity's onCreate() method
    if (myTwitter.getPrefs().getString("username", null) == null) {	// if username is null, everything else should be null
    	startActivity(new Intent(this, PrefsActivity.class)); 		// if Preferences aren't set, go to PrefsActivity. Rest of onCreate() will still run
    	Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
    }
    	
    // Find your views
    listTimeline = (ListView) findViewById(R.id.listTimeline);  // get ListView from XML layout
    
    // Create the receiver and filter
    receiver = new TimelineReceiver();
    filter = new IntentFilter( UpdaterService.NEW_STATUS_INTENT );
  }

  protected void onResume() {
    super.onResume();

    // setup List
    this.setupList();
    
    // register the BroadcastReceiver inner class
    registerReceiver(receiver, filter);
  }

  protected void onPause() {
	 super.onPause();

	 // Unregister the receiver
	 unregisterReceiver(receiver); 
  }
  
  public void onDestroy() {
    super.onDestroy();
 
    // close the database
    myTwitter.getStatusData().close();
  }
  
  // responsible for fetching data and setting up the List and the Adapter
  private void setupList() {
	  // get the data from the database
	  cursor = myTwitter.getStatusData().getStatusUpdates();
	  startManagingCursor(cursor); // startManagingCursor(Cursor c); This method is deprecated. Use CursorLoader instead.
	  
	  // Setup Adapter: SimpleCursorAdapter constructor (deprecated): SimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
	  adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
	  adapter.setViewBinder(VIEW_BINDER);	// attach custom ViewBinder instance to vanilla adapter 
	  listTimeline.setAdapter(adapter); 	// tell Listview to use our created Adapter
  }
  
  // View binder constant to inject business logic for timestamp to relative time conversion
  static final ViewBinder VIEW_BINDER = new ViewBinder() { 

	// called for each data element that needs to be bound to a particular view
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
      
    	// if View doesn't represent when the status was created, exit fxn
    	if (view.getId() != R.id.textCreatedAt) 
    		return false;

      // Update the created at text to relative time
      long timestamp = cursor.getLong(columnIndex);		// get raw timestamp value from Cursor
      CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp + 1680000);	// business logic we're injecting (adding 1680000 (ms) because for some reason, times are 28 minutes behind)  
      		//if I add argument 'view.getContext(), ' before 'timestamp', it gives the relative time
      ((TextView)view).setText(relTime);	// update text on actual view

      return true;	// return true so that SimpleCursorAdapter doesn't process bindView() on this element in its standard way
    }
  };
  
  // Receiver to wake up when UpdaterService gets a new status
  // It refreshes the timeline list by requerying the cursor (without this, user would have to exit the screen and come back to it,
  // with this BroadcastReceiver, when UpdaterService receives an update, the Timeline updates
  class TimelineReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
      setupList();	// calls function that queries data and updates List
      Log.d("TimelineReceiver", "onReceived");
    }
  }
}
