package com.lagoru.quadromini;


import android.os.Bundle;
import android.preference.PreferenceFragment;

public class Preferences extends PreferenceFragment {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
