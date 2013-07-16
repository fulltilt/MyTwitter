package com.mytwitter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


/**
 * The base Activity with common features shared by TimelineActivity and StatusActivity
 */
public class BaseActivity extends Activity {
  MyTwitterApplication myTwitter; 

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    myTwitter = (MyTwitterApplication)getApplication(); // get global MyTwitterApplication reference
  }

  // Called only once first time menu is clicked on
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);	// get MenuInflater from context and inflate it
    return true;	// must return true for menu to be displayed
  }

  // Called every time user clicks on a menu item
  public boolean onOptionsItemSelected(MenuItem item) { 

    switch (item.getItemId()) {
    	case R.id.itemPrefs:
    	{
    		startActivity(new Intent(this, PrefsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)); // super-shortened way to switch between Activities
    		break;
    	}
    	case R.id.itemToggleService:
    	{
    		if (myTwitter.isServiceRunning()) {
    			stopService(new Intent(this, UpdaterService.class));
    		} else {
    			startService(new Intent(this, UpdaterService.class));
    		}
    		break;
    	}
    	case R.id.itemTimeline:
    	{
    		startActivity(new Intent(this, TimelineActivity.class).addFlags(
    				Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(	// If set, the activity will not be launched if it is already running at the top of the history stack.
    				Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));	// If set in an Intent passed to Context.startActivity(), this flag will cause the launched activity to be brought to the front of its task's history stack if it is already running.
    		break;
    	}
    	case R.id.itemStatus:
    	{
    		startActivity(new Intent(this, StatusActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    		break;
    	}
    }
    return true;
  }

  // Called every time menu is opened. Dynamically toggles icons depending on whether UpdaterService is running 
  public boolean onMenuOpened(int featureId, Menu menu) { 
    MenuItem toggleItem = menu.findItem(R.id.itemToggleService); // item whose icon will change
    if (myTwitter.isServiceRunning()) { // if UpdaterService is running, change icon to Stop icon
      toggleItem.setTitle(R.string.titleServiceStop);
      toggleItem.setIcon(android.R.drawable.ic_media_pause);
    } else { // if UpdaterService is not running, change icon to Start icon
      toggleItem.setTitle(R.string.titleServiceStart);
      toggleItem.setIcon(android.R.drawable.ic_media_play);
    }
    return true;
  }
}
