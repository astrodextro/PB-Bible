package com.felixunlimited.pbbible.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.utils.Util;

public class HelpActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTheme(this, R.style.AppBaseTheme_Dialog_Light);
		setContentView(R.layout.help);
		
		int fontSize = getIntent().getExtras().getInt(Constants.FONT_SIZE);
		int helpContent = getIntent().getExtras().getInt(Constants.HELP_CONTENT);
		
		TextView txtHelpContent = (TextView) findViewById(R.id.txtHelpContent);
		//txtHelpContent.setMovementMethod(LinkMovementMethod.getInstance());
		txtHelpContent.setTextSize(fontSize);
		txtHelpContent.setText(helpContent);
	}
}
