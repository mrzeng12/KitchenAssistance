package com.falcon.kitchenbuddy.ui

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.falcon.kitchenbuddy.MainActivity
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.helper.ActivityCallback

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ActivityCallback) {
            (activity as ActivityCallback).updateMenuIndex(R.id.nav_settings)
        }
        (activity as MainActivity).supportActionBar?.title = "Setting"
    }
}