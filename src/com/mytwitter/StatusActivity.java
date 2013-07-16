package com.mytwitter;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StatusActivity extends BaseActivity implements OnClickListener {
	public static final String TAG = "StatusActivity";
	Button updateButton;
	EditText editText;
	Twitter twitter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        editText = (EditText)findViewById(R.id.editText);
        updateButton = (Button)findViewById(R.id.buttonUpdate);
        updateButton.setOnClickListener(this);
    }
	
	@Override
	public void onClick(View v) {
		String statusText = editText.getText().toString();
		new PostToTwitter().execute(statusText); // execute() runs doInBackground() in class PostToTwitter
		Log.d(TAG, "onClicked");
	}
		
	// AsyncTask object that is a separate thread specific to the main UI
	class PostToTwitter extends AsyncTask<String, Integer, String> {	
		protected String doInBackground(String... statuses) {
			try 
			{
				((MyTwitterApplication)getApplication()).getTwitter().updateStatus(statuses[0]); //get Application object (as specified in manifest), cast it to MyTwitterApplication and call getTwitter()
				return "Posted the following status update: " + statuses[0];  // returns the status string (result goes to onPostExecute();
			} catch (TwitterException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
				return "Failed to post";
			}
		}
		
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// function does nothing for this program
		}
		
		protected void onPostExecute(String result) {
			// 'result' comes from the return value of 'doInBackground()'
			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show(); // super-shortened way to have Toast display a message
		}
	}
}