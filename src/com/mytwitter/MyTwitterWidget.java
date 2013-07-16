package com.mytwitter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

// Widget that uses Content Provider class to share data. AppWidgetProvider is a subclass of BroadcastReceiver
public class MyTwitterWidget extends AppWidgetProvider { 
  private static final String TAG = MyTwitterWidget.class.getSimpleName();

  // called whenever the widget needs to be updated
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) { 
    Cursor c = context.getContentResolver().query(StatusProvider.CONTENT_URI,
        null, null, null, null); // similar to how we access SQLiteDatabase. Instead of passing a DB, we pass a content URI
    try {
      if (c.moveToLast()) { // move to the latest Status update
    	// extract data from Cursor object and store it in local variables  
        CharSequence user = c.getString(c.getColumnIndex(StatusData.C_USER)); 
        CharSequence createdAt = DateUtils.getRelativeTimeSpanString(context, c
            .getLong(c.getColumnIndex(StatusData.C_CREATED_AT)));
        CharSequence message = c.getString(c.getColumnIndex(StatusData.C_TEXT));

        // Loop through all instances of this widget
        for (int appWidgetId : appWidgetIds) { // loop through all possible MyTwitter widgets and update accordingly
          Log.d(TAG, "Updating widget " + appWidgetId);
          RemoteViews views = new RemoteViews(context.getPackageName(),
              R.layout.mytwitter_widget); // RemoteViews framework is a special shared memory system designed specifically for widgets
          // once we have our widget views' Java memory space in another process, we can update those views
          views.setTextViewText(R.id.textUser, user); 
          views.setTextViewText(R.id.textCreatedAt, createdAt);
          views.setTextViewText(R.id.textText, message);
          views.setOnClickPendingIntent(R.id.mytwitter_icon, PendingIntent
              .getActivity(context, 0, new Intent(context,
                  TimelineActivity.class), 0));
          appWidgetManager.updateAppWidget(appWidgetId, views); // once we update the remote views, updateAppWidget(...) posts a message telling system to update our widget
        }
      } else {
        Log.d(TAG, "No data to update");
      }
    } finally {
      c.close();
    }
    Log.d(TAG, "onUpdated");
  }

  // get latest Status data updated on the widget
  public void onReceive(Context context, Intent intent) { 
    super.onReceive(context, intent);
    if (intent.getAction().equals(UpdaterService.NEW_STATUS_INTENT)) { // check whether the Intent was for the new Status broadcast
      Log.d(TAG, "onReceived detected new status update");
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context); 
      this.onUpdate(context, appWidgetManager, appWidgetManager
          .getAppWidgetIds(new ComponentName(context, MyTwitterWidget.class))); 
    }
  }
}
