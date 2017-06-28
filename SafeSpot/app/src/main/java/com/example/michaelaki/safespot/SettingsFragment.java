package com.example.michaelaki.safespot;

import android.support.v7.preference.PreferenceFragmentCompat;
import android.os.*;

/**
 * Created by jspall16 on 6/25/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_preferences);
    }
}
