package com.github.palFinderTeam.palfinder.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.github.palFinderTeam.palfinder.R

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt(getString(R.string.theme), R.style.palFinder_default_theme)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        //setContentView(R.xml.settings_activity)
        //set container for fragment outside of res/xml

        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, SettingsFragment()).commit()
        }
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)

            bindPreferenceSummaryToValue(findPreference("list_color"))

        }
    }





    companion object {


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
                    val listPreference = preference
                    val index = listPreference.findIndexOfValue(stringValue)


                    // Set the summary to reflect the new value.
                    preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null
                    )


                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
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
            "list_color" -> {
                if (sharedPreferences.getString(key, "Default") == "Default") {
                    val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
                    with(sharedPref.edit()) {
                        putInt(getString(R.string.theme), R.style.palFinder_default_theme)
                        apply()
                    }
                    recreate()
                } else if (sharedPreferences.getString(key, "Warm") == "Warm") {
                    val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
                    with(sharedPref.edit()) {
                        putInt(getString(R.string.theme), R.style.palFinder_warm_theme)
                        apply()
                    }
                    recreate()
                }
            }
        }
    }
}
