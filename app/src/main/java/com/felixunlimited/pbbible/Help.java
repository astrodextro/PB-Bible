package com.felixunlimited.pbbible;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Help extends Activity {
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
