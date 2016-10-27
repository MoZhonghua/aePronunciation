package com.aepronunciation.ipa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import java.util.ArrayList;

public class SelectSoundActivity extends BaseActivity {
    protected CheckBox cbAllVowels = null;
    protected CheckBox cbAllConsonants = null;

    protected SharedPreferences settings;
    private boolean doubleSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sound);

        cbAllVowels = (CheckBox)findViewById(R.id.cbAllVowels);
        cbAllConsonants = (CheckBox)findViewById(R.id.cbAllConsonants);

        cbAllConsonants.setChecked(false);
        cbAllVowels.setChecked(false);

        doubleSounds = getIntent().getBooleanExtra("doubleSounds", false);

        fragmentManager = getSupportFragmentManager();
        keyboardFragment = (KeyboardFragment) fragmentManager
                .findFragmentByTag(KEYBOARD_FRAGMENT_TAG);
        if (keyboardFragment == null) {
            keyboardFragment = new KeyboardFragment();
            fragmentManager
                    .beginTransaction()
                    .add(R.id.keyboardContainer, keyboardFragment,
                            KEYBOARD_FRAGMENT_TAG).commitAllowingStateLoss();
        }

        final ArrayList<String> allowedSounds = getIntent().getStringArrayListExtra("allowedSounds");
        cbAllVowels.post(new Runnable() {
            public void run() {
                keyboardFragment.setSelectMode(true);
                keyboardFragment.hideFunctionKeys();
                keyboardFragment.hideUnstressedVowels();
                if (allowedSounds != null) {
                    keyboardFragment.setSoundsSelected(allowedSounds);
                }
            }
        });
    }

    @Override
    public void onKeyTouched(String keyString) {
        Log.d("debug", "keyString: ");
    }

    public void vowelsBoxClick(View v) {
            Log.d("debug", "onClickCheckBoxAllVowels: ");
            keyboardFragment.selectAllVowels(cbAllVowels.isChecked());
    }

    public void consonantsBoxClick(View v) {
        Log.d("debug", "onClickCheckBoxAllConsonants: ");
        keyboardFragment.selectAllConsonants(cbAllConsonants.isChecked());
    }

    protected void okClick(View v) {
        Intent intent = new Intent(this, PracticeDoubleActivity.class);
        ArrayList<String> selected = keyboardFragment.getSelectedSounds();
        intent.putExtra("selected", selected);

        if (doubleSounds) {
            int vowels = 0;
            int consonants = 0;
            for (String s : selected) {
                if (PhonemeTable.INSTANCE.isConsonant(s)) {
                    consonants++;
                } else {
                    vowels++;
                }
            }

            // You must select at least on vowel and one consonant
            if (consonants == 0 || vowels == 0) {
                showError(getResources().getString(R.string.err_select_at_least_c_and_v));
                return;
            }
        } else {
            if (selected.isEmpty()) {
                showError(getResources().getString(R.string.err_select_at_least_one_sound));
                return;
            }
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showError(String msg) {
        Bundle args = new Bundle();
        args.putString("errorMessage", msg);
        DialogFragment dialog = new ErrorDialogFragment();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "ErrorDialogFragmentTag");
        return;
    }

    protected void cancelClick(View v) {
        setResult(RESULT_CANCELED, null);
        finish();
    }
}
