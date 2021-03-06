package com.aepronunciation.ipa;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class PracticeSingleActivity extends BaseActivity implements
		SoundPool.OnLoadCompleteListener {

	static final String STATE_READY_FOR_NEW_SOUND = "ready";
	static final String STATE_IPA = "ipaSymbol";

	private SingleSound singleSound;
	private TextView tvInputWindow;
	// private RelativeLayout playButton;
	private ImageView playButtonImage;
	private CheckBox cbConsonants;
	private CheckBox cbVowels;
	private String currentIpa = "";
	// TransitionDrawable background;
	TransitionDrawable rightAnswerTransistion;
	TransitionDrawable wrongAnswerTransistion;
	private static final int SRC_QUALITY = 0;
	private static final int PRIORITY = 1;
	private SoundPool soundPool = null;
	boolean readyForNewSound = true;
	String lastPlayedSound;
	long startTime;
	SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practice_single);

		// create objects
		tvInputWindow = (TextView) findViewById(R.id.tvInputWindow);
		playButtonImage = (ImageView) findViewById(R.id.ivPlay);
		cbConsonants = (CheckBox) findViewById(R.id.cbConsonants);
		cbVowels = (CheckBox) findViewById(R.id.cbVowels);
		singleSound = new SingleSound();

		// Set up fragment
		fragmentManager = getSupportFragmentManager();
		keyboardFragment = (KeyboardFragment) fragmentManager
				.findFragmentByTag(KEYBOARD_FRAGMENT_TAG);
		if (keyboardFragment == null) {
			keyboardFragment = new KeyboardFragment();
			fragmentManager
					.beginTransaction()
					.add(R.id.keyboardContainer, keyboardFragment,
							KEYBOARD_FRAGMENT_TAG).commit();

		}

		// Create the green and red effects for right/wrong answers
		Drawable backgrounds[] = new Drawable[2];
		Resources res = getResources();
		backgrounds[0] = res.getDrawable(R.drawable.input_window_normal);
		backgrounds[1] = res.getDrawable(R.drawable.input_window_right);
		rightAnswerTransistion = new TransitionDrawable(backgrounds);
		backgrounds[1] = res.getDrawable(R.drawable.input_window_wrong);
		wrongAnswerTransistion = new TransitionDrawable(backgrounds);

		// Hide function keys after layout done
		tvInputWindow.post(new Runnable() {
			public void run() {
				keyboardFragment.hideFunctionKeys();
			}
		});

	}

	@Override
	protected void onResume() {

		// start timing
		startTime = System.nanoTime();

		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, SRC_QUALITY);
		soundPool.setOnLoadCompleteListener(this);

		super.onResume();
	}

	@Override
	protected void onPause() {

		// Increment stored time by elapsed time
		settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		long formerTime = settings.getLong(TIME_PRACTICE_SINGLE_KEY,
				TIME_DEFAULT);
		long elapsedTime = System.nanoTime() - startTime;
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(TIME_PRACTICE_SINGLE_KEY, formerTime + elapsedTime);
		editor.commit();

		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
		}

		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Save the user's current game state
		savedInstanceState.putBoolean(STATE_READY_FOR_NEW_SOUND,
				readyForNewSound);
		savedInstanceState.putString(STATE_IPA, currentIpa);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		readyForNewSound = savedInstanceState
				.getBoolean(STATE_READY_FOR_NEW_SOUND);
		currentIpa = savedInstanceState.getString(STATE_IPA);

		if (!readyForNewSound) {

			// show the replay icon
			playButtonImage.setImageResource(R.drawable.ic_action_replay);

		}

	}

	public void playClick(View v) {

		if (readyForNewSound) {
			String ipa;
			// get random single ipa
			if (cbConsonants.isChecked() && cbVowels.isChecked()) {
				// any single sound
				ipa = singleSound.getRandomIpa(getApplicationContext());

			} else if (cbConsonants.isChecked() && !cbVowels.isChecked()) {
				// consonant
				ipa = singleSound
						.getRandomConsonantIpa(getApplicationContext());
			} else {
				// vowel
				ipa = singleSound.getRandomVowelIpa(getApplicationContext());
			}

			// look up audio resource id for that sound
			int soundId = singleSound.getSoundResourceId(ipa);

			// load (and play) sound
			soundPool.load(this, soundId, PRIORITY);

			currentIpa = ipa;
			readyForNewSound = false;
			rightAnswerTransistion.resetTransition();
			tvInputWindow.setText("");

			// change the icon to repeat
			playButtonImage.setImageResource(R.drawable.ic_action_replay);

		} else {

			// play the old sound again
			int soundId = singleSound.getSoundResourceId(currentIpa);
			soundPool.load(this, soundId, PRIORITY);

		}

		lastPlayedSound = currentIpa;
	}

	public void tellMeClick(View v) {
		if (readyForNewSound == true) {
			return;
		}
		tvInputWindow.setText(currentIpa);
		animateBackground(true);

		// play sound
		int soundId = singleSound.getSoundResourceId(currentIpa);
		soundPool.load(this, soundId, PRIORITY);

		// change the play button to next
		playButtonImage.setImageResource(R.drawable.ic_action_next);

		readyForNewSound = true;
	}

	public void consonantsBoxClick(View v) {

		// called right after a change

		if (!cbConsonants.isChecked() && cbVowels.isChecked()) {
			// hide consonants
			keyboardFragment.hideConsonants();

		} else if (!cbConsonants.isChecked() && !cbVowels.isChecked()) {
			cbVowels.setChecked(true);
			// hide consonants and show vowels
			keyboardFragment.hideConsonants();
			keyboardFragment.showVowels();
		} else {
			// show consonants
			keyboardFragment.showConsonants();
		}

		// redo sound and display
		resetSoundAndDisplay();

	}

	public void vowelsBoxClick(View v) {

		if (!cbVowels.isChecked() && cbConsonants.isChecked()) {
			// hide vowels
			keyboardFragment.hideVowels();
		} else if (!cbVowels.isChecked() && !cbConsonants.isChecked()) {
			cbConsonants.setChecked(true);
			// hide vowels and show consonants
			keyboardFragment.hideVowels();
			keyboardFragment.showConsonants();
		} else {
			// show vowels
			keyboardFragment.showVowels();
		}

		// redo sound and display
		resetSoundAndDisplay();
	}

	private void resetSoundAndDisplay() {
		readyForNewSound = true;
		lastPlayedSound = "";
		playButtonImage.setImageResource(R.drawable.ic_action_play);
		rightAnswerTransistion.resetTransition();
		tvInputWindow.setText("");
	}

	@Override
	public void onKeyTouched(String keyString) {

		if (TextUtils.isEmpty(keyString)) {
			return;
		}

		// don't allow more clicks when green
		if (readyForNewSound) {
			return;
		}

		tvInputWindow.setText(keyString);

		// check if right or not
		if (keyString.equals(currentIpa)) {
			// if right then animate backgound to green and back
			animateBackground(true);

			// change the play button to next
			playButtonImage.setImageResource(R.drawable.ic_action_next);

			// play sound that was pressed if different than last played
			if (!lastPlayedSound.equals(currentIpa)) {
				int soundId = singleSound.getSoundResourceId(keyString);
				soundPool.load(this, soundId, PRIORITY);
			}

			readyForNewSound = true;
		} else {
			// if wrong then animate to red and back, play wrong sound
			animateBackground(false);

			// play sound that was pressed
			int soundId = singleSound.getSoundResourceId(keyString);
			soundPool.load(this, soundId, PRIORITY);

		}
		lastPlayedSound = "";
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void animateBackground(boolean answerIsCorrect) {

		if (answerIsCorrect) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				tvInputWindow.setBackground(rightAnswerTransistion);
			} else {
				tvInputWindow.setBackgroundDrawable(rightAnswerTransistion);
			}

			rightAnswerTransistion.startTransition(300);
			// rightAnswerTransistion.reverseTransition(300);

		} else {

			final int TRANSITION_START_TIME = 300;
			final int TRANSITION_REVERSE_TIME = 300;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				tvInputWindow.setBackground(wrongAnswerTransistion);
			} else {
				tvInputWindow.setBackgroundDrawable(wrongAnswerTransistion);
			}

			wrongAnswerTransistion.startTransition(300);
			wrongAnswerTransistion.reverseTransition(300);

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					tvInputWindow.setText("");
				}
			}, TRANSITION_START_TIME + TRANSITION_REVERSE_TIME);
		}
	}

	@Override
	public void onLoadComplete(SoundPool sPool, int sid, int status) {

		if (status != 0) // 0=success
			return;

		soundPool.play(sid, 1, 1, PRIORITY, 0, 1.0f);
		soundPool.unload(sid);

	}
}
