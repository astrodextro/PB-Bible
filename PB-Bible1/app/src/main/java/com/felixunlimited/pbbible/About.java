package com.felixunlimited.pbbible;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class About extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTheme(this, R.style.AppBaseTheme_Dialog_Light);
		setContentView(R.layout.about);
		TextView txtAboutContent = (TextView) findViewById(R.id.about_content);
		txtAboutContent.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
