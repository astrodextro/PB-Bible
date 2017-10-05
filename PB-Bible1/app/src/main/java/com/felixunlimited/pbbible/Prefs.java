package com.felixunlimited.pbbible;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		android.app.ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		addPreferencesFromResource(R.xml.settings);

		final ListPreference listLanguage = (ListPreference) this.findPreference("bookLanguage");
		final ListPreference listColorSchemes = (ListPreference) this.findPreference("prefColorSchemeLabels");
		bindPreferenceSummaryToValue(findPreference(getString(R.string.prefColorSchemeKey)));

//		String state = Environment.getExternalStorageState();
//		if (Environment.MEDIA_MOUNTED.equals(state)) {
//			File sdcard = Environment.getExternalStorageDirectory();
//			File bookNameFolder = new File(sdcard.getPath() + Constants.BOOKNAME_FOLDER);
//			if (!bookNameFolder.isDirectory()) {
//				return;
//			}
//			File[] bookNameFiles = bookNameFolder.listFiles();
//			List<String> listFileDisplay = new ArrayList<String>();
//			List<String> listFileValues = new ArrayList<String>();
//			for (File file : bookNameFiles) {
//				if (!file.getName().endsWith(".bkn")) continue;
//				String display = file.getName().toLowerCase();
//				display = display.substring(0,1).toUpperCase() + display.substring(1);
//				display = display.substring(0, display.length()-4);
//				listFileDisplay.add(display);
//				listFileValues.add(display.toLowerCase());
//			}
//			String[] arrDisplay = new String[listFileDisplay.size()];
//			String[] arrValues = new String[listFileValues.size()];
//			arrDisplay = listFileDisplay.toArray(arrDisplay);
//			arrValues = listFileValues.toArray(arrValues);
//			listLanguage.setEntries(arrDisplay);
//			listLanguage.setEntryValues(arrValues);
//		}
//
//		listLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()	{
//			public boolean onPreferenceChange(Preference p,
//					Object newValue) {
//				String str = getResources().getString(R.string.prefLangChanged);
//				String display = (String) newValue;
//				display = display.substring(0,1).toUpperCase() + display.substring(1);
//				String msg = String.format(str, display);
//				Toast.makeText(Prefs.this, msg, Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent showBibleActivity = new Intent(this, BiblesOffline.class);
        showBibleActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(showBibleActivity);
    }

    /**
	 * Attaches a listener so the summary is always updated with the preference value.
	 * Also fires the listener once, to initialize the summary (so it shows up before the value
	 * is changed.)
	 */
	private void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(this);

		Object value = PreferenceManager
				.getDefaultSharedPreferences(preference.getContext())
				.getString(preference.getKey(), "");
		String stringValue = value.toString();

		if (preference instanceof ListPreference) {
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list (since they have separate labels/values).
			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(stringValue);

			if (prefIndex >= 0) {
				preference.setSummary(listPreference.getEntries()[prefIndex]);
			}

		} else {
			// For other preferences, set the summary to the value's simple string representation.
			preference.setSummary(stringValue);
		}
		// Trigger the listener immediately with the preference's
		// current value.
//        onPreferenceChange(preference,
//                PreferenceManager
//                        .getDefaultSharedPreferences(preference.getContext())
//                        .getString(preference.getKey(), ""));
	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		String stringValue = value.toString();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final Context context = getApplicationContext();
		if (preference instanceof ListPreference) {
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list (since they have separate labels/values).
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list.
			ListPreference listPreference = (ListPreference) preference;
			int index = listPreference.findIndexOfValue(stringValue);

			// Set the summary to reflect the new value.
			preference.setSummary(
					index >= 0
							? listPreference.getEntries()[index]
							: null);

		} else {
			// For other preferences, set the summary to the value's simple string representation.
			preference.setSummary(stringValue);
		}
//        try {
//            stringValue = StorageUtil.getRemovebleSDCardPath();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
		return true;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public Intent getParentActivityIntent() {
		return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

}
