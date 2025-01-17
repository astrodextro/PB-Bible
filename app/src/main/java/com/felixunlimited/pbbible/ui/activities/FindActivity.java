package com.felixunlimited.pbbible.ui.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.models.BibleVersion;
import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.models.DatabaseHelper;
import com.felixunlimited.pbbible.models.SearchResult;
import com.felixunlimited.pbbible.ui.adapters.DisplaySearchResultAdapter;
import com.felixunlimited.pbbible.utils.Util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FindActivity extends ListActivity
		implements
		OnClickListener,
		OnItemClickListener,
		TextView.OnEditorActionListener {

	private DatabaseHelper databaseHelper;
	private ProgressDialog pd = null;
	
	private List<String> wordsToSearch = new ArrayList<String>();
	private boolean searchOldTestament;
	private boolean searchNewTestament;
	private String bibleName;
	private String bibleFileName;
	
	private String currentBookLanguage;
	private int currentFontSize;
	
	private DisplaySearchResultAdapter adapter;
	private List<SearchResult> resultList = new ArrayList<SearchResult>();
	
	private AlertDialog dialogTestament;
	private ListView viewTestament;
	
	private final static String TAG = "FindActivity";

	EditText edtSearch;
	Spinner spnBible;

	int numberOfOccurrencesNT, numberOfOccurrencesOT, numberOfOccurrencesALL;
	TextView occurrencesNT, occurrencesOT, occurrencesALL;
	boolean scroll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTheme(this, R.style.AppBaseTheme_Light);
		setContentView(R.layout.find);

		readPreference();
		occurrencesNT = findViewById(R.id.numberOfOccurrencesNT);
        occurrencesOT = findViewById(R.id.numberOfOccurrencesOT);
        occurrencesALL = findViewById(R.id.numberOfOccurrencesALL);
        occurrencesOT.setTextSize(currentFontSize-2);
        occurrencesNT.setTextSize(currentFontSize-2);
        occurrencesALL.setTextSize(currentFontSize-2);
		spnBible = (Spinner) findViewById(R.id.spnBible);
		edtSearch = (EditText) findViewById(R.id.edtSearch);
		edtSearch.setOnEditorActionListener(this);

		databaseHelper = new DatabaseHelper(this);
		databaseHelper.open();
		
		List<String> bibleList = databaseHelper.getBibleNameList();
		String[] arrBible = new String[bibleList.size()];
		arrBible = bibleList.toArray(arrBible);
		ArrayAdapter<String> aaBible = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, arrBible);
		aaBible.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnBible.setAdapter(aaBible);
		if (getIntent().getExtras() != null) {
			String currentBibleName = getIntent().getExtras().getString(Constants.CURRENT_BIBLE);
			for (int i = 0; i < bibleList.size(); i++) {
				if (currentBibleName.equals(bibleList.get(i))) {
					spnBible.setSelection(i);
					break;
				}
			}
		}
		if (bibleList.size() == 0) {
			Toast.makeText(this, R.string.downloadBibleRequired, Toast.LENGTH_LONG).show();
		}
		
		Button btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(this);
		
		adapter = new DisplaySearchResultAdapter(this, R.layout.rowfind, resultList, wordsToSearch, currentBookLanguage, currentFontSize);
		setListAdapter(adapter);
		registerForContextMenu(getListView());
		int lastVisibleIndex;
		getListView().setOnItemClickListener(this);
		getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    scroll = true;
                } else {
                    scroll = false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (scroll) {
//                    Transition.c transition = new TransitionSet().
                    if (occurrencesOT.getVisibility() == View.VISIBLE) occurrencesOT.setVisibility(View.GONE);
                    if (occurrencesNT.getVisibility() == View.VISIBLE) occurrencesNT.setVisibility(View.GONE);
                    if (occurrencesALL.getVisibility() == View.VISIBLE) occurrencesALL.setVisibility(View.GONE);
                }
            }
        });
		
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle(R.string.searchIn);
		String[] arrTestament = new String[] {getResources().getString(R.string.bothTestament), 
				getResources().getString(R.string.oldTestament), getResources().getString(R.string.newTestament)};
		viewTestament = new ListView(this);
		viewTestament.setAdapter(new ArrayAdapter<String>(this, R.layout.listitemmedium, arrTestament));
		viewTestament.setOnItemClickListener(this);
		
		ad.setView(viewTestament);		
		dialogTestament = ad.create();
	}

	private void readPreference() {
		SharedPreferences preference = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
		currentBookLanguage = preference.getString(Constants.BOOK_LANGUAGE, Constants.LANG_ENGLISH);
		currentFontSize = preference.getInt(Constants.FONT_SIZE, 14);
	}

	@Override
	protected void onDestroy() {
		databaseHelper.close();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		databaseHelper.open();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSearch:

				if (!prepareForSearch()) return;
				dialogTestament.show();
				break;
		}
	}

	private boolean prepareForSearch() {
		bibleName = (String) spnBible.getSelectedItem();
		if (bibleName == null) {
            Toast.makeText(this, R.string.bibleNameRequired, Toast.LENGTH_LONG).show();
			return false;
        }
		StringBuilder searchText = new StringBuilder(edtSearch.getText().toString().trim());
		if (searchText.length() == 0) return false;
		wordsToSearch.clear();
		while (searchText.indexOf("\"") > -1) {
            int posStartQuote = searchText.indexOf("\"");
            if (posStartQuote == searchText.length()-1) {
                searchText.delete(posStartQuote, searchText.length());
                continue;
            }
            int posEndQuote = searchText.indexOf("\"", posStartQuote+1);
            if (posEndQuote == -1) {
                String word = searchText.substring(posStartQuote+1);
                if (word.length() > 1) {
                    int j = 0;
                    for (String checkWord : wordsToSearch) {
                        if (checkWord.length() < word.length()) {
                            break;
                        }
                        j++;
                    }
                    wordsToSearch.add(j, word.toLowerCase());
                }
                searchText.delete(posStartQuote, searchText.length());
            } else {
                String word = searchText.substring(posStartQuote+1, posEndQuote);
                if (word.length() > 1) {
                    int j = 0;
                    for (String checkWord : wordsToSearch) {
                        if (checkWord.length() < word.length()) {
                            break;
                        }
                        j++;
                    }
                    wordsToSearch.add(j, word.toLowerCase());
                }
                searchText.delete(posStartQuote, posEndQuote+1);
            }
        }
		if (searchText.length() > 0) {
            String[] arrWords = searchText.toString().split(" ");
            for (String word : arrWords) {
                if (word.length() > 1) {
                    int j = 0;
                    for (String checkWord : wordsToSearch) {
                        if (checkWord.length() < word.length()) {
                            break;
                        }
                        j++;
                    }
                    wordsToSearch.add(j, word.toLowerCase());
                }
            }
        }
		if (wordsToSearch.size() == 0) return false;
		InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		in.hideSoftInputFromWindow(edtSearch.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return true;
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
		if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER )) ||
				actionId == EditorInfo.IME_ACTION_DONE){
			if (!prepareForSearch()) return false;
			searchOldTestament = true;
			searchNewTestament = true;
			StringBuilder sb = new StringBuilder();
			sb.append("Searching for: ");
			for (String word : wordsToSearch) {
				sb.append("'").append(word).append("', ");
			}
			sb.delete(sb.length()-2, sb.length());

			this.pd = ProgressDialog.show(this, getResources().getString(R.string.pleaseWait), sb.toString(), true, false);
			new SearchingTask(this).execute((Object)null);

			return true;
		}


		return false;
	}

	private class SearchingTask extends AsyncTask<Object, Void, Object> {
		private Context context;
		public SearchingTask(Context context) {
			this.context = context;
            if (occurrencesOT.getVisibility() == View.VISIBLE) occurrencesOT.setVisibility(View.GONE);
            if (occurrencesNT.getVisibility() == View.VISIBLE) occurrencesNT.setVisibility(View.GONE);
            if (occurrencesALL.getVisibility() == View.VISIBLE) occurrencesALL.setVisibility(View.GONE);
		}
		
		@Override
		protected Object doInBackground(Object... arg) {
			BibleVersion bibleVersion = databaseHelper.getBibleVersionByBibleName(bibleName);
			File sdcard = Environment.getExternalStorageDirectory();
			File file = new File(sdcard, Constants.BIBLE_FOLDER + "/" + bibleVersion.getFileName());
			bibleFileName = bibleVersion.getFileName();
			
			byte[] bomUtf8 = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
			
//			int book = 1;
//			int chapter = 1;
//			if (occurrencesNT.getVisibility() == View.VISIBLE)
//				occurrencesNT.setVisibility(View.GONE);
//			if (occurrencesOT.getVisibility() == View.VISIBLE)
//				occurrencesOT.setVisibility(View.GONE);

			resultList.clear();
			int verse = 0;
			int chapterIndex = 0;
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 8192);
				if (!searchOldTestament) {					
					String indexFileName = file.getAbsolutePath().replaceAll(".ont", ".idx");
					File fIndex = new File(indexFileName);
					DataInputStream is = new DataInputStream(new FileInputStream(fIndex));
					is.skip(929*4); // Mat 1
					int startOffset = is.readInt();
					is.close();
					br.skip(startOffset);
//					book = 40;
					chapterIndex = 929;
					numberOfOccurrencesOT = 0;
				}
				String line = null;
				String verseCount = Constants.arrVerseCount[chapterIndex];
				int maxVerse = Integer.valueOf(verseCount.substring(verseCount.lastIndexOf(";") + 1));				
				
				while ((line = br.readLine()) != null) {
					verse++;
					if (verse > maxVerse) {
						chapterIndex ++;
						verseCount = Constants.arrVerseCount[chapterIndex];
						maxVerse = Integer.valueOf(verseCount.substring(verseCount.lastIndexOf(";") + 1));
						verse = 1;
						if (chapterIndex == 929 && !searchNewTestament) {
							break;						
						}
						
					}
					
					boolean match = true;
//					Log.d(TAG, wordsToSearch.get(0));
					for (int i = 0; i < wordsToSearch.size(); i++) {
						if (line.toLowerCase().indexOf(wordsToSearch.get(i)) == -1) {
							match = false;
							break;
						}
					}
					if (!match) {
						continue;
					}

					// if execution gets to these line, match successful
					String[] arrBookChapter = Constants.arrVerseCount[chapterIndex].split(";");
			  		int book = Integer.parseInt(arrBookChapter[0]);
			  		int chapter = Integer.parseInt(arrBookChapter[1]);
					
			  		if (chapterIndex == 0) {
			  			if (Character.toString(line.charAt(0)).equals(new String(bomUtf8, "UTF-8"))) {
							line = line.substring(1);
						}
			  		}
			  		
					SearchResult sr = new SearchResult();
					sr.setBook(book);
					sr.setChapter(chapter);
					sr.setVerse(verse);
					sr.setContent(Util.parseVerse(line));
					resultList.add(sr);
					int MAX_RESULT = 10000;
					if (resultList.size() == MAX_RESULT) {
						break;
					}

					if (chapterIndex < 929)
						numberOfOccurrencesOT++;
					else
						numberOfOccurrencesNT++;
				}
				
			} catch (Exception e) { 
				Log.d(TAG, "Error searching in bible file");
			} finally {
                numberOfOccurrencesALL = numberOfOccurrencesNT + numberOfOccurrencesOT;
                if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return resultList.size();
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (pd != null) {
                pd.dismiss();
            }
			Integer countResult = (Integer) result;
			adapter.notifyDataSetChanged();
			String strSuccessFormat = getResources().getString(R.string.search_success);
			String strSuccessMsg = String.format(strSuccessFormat, countResult);
			if (countResult.intValue() < 2) {
				strSuccessMsg = strSuccessMsg.substring(0, strSuccessMsg.length()-1);
			}

            if (searchOldTestament && (numberOfOccurrencesOT != 0)) {
			    if (occurrencesOT.getVisibility() == View.GONE) occurrencesOT.setVisibility(View.VISIBLE);
                occurrencesOT.setText("Old Testament: " + numberOfOccurrencesOT);
            }
            if (searchNewTestament && (numberOfOccurrencesNT != 0)) {
                if (occurrencesNT.getVisibility() == View.GONE) occurrencesNT.setVisibility(View.VISIBLE);
			    occurrencesNT.setText("New Testament: "+numberOfOccurrencesNT);
            }
            if (searchNewTestament && searchOldTestament && (numberOfOccurrencesOT != 0) && (numberOfOccurrencesNT != 0)) {
                if (occurrencesALL.getVisibility() == View.GONE) occurrencesALL.setVisibility(View.VISIBLE);
			    occurrencesALL.setText("Total: "+numberOfOccurrencesALL);
            }

            numberOfOccurrencesOT = 0;
            numberOfOccurrencesNT = 0;
            numberOfOccurrencesALL = 0;
//			if (searchOldTestament && (occurrencesOT.getVisibility() == View.GONE)) {
//			    occurrencesOT.setVisibility(View.VISIBLE);
//				occurrencesOT.setText("Old Testament Occurrences: "+numberOfOccurrencesOT);
//			}
//			if (searchNewTestament && (occurrencesNT.getVisibility() == View.GONE)) {
//			    occurrencesNT.setVisibility(View.VISIBLE);
//				occurrencesNT.setText("New Testament Occurrences: "+numberOfOccurrencesNT);
//			}
			Toast.makeText(context, strSuccessMsg, Toast.LENGTH_LONG).show();
			context = null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent == getListView()) {
			SearchResult sr = resultList.get(position);
		    if (sr == null) return;
		    Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
		    int chapterIdx = Constants.arrBookStart[sr.getBook()-1] + sr.getChapter()-1;
		    editor.putInt(Constants.CHAPTER_INDEX, chapterIdx);
		    editor.putString(Constants.POSITION_BIBLE_NAME, bibleFileName);
		    editor.commit();
	        Intent showBibleActivity = new Intent(this, BiblesOfflineActivity.class);
	        showBibleActivity.putExtra(Constants.FROM_BOOKMARKS, true);
	        showBibleActivity.putExtra(Constants.BOOKMARK_VERSE_START, sr.getVerse());       
	        startActivity(showBibleActivity);
		} else if (parent == viewTestament) {			
			dialogTestament.dismiss();
			
			searchOldTestament = false;
			searchNewTestament = false;
			if (position == 0 || position == 1) {
				searchOldTestament = true;
			}
			if (position == 0 || position == 2) {
				searchNewTestament = true;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("Searching for: ");
			for (String word : wordsToSearch) {
				sb.append("'").append(word).append("', ");
			}
			sb.delete(sb.length()-2, sb.length());
			
			this.pd = ProgressDialog.show(this, getResources().getString(R.string.pleaseWait), sb.toString(), true, false);
	        new SearchingTask(this).execute((Object)null);
		};
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, R.id.help, Menu.NONE, R.string.help);
		item.setIcon(R.drawable.menu_help);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.help:
			Intent iHelp = new Intent(this, HelpActivity.class);
			iHelp.putExtra(Constants.FONT_SIZE, currentFontSize);
			iHelp.putExtra(Constants.HELP_CONTENT, R.string.help_find);
			startActivity(iHelp);
			return true;
		}
		return false;
	}
}