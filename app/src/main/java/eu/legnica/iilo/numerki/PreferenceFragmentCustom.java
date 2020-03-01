package eu.legnica.iilo.numerki;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

public class PreferenceFragmentCustom extends PreferenceFragmentCompat {

    private Preference notifyNumbersPreference;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.root_preferences);

        notifyNumbersPreference = findPreference(getString(R.string.notify_numbers_key));

        // Zaktualizuj status opcji z wyborem numerka
        updateUserType(null);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;

        // ---- Custom Preferences ----

        // Sprawdź czy wybrano opcję z wyborem godziny
        if (preference instanceof TimePreference) {
            dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }
        // ----------------------------

        // Utworzono jeden z customowych dialogów. Wyświetl go.
        if (dialogFragment != null && this.getFragmentManager() != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "androidx.preference" +
                    ".PreferenceFragment.DIALOG");
        } else {
            // Wybrano standardowy dialog. Uruchamiamy go normalnie.
            super.onDisplayPreferenceDialog(preference);
        }

        // Zmiana typu użytkownika ma wpływ na inne ustawienia.
        if(preference.getKey().equals(getString(R.string.user_type_key))) {
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                updateUserType(newValue.toString());
                return true;
            });
        }
    }

    // Zmiana typu użytkownika ma wpływ na inne ustawienia.
    private void updateUserType(String newUserType) {
        PreferenceCategory preferenceCategory = findPreference(getString(R.string.notify_category_key));
        Preference notifyPreference = findPreference(getString(R.string.notify_key));

        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

        newUserType = newUserType == null ? prefs.getString(getString(R.string.user_type_key), "unknown") : newUserType;

        // Usuwa dziwne ostrzeżenia, że może wystąpić NullPointerException.
        assert newUserType != null;
        assert preferenceCategory != null;
        assert notifyPreference != null;

        if(newUserType.equals("teacher")) {
            preferenceCategory.removePreference(notifyNumbersPreference);
            notifyPreference.setTitle(R.string.notify_teacher);
        } else {
            preferenceCategory.addPreference(notifyNumbersPreference);
            notifyPreference.setTitle(R.string.notify_student);
        }
    }
}
