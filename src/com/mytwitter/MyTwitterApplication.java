package com.mytwitter;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

// Applications are objects are reserved for functions that are used by many parts of an app.
// It represents the common parts of the entire app. As long as any part of an app is running,
// the application object will be created
public class MyTwitterApplication extends Application implements OnSharedPreferenceChangeListener { 
  private static final String TAG = MyTwitterApplication.class.getSimpleName();
  public Twitter twitter; 
  private SharedPreferences prefs;  // global Preferences object
  private boolean serviceRunning;	// flag that indicates if the Updater Service is running
  
  private StatusData statusData;	// MyTwitterApplication encapsulates StatusData. This means that UpdaterService & TimelineActivity are in a has-a relationship to StatusData

  public void onCreate() { 
    super.onCreate();
    
    //setup preferences
    this.prefs = PreferenceManager.getDefaultSharedPreferences(this);	// each app has its own shared preferences. To get it pass 'this' as the current context for this app and pass it
    this.prefs.registerOnSharedPreferenceChangeListener(this);			// notifies when the Preferences is changed. If so, call onSharedPreferenceChanged()
    this.statusData = new StatusData(this);								// initialize StatusData object we'll use to access db
    Log.i(TAG, "onCreated");
  }

  public void onTerminate() { 
    super.onTerminate();
    Log.i(TAG, "onTerminated");
  }

  // since the username and password isn't hardcoded, this function gets the information from 
  // preferences and creates a new Twitter object from it.
  // synchronized to ensure that multiple threads don't access this fxn at the same time
  public synchronized Twitter getTwitter() {
	// if Twitter object exists do nothing, else create a new Twitter object  
    if (this.twitter == null) {
      String username = this.prefs.getString("username", null);
      String password = this.prefs.getString("password", null);
      String apiRoot = prefs.getString("apiRoot", "http://yamba.marakana.com/api");
      this.twitter = new Twitter(username, password);
      this.twitter.setAPIRootUrl(apiRoot);
    }
    return this.twitter;
  }
  
  public SharedPreferences getPrefs()
  {
	  return prefs;
  }

  // called when Preferences change
  public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	// if the preferences change, invalidate the Twitter object
    this.twitter = null;
  }

  // returns whether or not the UpdaterService is running
  public boolean isServiceRunning() {
	  return serviceRunning;
  }
  
  // sets whether or not the the UpdaterService is running (not the actual Service itself)
  public void setServiceRunning(boolean serviceRunning) {
	  this.serviceRunning = serviceRunning;
  }
  
  public StatusData getStatusData() {
	  return statusData;
  }
  
  // Connects to the online service and puts the latest statuses into DB.
  // Returns the count of new Statuses
  public synchronized int fetchStatusUpdates() { 
    Log.d(TAG, "Fetching status updates");
    Twitter twitter = this.getTwitter();
    if (twitter == null) {
      Log.d(TAG, "Twitter connection info not initialized");
      return 0;
    }
    
    try {
      List<Status> statusUpdates = twitter.getFriendsTimeline();
      long latestStatusCreatedAtTime = this.getStatusData().getLatestStatusCreatedAtTime();
      int count = 0;
      ContentValues values = new ContentValues();   // ContentValues is a simple name-value pair data structure that maps db table names to their respective values
      for (Status status : statusUpdates) { // iterate through each Status in List statusUpdates and parse the data to insert into database
        values.put(StatusData.C_ID, status.getId());
        long createdAt = status.getCreatedAt().getTime();
        values.put(StatusData.C_CREATED_AT, createdAt);
        values.put(StatusData.C_TEXT, status.getText());
        values.put(StatusData.C_USER, status.getUser().getName());
        Log.d(TAG, "Got update with id " + status.getId() + ". Saving");
        this.getStatusData().insertOrIgnore(values);
        if (latestStatusCreatedAtTime < createdAt) 	// if the data of the latest entry in database is less than the next entry pulled from the cloud, the new entry is a new one
        	count++;        
      }
      Log.d(TAG, count > 0 ? "Got " + count + " status updates" : "No new status updates");
      return count;
    } catch (RuntimeException e) {
      Log.e(TAG, "Failed to fetch status updates", e);
      return 0;
    }
  }
}