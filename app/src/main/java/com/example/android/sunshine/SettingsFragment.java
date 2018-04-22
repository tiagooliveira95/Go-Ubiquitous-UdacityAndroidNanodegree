/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.sunshine;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.LocationService;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.example.android.sunshine.utilities.SunshineLocationUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

/**
 * The SettingsFragment serves as the display for all of the user's settings. In Sunshine, the
 * user will be able to change their preference for units of measurement from metric to imperial,
 * set their preferred weather location, and indicate whether or not they'd like to see
 * notifications.
 *
 * Please note: If you are using our dummy weather services, the location returned will always be
 * Mountain View, California.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private void setPreferenceSummary(Preference preference, Object value) {
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
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_location_auto),false);
        getPreferenceScreen().findPreference(getString(R.string.pref_location_key)).setEnabled(!isEnabled);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();

        if (key.equals(getString(R.string.pref_location_key))) {
            // we've changed the location
            // Wipe out any potential PlacePicker latlng values so that we can use this text entry.
            SunshinePreferences.resetLocationCoordinates(activity);
            SunshineSyncUtils.startImmediateSync(activity);
        } else if (key.equals(getString(R.string.pref_units_key))) {
            // units have changed. update lists of weather entries accordingly
            SunshineWeatherUtils.notifyWearUnitsChanged(activity);
            activity.getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        } else if(key.endsWith(getString(R.string.pref_location_auto))){

            boolean isEnabled = sharedPreferences.getBoolean(getString(R.string.pref_location_auto),false);
            getPreferenceScreen().findPreference(getString(R.string.pref_location_key)).setEnabled(!isEnabled);

            if(isEnabled){
                askForPermission(activity,sharedPreferences);
            }else{
                SunshineLocationUtils.autoLocationService(activity,false);
            }


        }
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }


    private void askForPermission(Activity activity, SharedPreferences sharedPreferences) {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

            getPreferenceScreen().findPreference(getString(R.string.pref_location_key)).setEnabled(true);
            sharedPreferences.edit().putBoolean(getString(R.string.pref_location_auto),false).apply();

            ((CheckBoxPreference)getPreferenceScreen().findPreference(getString(R.string.pref_location_auto))).setChecked(false);


            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, 9001);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, 9001);
            }
        }else{
            SunshineLocationUtils.autoLocationService(activity,true);
        }
    }





}
