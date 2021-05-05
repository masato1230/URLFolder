package com.jp_funda.urlfolder.Activities.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.jp_funda.urlfolder.R;

public class SettingsPrefFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String msg = "";
        msg += "delete: " + pref.getBoolean("dialog_delete_dialog_check_box", false);
        msg += "float: " + pref.getBoolean("float_button_show_checkbox", true);

        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
