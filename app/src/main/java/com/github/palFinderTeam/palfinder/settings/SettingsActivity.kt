package com.github.palFinderTeam.palfinder.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.github.palFinderTeam.palfinder.PalFinderBaseActivity
import com.github.palFinderTeam.palfinder.R

class SettingsActivity : PalFinderBaseActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, SettingsFragment()).commit()
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)

            bindPreferenceSummaryToValue(findPreference(COLOR_KEY))

        }
    }


    companion object {
        const val DEF_THEME = "Default"
        const val WARM_THEME = "Warm"
        const val COLOR_KEY = "list_color"
        const val THEME_KEY = "theme"

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference, value ->

                val stringValue = value.toString()

                if (preference is ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val index = preference.findIndexOfValue(stringValue)

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null
                    )
                }

                true
            }

        private fun bindPreferenceSummaryToValue(preference: Preference?) {
            // Set the listener to watch for value changes.
            if (preference == null) return

            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "")
            )
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            COLOR_KEY -> {
                val themeId = when(sharedPreferences.getString(key, DEF_THEME)) {
                    WARM_THEME -> {
                        R.style.palFinder_warm_theme
                    }
                    else -> {
                        R.style.palFinder_default_theme
                    }
                }

                val sharedPref = getSharedPreferences(THEME_KEY, Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putInt(THEME_KEY, themeId)
                    apply()
                }
                recreate()
            }
        }
    }
}
