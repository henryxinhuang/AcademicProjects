package com.EECE381.launchpad;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import com.EECE381.launchpad.MainActivity;;

public class SettingsActivity extends PreferenceActivity {

	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		
//		Preference button1 = (Preference)findPreference("button1");
//		button1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//		                @Override
//		                public boolean onPreferenceClick(Preference arg0) { 
//		                    //code for what you want it to do
//
//		                    return true;
//		                }
//		            });
//		
//		Preference button2 = (Preference)findPreference("button2");
//		button2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//		                @Override
//		                public boolean onPreferenceClick(Preference arg0) { 
//		                    //code for what you want it to do   
//		                    return true;
//		                }
//		            });
	}
}
