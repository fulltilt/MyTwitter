package com.mytwitter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// common container for database functionality. Encapsulates SQLite in a higher-level class
// accessible to other classes in app. 
public class StatusData { 
  private static final String TAG = StatusData.class.getSimpleName();

  static final int VERSION = 1;							// database version. Used to update databases to newest schema
  static final String DATABASE = "timeline.db";			// database filename
  static final String TABLE = "timeline";				// the following are database constants which can be accessed by other classes
  public static final String C_ID = "_id";
  public static final String C_CREATED_AT = "created_at";
  public static final String C_TEXT = "txt";
  public static final String C_USER = "user";

  private static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC"; // 'created_at DESC' - return list in descending order 
  private static final String[] MAX_CREATED_AT_COLUMNS = { "max("   // 'max(created_at) - return most recent entry
      + StatusData.C_CREATED_AT + ")" };
  private static final String[] DB_TEXT_COLUMNS = { C_TEXT };		// txt

  final DbHelper dbHelper; 		// ensures that there will only be one global dbHelper for the whole app

  public StatusData(Context context) {  
    this.dbHelper = new DbHelper(context);  // initialize dbHelper and pass 'this' as the context (Service is a subclass of Context)
											// other subclasses of Context is Activity & Application
    Log.i(TAG, "Initialized data");
  }

  public void close() { 
    this.dbHelper.close();
  }

  // improved version of db.insertOrIgnore() which ignores conflicts
  public void insertOrIgnore(ContentValues values) {  
    Log.d(TAG, "insertOrIgnore on " + values);
    SQLiteDatabase db = this.dbHelper.getWritableDatabase();  // open db right before writing to it
    try {
      db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);  // special insert which ignores duplicates
    } finally {
      db.close(); 
    }
  }

  // @return Cursor where the columns are _id, created_at, user, txt
  // returns all the Statuses in db with the latest first (only retrieves the last 20 Statuses)
  public Cursor getStatusUpdates() {  
    SQLiteDatabase db = this.dbHelper.getReadableDatabase();
    
    /* from Android reference
     *  query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
		Query the given table, returning a Cursor over the result set.
     */
    return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY, "20"); 
  }
 
  //@return Timestamp of the latest status we have it the database
  // helpful to ensure we add only new statuses into the database
  public long getLatestStatusCreatedAtTime() {  
    SQLiteDatabase db = this.dbHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(TABLE, MAX_CREATED_AT_COLUMNS, null, null, null,
          null, null);
      try {
        return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE; // if a cursor points to a row, return the timestamp (column 0), else return the minimum value of a Long
      } finally {
        cursor.close();
      }
    } finally {
      db.close();
    }
  }

  /**
   * @param id of the status we are looking for
   * @return Text of the status
   */
  public String getStatusTextById(long id) { 
    SQLiteDatabase db = this.dbHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(TABLE, DB_TEXT_COLUMNS, C_ID + "=" + id, null,
          null, null, null);
      try {
        return cursor.moveToNext() ? cursor.getString(0) : null;	// if cursor points to a row return the Text (column 0 of the result set) 
      } finally {
        cursor.close();
      }
    } finally {
      db.close();
    }
  }
  
  //DbHelper implementations
  //SQLiteOpenHelper is a helper class that provides a "connection" to the database, creating
  //the connection if it doesn't already exist
  class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
      super(context, DATABASE, null, VERSION);
    }

    // Called only once, first time the DB is created
    public void onCreate(SQLiteDatabase db) {
      Log.i(TAG, "Creating database: " + DATABASE);
      db.execSQL("create table " + TABLE + " (" + C_ID + " int primary key, "
          + C_CREATED_AT + " int, " + C_USER + " text, " + C_TEXT + " text)");
      /*  CREATE TABLE TIMELINE (
       *  _ID int primary_key, 
       *  CREATED_AT int,
       *  USER text,
       *  TXT text);
       */
    }

    // Called whenever newVersion != oldVersion
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("drop table " + TABLE);
      this.onCreate(db);
    }
  }
}
