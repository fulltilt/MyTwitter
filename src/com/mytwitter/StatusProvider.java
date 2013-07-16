package com.mytwitter;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

// class that exposes Status updates to other Applications using Content Providers
public class StatusProvider extends ContentProvider {
	private static final String TAG = StatusProvider.class.getSimpleName();

	// Parts of URI: content://[authority]/[type of data provided (typically name of class in lower-class)]/[optional ID]
	public static final Uri CONTENT_URI = Uri.parse("content://com.mytwitter.statusprovider");
	public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.mytwitter.status";
	public static final String MULTIPLE_RECORDS_MIME_TYPE = "vnd.android.cursor.dir/vnd.mytwitter.status";

	StatusData statusData;
	  
	public boolean onCreate() {
		statusData = new StatusData(getContext());
	    return true;
	}
	
	// get MIME type. Uses getID() to determine if the URI has an ID part
	public String getType(Uri uri) {
		return this.getId(uri) < 0 ? MULTIPLE_RECORDS_MIME_TYPE
		        : SINGLE_RECORD_MIME_TYPE;
	}

	// insert record into DB via Content Provider interface
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
	    try {
	      long id = db.insertOrThrow(StatusData.TABLE, null, values);	// attempt to add new record into DB and upon succesful insert, get ID of new record
	      if (id == -1) {
	        throw new RuntimeException(String.format(
	            "%s: Failed to insert [%s] to [%s] for unknown reasons.", TAG, values, uri));
	      } else {
	        Uri newUri = ContentUris.withAppendedId(uri, id);	// if insert successful, use this helper method to craft a new URI containing the ID of the new record appended to the standard provider's URI
	        // Notify the Context's ContentResolver of the change
	        getContext().getContentResolver().notifyChange(newUri, null);
	        return newUri;
	      }
	    } finally {
	      db.close();
	    }
	}

	public Cursor query(Uri uri, String[] projection, String selection,
		      String[] selectionArgs, String sortOrder) {
		    long id = this.getId(uri);
		    SQLiteDatabase db = statusData.dbHelper.getReadableDatabase();
		    Log.d(TAG, "querying");

		    Cursor c;

		    if (id < 0) {
		      c = db.query(StatusData.TABLE, projection, selection, selectionArgs,
		          null, null, sortOrder); // if there's no ID, forward what we got for the content provider to the equivalent DB call
		    } else {
		      c = db.query(StatusData.TABLE, projection, StatusData.C_ID + "=" + id,
		          null, null, null, null);	// if an ID is present, use that ID as the WHERE clause to limit what record to return
		    }

		    // Notify the context's ContentResolver if the cursor result set changes
		    c.setNotificationUri(getContext().getContentResolver(), uri);

		    return c;
	}

	public int update(Uri uri, ContentValues values, String selection,
		      String[] selectionArgs) {
		    long id = this.getId(uri);	// extract ID from the URI. If no ID is present, -1 is returned
		    int count;
		    SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
		    try {
		      if (id < 0) {
		        count = db.update(StatusData.TABLE, values, selection, selectionArgs);	// if there's no ID, that means we're updating all the DB records that match the 'selection' and 'selectionArgs' constraints
		      } else {
		        count = db.update(StatusData.TABLE, values, StatusData.C_ID + "=" + id, null); // if an ID is present, we're using that ID as the only part of the WHERE clause to limit the single record we're updating
		      }
		    } finally {
		      db.close();
		    }

		    // Notify the Context's ContentResolver of the change
		    getContext().getContentResolver().notifyChange(uri, null);

		    return count;
	}
	
	// deleting data. Comments and args similar to update(...)
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    long id = this.getId(uri);
	    int count;
	    SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
	    try {
	      if (id < 0) {
	        count = db.delete(StatusData.TABLE, selection, selectionArgs);
	      } else {
	        count = db.delete(StatusData.TABLE, StatusData.C_ID + "=" + id, null);
	      }
	    } finally {
	      db.close();
	    }

	    // Notify the Context's ContentResolver of the change
	    getContext().getContentResolver().notifyChange(uri, null);

	    return count;
	  }
	
	  // Helper method to extract ID from URI
	  private long getId(Uri uri) {
	    String lastPathSegment = uri.getLastPathSegment();	// take last part of URI
	    if (lastPathSegment != null) {	// if last part is not null, try to parse it as a long and return it
	      try {
	        return Long.parseLong(lastPathSegment);
	      } catch (NumberFormatException e) {
	        // at least we tried
	      }
	    }
	    return -1;
	  }
}
