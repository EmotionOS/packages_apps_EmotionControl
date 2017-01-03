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

package com.emotion.emotioncontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.content.res.Configuration;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.widget.Toast;
import com.emotion.emotioncontrol.util.Helpers;

import com.emotion.emotioncontrol.R;
import com.emotion.emotioncontrol.widgets.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockScreenSettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {
        }

        private static final String PREF_KEYGUARD_TORCH = "keyguard_toggle_torch";
        private static final String LOCKSCREEN_MAX_NOTIF_CONFIG = "lockscreen_max_notif_cofig";

        private SwitchPreference mKeyguardTorch;

        static final int DEFAULT = 0xffffffff;

        private FingerprintManager mFingerprintManager;
        private SwitchPreference mFingerprintVib;
        private SeekBarPreferenceCham mMaxKeyguardNotifConfig;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.fragment_lockscreen_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            // Fingerprint vibration
            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            mFingerprintVib = (SwitchPreference) prefSet.findPreference("fingerprint_success_vib");
            if (!mFingerprintManager.isHardwareDetected()){
                prefSet.removePreference(mFingerprintVib);
            }

            // Keyguard Torch
            mKeyguardTorch = (SwitchPreference) prefSet.findPreference(PREF_KEYGUARD_TORCH);

            mMaxKeyguardNotifConfig = (SeekBarPreferenceCham) findPreference(LOCKSCREEN_MAX_NOTIF_CONFIG);
            int kgconf = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, 5);
            mMaxKeyguardNotifConfig.setValue(kgconf);
            mMaxKeyguardNotifConfig.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mMaxKeyguardNotifConfig) {
                int kgconf = (Integer) newValue;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, kgconf);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
