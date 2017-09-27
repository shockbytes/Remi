package at.shockbytes.remote.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import at.shockbytes.remote.R;

public class SettingsFragment extends PreferenceFragment {

	public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
