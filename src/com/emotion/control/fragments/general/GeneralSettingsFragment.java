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

package com.emotion.control.fragments.general;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.emotion.control.R;
import com.emotion.control.util.Helpers;
import com.emotion.control.widgets.NumberPickerPreference;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class GeneralSettingsFragment extends Fragment {

    public GeneralSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.general_settings_main, new GeneralSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class GeneralSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public GeneralSettingsPreferenceFragment() {

        }

        private static final String TAG = "GeneralSettingsPreferenceFragment";
        private static final String KEY_LOCKCLOCK = "lock_clock";
        // Package name of the cLock app
        public static final String LOCKCLOCK_PACKAGE_NAME = "com.cyanogenmod.lockclock";

        private static final int MIN_DELAY_VALUE = 1;
        private static final int MAX_DELAY_VALUE = 30;

        private static final String SCREENSHOT_TYPE = "screenshot_type";
        private static final String SCREENSHOT_DELAY = "screenshot_delay";
        private static final String SCREENSHOT_SUMMARY = "Delay is set to ";

        private ListPreference mScreenshotType;
        private NumberPickerPreference mScreenshotDelay;

        private Context mContext;
        private Preference mLockClock;
        private Preference mStatsEmotion;
        private static final String PREF_STATS_EMOTION = "emotion_stats";

        public static final String STATS_PACKAGE_NAME = "com.emotion.control";
        public static Intent INTENT_STATS = new Intent(Intent.ACTION_MAIN)
                .setClassName(STATS_PACKAGE_NAME, STATS_PACKAGE_NAME + ".romstats.AnonymousStats");


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();

            ContentResolver resolver = getActivity().getContentResolver();

            mScreenshotType = (ListPreference) findPreference(SCREENSHOT_TYPE);
            int mScreenshotTypeValue = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, 0);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            mScreenshotType.setSummary(mScreenshotType.getEntry());
            mScreenshotType.setOnPreferenceChangeListener(this);

            mScreenshotDelay = (NumberPickerPreference) findPreference(SCREENSHOT_DELAY);
            mScreenshotDelay.setOnPreferenceChangeListener(this);
            mScreenshotDelay.setMinValue(MIN_DELAY_VALUE);
            mScreenshotDelay.setMaxValue(MAX_DELAY_VALUE);
            int ssDelay = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_DELAY, 1);
            mScreenshotDelay.setCurrentValue(ssDelay);
            updateScreenshotDelaySummary(ssDelay);

        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_general_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            mContext = getActivity().getApplicationContext();
            PackageManager pm = getActivity().getPackageManager();

            // cLock app check
            mLockClock = (Preference)
                    prefSet.findPreference(KEY_LOCKCLOCK);
            if (!Helpers.isPackageInstalled(LOCKCLOCK_PACKAGE_NAME, pm)) {
                prefSet.removePreference(mLockClock);
            }

            mStatsEmotion = prefSet.findPreference(PREF_STATS_EMOTION);

            return prefSet;

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if  (preference == mScreenshotType) {
                int mScreenshotTypeValue = Integer.parseInt(((String) newValue).toString());
                mScreenshotType.setSummary(
                        mScreenshotType.getEntries()[mScreenshotTypeValue]);
                Settings.System.putInt(resolver,
                        Settings.System.SCREENSHOT_TYPE, mScreenshotTypeValue);
                mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
                return true;
            } else if (preference == mScreenshotDelay) {
                int mScreenshotDelayValue = Integer.parseInt(newValue.toString());
                Settings.System.putInt(resolver, Settings.System.SCREENSHOT_DELAY,
                        mScreenshotDelayValue);
                updateScreenshotDelaySummary(mScreenshotDelayValue);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mStatsEmotion) {
                startActivity(INTENT_STATS);
            }
            return false;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private void updateScreenshotDelaySummary(int screenshotDelay) {
            mScreenshotDelay.setSummary(
                    getResources().getString(R.string.powermenu_screenshot_delay_message, screenshotDelay));
        }
    }
}
