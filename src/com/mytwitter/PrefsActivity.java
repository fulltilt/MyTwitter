package com.mytwitter;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}

// the difference between Preference Activities and other Activities is that it 
// extends PreferenceActivity instead of Activity and instead of setContentView()
// it uses addPreferencesFromResource()