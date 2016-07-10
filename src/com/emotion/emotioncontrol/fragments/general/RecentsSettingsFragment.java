/*
 * Copyright (C) 2016 Emotroid Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emotion.emotioncontrol.fragments.general;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.emotion.emotioncontrol.R;
import com.emotion.emotioncontrol.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class RecentsSettingsFragment extends Fragment {

    public RecentsSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recents_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.recents_settings_main, new RecentsSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class RecentsSettingsPreferenceFragment extends PreferenceFragment
           implements Preference.OnPreferenceChangeListener {

        public RecentsSettingsPreferenceFragment() {

        }

        private static final String IMMERSIVE_RECENTS = "immersive_recents";
        private ListPreference mImmersiveRecents;

	private static final String TAG = "OmniSwitch";

        private static final String CAT_OMNISWITCH = "omniswitch_category";
        private static final String RECENTS_USE_OMNISWITCH = "recents_use_omniswitch";
        private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";

        // Package name of the omnniswitch app
        public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
        // Intent for launching the omniswitch settings actvity
        public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN)
                .setClassName(OMNISWITCH_PACKAGE_NAME, OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");

        private PreferenceCategory mOmniSwitchCategory;
        private SwitchPreference mRecentsUseOmniSwitch;
        private Preference mOmniSwitchSettings;
        private boolean mOmniSwitchInitCalled;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_recents_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();
            final PreferenceScreen prefScreen = getPreferenceScreen();
            PackageManager pm = getActivity().getPackageManager();	    
            final Resources res = getResources();

            mImmersiveRecents = (ListPreference) findPreference(IMMERSIVE_RECENTS);
            mImmersiveRecents.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.IMMERSIVE_RECENTS, 0)));
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
    		mImmersiveRecents.setOnPreferenceChangeListener(this);

            mRecentsUseOmniSwitch = (SwitchPreference)
                    findPreference(RECENTS_USE_OMNISWITCH);
            try {
                mRecentsUseOmniSwitch.setChecked(Settings.System.getInt(resolver,
                        Settings.System.RECENTS_USE_OMNISWITCH) == 1);
                mOmniSwitchInitCalled = true;
            } catch(SettingNotFoundException e){
                // if the settings value is unset
            }
            mRecentsUseOmniSwitch.setOnPreferenceChangeListener(this);

            mOmniSwitchSettings = (Preference)
                    findPreference(OMNISWITCH_START_SETTINGS);
            mOmniSwitchSettings.setEnabled(mRecentsUseOmniSwitch.isChecked());

            mOmniSwitchCategory = (PreferenceCategory) prefSet.findPreference(CAT_OMNISWITCH);
            if (!Helpers.isPackageInstalled(OMNISWITCH_PACKAGE_NAME, pm)) {
                prefSet.removePreference(mOmniSwitchCategory);
            }
            return prefSet;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) 	  {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mImmersiveRecents) {
                Settings.System.putInt(resolver, Settings.System.IMMERSIVE_RECENTS,
                        Integer.valueOf((String) newValue));
                mImmersiveRecents.setValue(String.valueOf(newValue));
                mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
                return true;
           } else if (preference == mRecentsUseOmniSwitch) {
                boolean value = (Boolean) newValue;

                // if value has never been set before
                if (value && !mOmniSwitchInitCalled){
                    openOmniSwitchFirstTimeWarning();
                    mOmniSwitchInitCalled = true;
                }

                Settings.System.putInt(
                        resolver, Settings.System.RECENTS_USE_OMNISWITCH, value ? 1 : 0);
                mOmniSwitchSettings.setEnabled(value);
            } else {
                return false;
            }

            return true;
        }

        private void openOmniSwitchFirstTimeWarning() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.omniswitch_first_time_title))
                    .setMessage(getResources().getString(R.string.omniswitch_first_time_message))
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                    }).show();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	if (preference == mOmniSwitchSettings){
                getActivity().startActivity(INTENT_OMNISWITCH_SETTINGS);
            }
    		return false;
        }
    }
}
