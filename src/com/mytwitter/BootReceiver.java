package com.mytwitter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// Broadcast receiver that the system will launch when the boot is complete. In this case,
// it will automatically launch the UpdaterService
public class BootReceiver extends BroadcastReceiver { 

  // method gets called when an intent matches this receiver
  public void onReceive(Context context, Intent intent) {
    context.startService(new Intent(context, UpdaterService.class));
    Log.d("BootReceiver", "onReceived");
  }
}