package com.mytwitter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

// A Service is code that runs in the background and doesn't need an user interface. They should
// run independently of Activities. Services states are controlled by Intents. 
public class UpdaterService extends Service {
  static final String TAG = "UpdaterService";
  public static final String NEW_STATUS_INTENT = "com.mytwitter.NEW_STATUS";	// intent action used for Filter in TimelineActivity
  public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
  
  static final int DELAY = 60000;	// a minute
  private boolean runFlag = false;	// let's us know whether the service is running
  private Updater updater; 			// class that extends Thread which will be used for the network calls to pull in data
  private MyTwitterApplication myTwitter;		// use this classes ability to connect to online services

  // used in bound Services to return the actual implementation of a Binder
  public IBinder onBind(Intent intent) { 
    return null;
  }

  // called when a Service is first created. Called only once during a Services life cycle
  public void onCreate() { 
    super.onCreate();
    
    this.myTwitter = (MyTwitterApplication)getApplication();	// get reference to MyTwitterApplication object 
    this.updater = new Updater();
    
    Log.d(TAG, "onCreated");
  }

  // called when a Service first starts
  public int onStartCommand(Intent intent, int flags, int startId) { 
    super.onStartCommand(intent, flags, startId);
    
    this.runFlag = true;
    this.updater.start();	// start the Thread
    this.myTwitter.setServiceRunning(true);
    
    Log.d(TAG, "onStarted");
    return START_STICKY;
  }

  // called when a Service is terminated
  public void onDestroy() { 
    super.onDestroy();
    
    this.runFlag = false;
    this.updater.interrupt();
    this.updater = null;
    this.myTwitter.setServiceRunning(false);
    
    Log.d(TAG, "onDestroyed");
  }
  
  	// Thread that performs the actual update from the online service
  	private class Updater extends Thread 
  	{      
  		//List<Twitter.Status> timeline;		// list of last 20 most recent posts
  		Intent intent;
	    
	    public Updater() 
	    {
	    	super("UpdaterService-Updater");  // give the Thread a name. This helps id various running threads and aids in debugging
	    }

	    @Override
	    public void run() 
	    { 
	    	UpdaterService updaterService = UpdaterService.this; 
		    while (updaterService.runFlag) 
		    {  
		    	Log.d(TAG, "Running background Updater thread");
		    	try 
		    	{
		     		MyTwitterApplication myTwitter = (MyTwitterApplication)updaterService.getApplication();
		    		int newUpdates = myTwitter.fetchStatusUpdates();
		    		if (newUpdates > 0)	// the following block sends a broadcast to the TimelineReceiver (which subclasses BroadcastReceiver) and have it update the Timeline if a new update is received 
		    		{
		    			Log.d(TAG, "We have a new status!");
		    			intent = new Intent(NEW_STATUS_INTENT);	// intent to broadcast. Argument is a constant that represents an arbitrary action
		    			intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);	// adds data to the Intent. Communicates to others how many new statuses there are. Assigns newUpdates to the constant
		    			updaterService.sendBroadcast(intent);	// send the broadcast with the intent as the argument
		    		}
		    		
		    		Thread.sleep(DELAY);
		    	} 
		    	catch (InterruptedException e) 
		    	{
		    		updaterService.runFlag = false;
		    	}
		    }
	    }
  	}
}
