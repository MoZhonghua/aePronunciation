package com.aepronunciation.ipa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class IpaTextView extends TextView {

	// Constructors
	public IpaTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	public IpaTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public IpaTextView(Context context) {
		super(context);
		init();
	}

	// This class requires FreeSans.ttf to be in the assets/fonts folder
	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/FreeSans.ttf");
		setTypeface(tf);
	}

}