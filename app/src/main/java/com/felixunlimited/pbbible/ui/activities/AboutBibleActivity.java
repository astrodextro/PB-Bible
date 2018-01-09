package com.felixunlimited.pbbible.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.models.DatabaseHelper;
import com.felixunlimited.pbbible.utils.Util;

public class AboutBibleActivity extends Activity {
	private DatabaseHelper databaseHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTheme(this, R.style.AppBaseTheme_Dialog_Light);
		setContentView(R.layout.aboutbible);
		if (getIntent().getExtras() == null) return;
		databaseHelper = new DatabaseHelper(this);
		databaseHelper.open();
		String currentBible = getIntent().getExtras().getString(Constants.CURRENT_BIBLE);
		setTitle(currentBible);
		String about = databaseHelper.getAboutByBibleName(currentBible);
		TextView txtAboutBible = (TextView) findViewById(R.id.txtAboutBible);
		txtAboutBible.setTextSize(16);
		txtAboutBible.setText(Html.fromHtml(about));
	}
	
	@Override
	protected void onDestroy() {
		databaseHelper.close();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		databaseHelper.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		databaseHelper.open();
	}
}
