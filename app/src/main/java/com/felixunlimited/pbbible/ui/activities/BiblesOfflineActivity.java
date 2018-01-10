package com.felixunlimited.pbbible.ui.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.models.BibleVersion;
import com.felixunlimited.pbbible.models.Bookmark;
import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.models.DatabaseHelper;
import com.felixunlimited.pbbible.models.DisplayVerse;
import com.felixunlimited.pbbible.services.RandomMonthlyTheme;
import com.felixunlimited.pbbible.sync.Sync;
import com.felixunlimited.pbbible.ui.adapters.DisplayHistoryAdapter;
import com.felixunlimited.pbbible.ui.adapters.DisplayVerseAdapter;
import com.felixunlimited.pbbible.utils.NotePadUtils;
import com.felixunlimited.pbbible.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.felixunlimited.pbbible.models.Constants.BIBLE_FOLDER;
import static com.felixunlimited.pbbible.models.Constants.BOOKMARK_VERSE_START;
import static com.felixunlimited.pbbible.models.Constants.BOOKNAME_FOLDER;
import static com.felixunlimited.pbbible.models.Constants.BOOK_LANGUAGE;
import static com.felixunlimited.pbbible.models.Constants.CHAPTER_INDEX;
import static com.felixunlimited.pbbible.models.Constants.CURRENT_BIBLE;
import static com.felixunlimited.pbbible.models.Constants.DB_DATE_FORMAT;
import static com.felixunlimited.pbbible.models.Constants.FONT_SIZE;
import static com.felixunlimited.pbbible.models.Constants.FROM_BOOKMARKS;
import static com.felixunlimited.pbbible.models.Constants.FROM_WIDGET;
import static com.felixunlimited.pbbible.models.Constants.FULL_SCREEN;
import static com.felixunlimited.pbbible.models.Constants.HELP_CONTENT;
import static com.felixunlimited.pbbible.models.Constants.LANG_BAHASA;
import static com.felixunlimited.pbbible.models.Constants.LANG_ENGLISH;
import static com.felixunlimited.pbbible.models.Constants.PARALLEL;
import static com.felixunlimited.pbbible.models.Constants.POSITION_BIBLE_NAME;
import static com.felixunlimited.pbbible.models.Constants.POSITION_BIBLE_NAME_2;
import static com.felixunlimited.pbbible.models.Constants.POSITION_BOOK;
import static com.felixunlimited.pbbible.models.Constants.PREFERENCE_NAME;
import static com.felixunlimited.pbbible.models.Constants.WIDGET_BIBLE;
import static com.felixunlimited.pbbible.models.Constants.WIDGET_BOOK;
import static com.felixunlimited.pbbible.models.Constants.WIDGET_CHAPTER;
import static com.felixunlimited.pbbible.models.Constants.WIDGET_VERSE;
import static com.felixunlimited.pbbible.models.Constants.arrActiveBookAbbr;
import static com.felixunlimited.pbbible.models.Constants.arrActiveBookName;
import static com.felixunlimited.pbbible.models.Constants.arrBookName;
import static com.felixunlimited.pbbible.models.Constants.arrBookNameIndo;
import static com.felixunlimited.pbbible.models.Constants.arrBookStart;
import static com.felixunlimited.pbbible.models.Constants.arrVerseCount;
import static com.felixunlimited.pbbible.models.Constants.voiceFriendlyBookNames;

public class BiblesOfflineActivity extends ListActivity implements OnClickListener,
		DialogInterface.OnClickListener,
		OnItemClickListener, View.OnTouchListener {
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
    private static final String TAG = "BiblesOfflineActivity";

	private DatabaseHelper databaseHelper;
	private boolean isOpen = false;

	//persist
	private int currentChapterIdx;
	private String currentBibleFilename;
	private String currentBibleFilename2;
	private String currentBookLanguage;
	private int currentFontSize;
	private boolean isFullScreen;
	//not persist
	private String currentBibleName;
	
	private View footnoteView;
	private View bookmarkView;	
	private View copyToClipboardView;
	private AlertDialog footnoteDialog;
	private AlertDialog bookmarkDialog;
	private AlertDialog highlightDialog;
	private AlertDialog copyToClipboardDialog;

	private char copyOrShare; //'c' or 's' or 'b' or 'n' or 'p'
	
	private ProgressDialog pd = null;
	
	private TextView txtEmpty;
	private TextView txtEmpty2;
	
	public View getFootnoteView() {
		return footnoteView;
	}
	public AlertDialog getFootnoteDialog() {
		return footnoteDialog;
	}
	
	private Handler handler = new Handler();
	private List<DisplayVerse> verseList = new ArrayList<DisplayVerse>();
	private DisplayVerseAdapter adapter;
	
	private List<DisplayVerse> verseParallelList = new ArrayList<DisplayVerse>();
	private DisplayVerseAdapter parallelAdapter;
	
	private boolean fromBookmarks;
	private int bookmarkVerseStart;
	private int bookmarkVerseEnd;
	private int bookmarkVerseMax;
	
	private boolean gotoDownloadBible = false;
	private boolean gotoBrowse = false;
	private boolean gotoSelectParallel = false;
	private boolean gotoPrefs = false;
	private boolean gotoDocuments = false;
	private int lastChapterIdx = 0;
	
	private AlertDialog dialogHistory;
	private ListView viewHistory;
	private List<Integer> historyList = new ArrayList<Integer>();
	private DisplayHistoryAdapter historyAdapter;
    public  View btnListen;

    private AlertDialog dialogBibles;
	private ListView viewBibles;
	private List<String> bibleList = new ArrayList<String>();
	private ArrayAdapter<String> biblesAdapter;
	
	private boolean isParallel;

	View mGestureView;
	private int mPtrCount = 0;

	private float mPrimStartTouchEventX = -1;
	private float mPrimStartTouchEventY = -1;
	private float mSecStartTouchEventX = -1;
	private float mSecStartTouchEventY = -1;
	private float mPrimSecStartTouchDistance = 0;

	private int mViewScaledTouchSlop = 0;

	private ScaleGestureDetector mScaleDetector;
//	@Override
//	public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
//		Menu menu = actionMode.getMenu();
//		DisplayVerse verse = verseList.get(position);
//		if (verse == null) return;
//
//		if (verse.isBookmark()) {
//			menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.editBookmark);
//			menu.add(Menu.NONE, Menu.FIRST+1, Menu.NONE, R.string.removeBookmark);
//			menu.add(Menu.NONE, Menu.FIRST+3, Menu.NONE, R.string.copyToClipboard);
//			menu.add(Menu.NONE, Menu.FIRST+5, Menu.NONE, R.string.share);
//			menu.add(Menu.NONE, Menu.FIRST+7, Menu.NONE, R.string.highlight);
//			menu.add(Menu.NONE, Menu.FIRST+9, Menu.NONE, R.string.send_to_bae);
//			menu.add(Menu.NONE, Menu.FIRST+11, Menu.NONE, R.string.send_to_prayer_list);
//		}
//	}
//
//	@Override
//	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
//		MenuInflater inflater = actionMode.getMenuInflater();
//		inflater.inflate(R.menu.cab_menu, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
//		return true;
//	}
//
//	@Override
//	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//		AdapterView.AdapterContextMenuInfo info;
//		try {
//			info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
//		} catch (ClassCastException e) {
//			Log.e(TAG, "bad menuInfo", e);
//			return false;
//		}
//
//		int index = info.position;
//
//		DisplayVerse verse = verseList.get(index);
//		if (verse == null) return false;
//		TextView txtVerse = (TextView) bookmarkView.findViewById(R.id.txtVerse);
//
//		switch (menuItem.getItemId()) {
//			case Menu.FIRST: //edit Bookmark
//				String[] arrBookChapter = Constants.arrVerseCount[currentChapterIdx].split(";");
//				int book = Integer.parseInt(arrBookChapter[0]);
//				int chapter = Integer.parseInt(arrBookChapter[1]);
//				Bookmark bm = databaseHelper.getBookmark(book, chapter, verse.getVerseNumber());
//				bookmarkVerseStart = bm.getVerseStart();
//				bookmarkVerseEnd = bm.getVerseEnd();
//				txtVerse.setTextSize(currentFontSize);
//				StringBuffer sb = new StringBuffer();
//				for (int j = bookmarkVerseStart; j <= bookmarkVerseEnd; j++) {
//					DisplayVerse v = verseList.get(index + (j - bookmarkVerseStart));
//					sb.append(SyncUtils.parseVerse(v.getVerse())).append(" ");
//				}
//				txtVerse.setText(sb.substring(0, sb.length() - 1));
//				refreshBookNameOnBookmarkDialog();
//				Spinner spnCategory = (Spinner) bookmarkView.findViewById(R.id.spnCategory);
//				for (int i = 0; i < spnCategory.getAdapter().getCount(); i++) {
//					String categoryName = (String) spnCategory.getAdapter().getItem(i);
//					if (categoryName.equals(bm.getCategoryName())) {
//						spnCategory.setSelection(i);
//						break;
//					}
//				}
//				bookmarkDialog.show();
//				return true;
//			case Menu.FIRST + 1: //remove Bookmark
//				final int verseNumber = verse.getVerseNumber();
//				new AlertDialog.Builder(this)
//						.setTitle(R.string.removeBookmark)
//						.setMessage(R.string.reallyRemoveBookmark)
//						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								String[] arrBookChapter = Constants.arrVerseCount[currentChapterIdx].split(";");
//								int book = Integer.parseInt(arrBookChapter[0]);
//								int chapter = Integer.parseInt(arrBookChapter[1]);
//								databaseHelper.removeBookmark(book, chapter, verseNumber);
//								displayBible(currentBibleFilename, currentChapterIdx, false);
//							}
//
//						})
//						.setNegativeButton(R.string.no, null)
//						.show();
//				return true;
//			case Menu.FIRST + 2: //add Bookmark
//				bookmarkVerseStart = verse.getVerseNumber();
//				bookmarkVerseEnd = verse.getVerseNumber();
//				txtVerse.setTextSize(currentFontSize);
//				txtVerse.setText(SyncUtils.parseVerse(verse.getVerse()));
//				refreshBookNameOnBookmarkDialog();
//				bookmarkDialog.show();
//				return true;
//			case Menu.FIRST + 3: //copy bookmarked verse to clipboard
//				copyOrShare = 'c';
//				arrBookChapter = Constants.arrVerseCount[currentChapterIdx].split(";");
//				book = Integer.parseInt(arrBookChapter[0]);
//				chapter = Integer.parseInt(arrBookChapter[1]);
//				bm = databaseHelper.getBookmark(book, chapter, verse.getVerseNumber());
//				bookmarkVerseStart = bm.getVerseStart();
//				bookmarkVerseEnd = bm.getVerseEnd();
//				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
//				txtVerse.setTextSize(currentFontSize);
//				sb = new StringBuffer();
//				for (int j = bookmarkVerseStart; j <= bookmarkVerseEnd; j++) {
//					DisplayVerse v = verseList.get(index + (j - bookmarkVerseStart));
//					sb.append(SyncUtils.parseVerse(v.getVerse())).append(" ");
//				}
//				txtVerse.setText(sb.substring(0, sb.length() - 1));
//				refreshBookNameOnCopyToClipboardDialog();
//				copyToClipboardDialog.setTitle("Copy to clipboard");
//				copyToClipboardDialog.show();
//				return true;
//			case Menu.FIRST + 4: //copy unbookmarked verse to clipboard
//				copyOrShare = 'c';
//				bookmarkVerseStart = verse.getVerseNumber();
//				bookmarkVerseEnd = verse.getVerseNumber();
//				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
//				txtVerse.setTextSize(currentFontSize);
//				txtVerse.setText(SyncUtils.parseVerse(verse.getVerse()));
//				refreshBookNameOnCopyToClipboardDialog();
//				copyToClipboardDialog.setTitle("Copy to clipboard");
//				copyToClipboardDialog.show();
//				return true;
//			case Menu.FIRST + 5: //share bookmarked
//				copyOrShare = 's';
//				arrBookChapter = Constants.arrVerseCount[currentChapterIdx].split(";");
//				book = Integer.parseInt(arrBookChapter[0]);
//				chapter = Integer.parseInt(arrBookChapter[1]);
//				bm = databaseHelper.getBookmark(book, chapter, verse.getVerseNumber());
//				bookmarkVerseStart = bm.getVerseStart();
//				bookmarkVerseEnd = bm.getVerseEnd();
//				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
//				txtVerse.setTextSize(currentFontSize);
//				sb = new StringBuffer();
//				for (int j = bookmarkVerseStart; j <= bookmarkVerseEnd; j++) {
//					DisplayVerse v = verseList.get(index + (j - bookmarkVerseStart));
//					sb.append(SyncUtils.parseVerse(v.getVerse())).append(" ");
//				}
//				txtVerse.setText(sb.substring(0, sb.length() - 1));
//				refreshBookNameOnCopyToClipboardDialog();
//				copyToClipboardDialog.setTitle("Share verse");
//				copyToClipboardDialog.show();
//				return true;
//			case Menu.FIRST + 6: //share unbookmarked
//				copyOrShare = 's';
//				bookmarkVerseStart = verse.getVerseNumber();
//				bookmarkVerseEnd = verse.getVerseNumber();
//				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
//				txtVerse.setTextSize(currentFontSize);
//				txtVerse.setText(SyncUtils.parseVerse(verse.getVerse()));
//				refreshBookNameOnCopyToClipboardDialog();
//				copyToClipboardDialog.setTitle("Share verse");
//				copyToClipboardDialog.show();
//				return true;
//		}
//		return true;
//	}
//
//
//	@Override
//	public void onDestroyActionMode(ActionMode actionMode) {
//		actionMode.finish();
//	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.VmPolicy.Builder VmPolicybuilder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(VmPolicybuilder.build());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).getBoolean(Constants.PERMISSION_GRANTED, false)) {
				Intent showSplashScreen = new Intent(this, SplashScreen.class);
				showSplashScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(showSplashScreen);
				finish();
				return;
			}
		}
		//Util.setTheme(this, R.style.AppBaseTheme_Light_NoTitleBar);
//        createKWSFile();
//		if (SyncUtils.isMyServiceRunning(RandomMonthlyTheme.class, this))
//			stopService(new Intent(BiblesOfflineActivity.this, RandomMonthlyTheme.class));
		if (!Util.isMyServiceRunning(RandomMonthlyTheme.class, this))
			startService(new Intent(this, RandomMonthlyTheme.class));
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		bookmarkVerseStart = 1;
		if (getIntent().getExtras() != null) {
			this.fromBookmarks = getIntent().getExtras().getBoolean(FROM_BOOKMARKS, false);
			if (fromBookmarks) {
				bookmarkVerseStart =  getIntent().getExtras().getInt(BOOKMARK_VERSE_START, 1);
			}
			boolean fromWidget = getIntent().getExtras().getBoolean(FROM_WIDGET, false);
			if (fromWidget) {
				//this is similar with from bookmarks
				if (arrActiveBookName[0] != null) {
					fromBookmarks = true;
				}
				bookmarkVerseStart = getIntent().getExtras().getInt(WIDGET_VERSE, 1);
				String bible = getIntent().getExtras().getString(WIDGET_BIBLE);
				int book = getIntent().getExtras().getInt(WIDGET_BOOK, 1);
				int chapter = getIntent().getExtras().getInt(WIDGET_CHAPTER, 1);
				Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
				int chapterIdx = arrBookStart[book-1] + chapter-1;
				editor.putInt(CHAPTER_INDEX, chapterIdx);
				editor.putString(POSITION_BIBLE_NAME, bible + ".ont");
				editor.apply();
			}
		}

		setContentView(R.layout.main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		txtEmpty = findViewById(R.id.txtEmpty);
		getListView().setEmptyView(txtEmpty);
//		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
//		getListView().setMultiChoiceModeListener(this);
		currentChapterIdx = -1;

		databaseHelper = new DatabaseHelper(this);
		databaseHelper.open();
		isOpen = true;

		LayoutInflater li = LayoutInflater.from(this);
		AlertDialog.Builder builder;

		footnoteView = li.inflate(R.layout.footnote, null);
		builder = new AlertDialog.Builder(this);
		builder.setTitle("SyncUtils");
		builder.setView(footnoteView);
		builder.setNeutralButton("Close", this);
		footnoteDialog = builder.create();

		bookmarkView = li.inflate(R.layout.bookmarkdialog, null);
		builder = new AlertDialog.Builder(this);
		builder.setTitle("Bookmark");
		builder.setView(bookmarkView);
		builder.setPositiveButton("OK", this);
		builder.setNeutralButton("Cancel", this);
		bookmarkDialog = builder.create();
		fillSpinnerCategory();
		Button btnPlus = (Button) bookmarkView.findViewById(R.id.btnPlus);
		btnPlus.setOnClickListener(this);
		Button btnMinus = (Button) bookmarkView.findViewById(R.id.btnMinus);
		btnMinus.setOnClickListener(this);

		copyToClipboardView = li.inflate(R.layout.copytoclipboarddialog, null);
		builder = new AlertDialog.Builder(this);
		builder.setTitle("Copy to clipboard");
		builder.setView(copyToClipboardView);
		builder.setPositiveButton("OK", this);
		builder.setNeutralButton("Cancel", this);
		copyToClipboardDialog = builder.create();
		fillSpinnerCategory();
		Button btnPlusClipboard = (Button) copyToClipboardView.findViewById(R.id.btnPlusClipboard);
		btnPlusClipboard.setOnClickListener(this);
		Button btnMinusClipboard = (Button) copyToClipboardView.findViewById(R.id.btnMinusClipboard);
		btnMinusClipboard.setOnClickListener(this);

		View prevButton = findViewById(R.id.btnPrev);
		prevButton.setOnClickListener(this);
		prevButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				changeBook('p');
				return true;
			}
		});

		View nextButton = findViewById(R.id.btnNext);
		nextButton.setOnClickListener(this);
		nextButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				changeBook('n');
				return true;
			}
		});

		View txtCurrent = findViewById(R.id.txtCurrent);
		txtCurrent.setOnClickListener(this);
		txtCurrent.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				startActivity(new Intent(BiblesOfflineActivity.this, BrowseBibleActivity.class));
				return true;
			}
		});

//		txtCurrent.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View view) {
//				startActivity(new Intent(BiblesOfflineActivity.this, PocketSphinxActivity.class));
//				return true;
//			}
//		});

		View btnFullscreen = findViewById(R.id.btnFullscreen);
		btnFullscreen.setOnClickListener(this);
		View btnMenu = findViewById(R.id.btnMenu);
		btnMenu.setOnClickListener(this);
//		btnMenu.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View view) {
//				startActivity(new Intent(BiblesOfflineActivity.this, PDFViewer.class));
//				return true;
//			}
//		});
		btnListen = findViewById(R.id.btnListen);
		btnListen.setOnClickListener(this);
		btnListen.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				startActivity(new Intent(BiblesOfflineActivity.this, Voice.class));
				return true;
			}
		});
		View btnZoomIn = findViewById(R.id.btnZoomIn);
		btnZoomIn.setOnClickListener(this);
		View btnZoomOut = findViewById(R.id.btnZoomOut);
		btnZoomOut.setOnClickListener(this);

		TextView txtBibleName = (TextView) findViewById(R.id.txtBibleName);
		txtBibleName.setOnClickListener(this);
		txtBibleName.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				startActivity(new Intent(BiblesOfflineActivity.this, DownloadBible.class));
				return true;
			}
		});

		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle(R.string.selectBibleVersion);
		biblesAdapter = new ArrayAdapter<String>(this, R.layout.listitemmedium, bibleList);
		viewBibles = new ListView(this);
		viewBibles.setAdapter(biblesAdapter);
		viewBibles.setOnItemClickListener(this);
		ad.setView(viewBibles);
		dialogBibles = ad.create();
		dialogBibles.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		readPreference();
		Util.copyAssetsFilesToSdCard(this);

		LinearLayout bottomBar = (LinearLayout) findViewById(R.id.bottomBar);
		if (isFullScreen) {
			bottomBar.setVisibility(View.GONE);
		} else {
			bottomBar.setVisibility(View.VISIBLE);
		}
		applyParallel(isParallel);

		adapter = new DisplayVerseAdapter(this, R.layout.row, verseList, currentFontSize);
		setListAdapter(adapter);
		registerForContextMenu(getListView());

		txtEmpty2 = (TextView) findViewById(R.id.txtEmpty2);
		ListView listviewParallel = (ListView) findViewById(R.id.listviewParallel);
		parallelAdapter = new DisplayVerseAdapter(this, R.layout.row, verseParallelList, currentFontSize);
		listviewParallel.setAdapter(parallelAdapter);
		listviewParallel.setEmptyView(txtEmpty2);

		updateBibleFontSize();
		updateBookLanguage();

		//history dialog
		ad = new AlertDialog.Builder(this);
		ad.setTitle(R.string.history);
		viewHistory = new ListView(this);
		historyAdapter = new DisplayHistoryAdapter(this, R.layout.listitemmedium, historyList, currentBookLanguage);
		viewHistory.setAdapter(historyAdapter);
		viewHistory.setOnItemClickListener(this);
		ad.setView(viewHistory);
		dialogHistory = ad.create();
		dialogHistory.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (!fromBookmarks && (savedInstanceState == null)) {
			// Show the ProgressDialog on this thread
			this.pd = ProgressDialog.show(this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.loading), true, false);
			if (txtEmpty != null) {
				txtEmpty.setText(getResources().getString(R.string.no_bibles));
			}
			new LoadingTask().execute((Object)null);
		} else {
			List<String> bibles = databaseHelper.getBibleNameList();
			populateBibleList(bibles);
			displayBible(currentBibleFilename, currentChapterIdx);
		}

		getListView().setOnScrollListener(new OnScrollListener() {
			private boolean scroll = false;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				scroll = scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL;
			}

			@Override
			public void onScroll(AbsListView view, final int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
				if (view.equals(getListView()) && isParallel && scroll) {
					final ListView listviewParallel = (ListView) findViewById(R.id.listviewParallel);
					listviewParallel.post(new Runnable(){
						public void run() {
							listviewParallel.setSelection(firstVisibleItem);
							listviewParallel.setSelected(false);
						}});
				}
			}
		});
		(new Sync(this)).execute();
		//checkVoiceRecognition();
		mGestureView = findViewById(R.id.linearList);
		final ViewConfiguration viewConfig = ViewConfiguration.get(this);
		mViewScaledTouchSlop = viewConfig.getScaledTouchSlop();
		mGestureView.setOnTouchListener(this);
//		mScaleDetector = new ScaleGestureDetector(this, new MyPinchListener());
//		mGestureView.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				//inspect the event.
//				mScaleDetector.onTouchEvent(event);
//				return true;
//			}
//		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		DisplayVerse verse = verseList.get(info.position);
		if (verse == null) return;
        if (!verse.isBookmark()) {
            menu.add(Menu.NONE, Menu.FIRST+2, Menu.NONE, R.string.addBookmark);
        } else {
            menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.editBookmark).setVisible(false);
            menu.add(Menu.NONE, Menu.FIRST+1, Menu.NONE, R.string.removeBookmark);
        }
		menu.add(Menu.NONE, Menu.FIRST+3, Menu.NONE, R.string.copyToClipboard);
		menu.add(Menu.NONE, Menu.FIRST+4, Menu.NONE, R.string.share);
		menu.add(Menu.NONE, Menu.FIRST+5, Menu.NONE, R.string.highlight).setVisible(false);
		menu.add(Menu.NONE, Menu.FIRST+6, Menu.NONE, R.string.send_to_bae).setVisible(false);
		menu.add(Menu.NONE, Menu.FIRST+7, Menu.NONE, R.string.send_to_note).setVisible(false);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		int index = info.position;

		DisplayVerse verse = verseList.get(index);
		if (verse == null) return false;
		TextView txtVerse = (TextView) bookmarkView.findViewById(R.id.txtVerse);

		switch (item.getItemId()) {
			case Menu.FIRST : //edit Bookmark
				String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
				int book = Integer.parseInt(arrBookChapter[0]);
				int chapter = Integer.parseInt(arrBookChapter[1]);
				Bookmark bm = databaseHelper.getBookmark(book, chapter, verse.getVerseNumber());
				bookmarkVerseStart = bm.getVerseStart();
				bookmarkVerseEnd = bm.getVerseEnd();
				txtVerse.setTextSize(currentFontSize);
				StringBuilder sb = new StringBuilder();
				for (int j=bookmarkVerseStart; j<=bookmarkVerseEnd; j++) {
					DisplayVerse v = verseList.get(index + (j-bookmarkVerseStart));
					sb.append(Util.parseVerse(v.getVerse())).append(" ");
				}
				txtVerse.setText(sb.substring(0, sb.length()-1));
				refreshBookNameOnBookmarkDialog();
				Spinner spnCategory = (Spinner) bookmarkView.findViewById(R.id.spnCategory);
				for (int i = 0; i < spnCategory.getAdapter().getCount(); i++) {
					String categoryName = (String) spnCategory.getAdapter().getItem(i);
					if (categoryName.equals(bm.getCategoryName())) {
						spnCategory.setSelection(i);
						break;
					}
				}
				bookmarkDialog.show();
				return true;
			case Menu.FIRST+1 : //remove Bookmark
				final int verseNumber = verse.getVerseNumber();
				new AlertDialog.Builder(this)
						.setTitle(R.string.removeBookmark)
						.setMessage(R.string.reallyRemoveBookmark)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
								int book = Integer.parseInt(arrBookChapter[0]);
								int chapter = Integer.parseInt(arrBookChapter[1]);
								databaseHelper.removeBookmark(book, chapter, verseNumber);
								displayBible(currentBibleFilename, currentChapterIdx, false);
							}

						})
						.setNegativeButton(R.string.no, null)
						.show();
				return true;
			case Menu.FIRST+2 : //add Bookmark
				bookmarkVerseStart = verse.getVerseNumber();
				bookmarkVerseEnd = verse.getVerseNumber();
				txtVerse.setTextSize(currentFontSize);
				txtVerse.setText(Util.parseVerse(verse.getVerse()));
				refreshBookNameOnBookmarkDialog();
				bookmarkDialog.show();
				return true;
			case Menu.FIRST+3 : //copy verse to clipboard
				copyOrShare = 'c';
				bookmarkVerseStart = verse.getVerseNumber();
				bookmarkVerseEnd = verse.getVerseNumber();
				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
				txtVerse.setTextSize(currentFontSize);
				txtVerse.setText(Util.parseVerse(verse.getVerse()));
				refreshBookNameOnCopyToClipboardDialog();
				copyToClipboardDialog.setTitle("Copy to clipboard");
				copyToClipboardDialog.show();
				return true;
			case Menu.FIRST+4 : //share verse
				copyOrShare = 's';
				bookmarkVerseStart = verse.getVerseNumber();
				bookmarkVerseEnd = verse.getVerseNumber();
				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
				txtVerse.setTextSize(currentFontSize);
				txtVerse.setText(Util.parseVerse(verse.getVerse()));
				refreshBookNameOnCopyToClipboardDialog();
				copyToClipboardDialog.setTitle("Share verse");
				copyToClipboardDialog.show();
				return true;
			case Menu.FIRST+5: //highlight
				copyOrShare = 'h';
				bookmarkVerseStart = verse.getVerseNumber();
				bookmarkVerseEnd = verse.getVerseNumber();
				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
				txtVerse.setTextSize(currentFontSize);
				txtVerse.setText(Util.parseVerse(verse.getVerse()));
				refreshBookNameOnBookmarkDialog();
				copyToClipboardDialog.setTitle("Highlight");
				copyToClipboardDialog.show();
				return true;
			case Menu.FIRST+6 : //send to bae
				copyOrShare = 'b';
				bookmarkVerseStart = verse.getVerseNumber();
				bookmarkVerseEnd = verse.getVerseNumber();
				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
				txtVerse.setTextSize(currentFontSize);
				txtVerse.setText(Util.parseVerse(verse.getVerse()));
				refreshBookNameOnCopyToClipboardDialog();
				copyToClipboardDialog.setTitle("Send to Bae");
				copyToClipboardDialog.show();
				return true;
			case Menu.FIRST+7 : //send to note
				copyOrShare = 'n';
				bookmarkVerseStart = verse.getVerseNumber();
				bookmarkVerseEnd = verse.getVerseNumber();
				txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
				txtVerse.setTextSize(currentFontSize);
				txtVerse.setText(Util.parseVerse(verse.getVerse()));
				refreshBookNameOnCopyToClipboardDialog();
				copyToClipboardDialog.setTitle("Send to note");
				copyToClipboardDialog.show();
				return true;
		}

		return super.onContextItemSelected(item);
	}

	public void saveBookNumber () {
		String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
		int bookNo = Integer.parseInt(arrBookChapter[0]);
		SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
		editor.putInt(POSITION_BOOK, bookNo);
		editor.putInt(CHAPTER_INDEX, currentChapterIdx);
		editor.apply();
	}

	public void dismissDialogs() {
	    if (dialogBibles != null) dialogBibles.dismiss();
	    if (dialogHistory != null) dialogHistory.dismiss();
	    if (bookmarkDialog != null) bookmarkDialog.dismiss();
	    if (copyToClipboardDialog != null) copyToClipboardDialog.dismiss();
	    if (footnoteDialog != null) footnoteDialog.dismiss();
	    if (pd != null) pd.dismiss();
    }

	private class LoadingTask extends AsyncTask<Object, Void, Object> {
		@Override
		protected Object doInBackground(Object... arg) {
			readBibleBookName();
			String[] arrBibles = readBibleFiles();
			populateBibleList(arrBibles);
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (pd != null) {
				pd.dismiss();
			}
			displayBible(currentBibleFilename, currentChapterIdx);
		}
	}

	private void populateBibleList(String[] arrBibles) {
		List<String> bibles = new ArrayList<String>();
		if (arrBibles != null) {
			Collections.addAll(bibles, arrBibles);
		}
		populateBibleList(bibles);
	}
	
	private void populateBibleList(List<String> bibles) {
		bibleList.clear();		
		for (String bible : bibles) {
			bibleList.add(bible);
		}
		biblesAdapter.notifyDataSetChanged();
		if (bibles.size() == 0) return;
		BibleVersion bibleVersion;
		if (currentBibleFilename == null || currentBibleFilename.equals("")) {
			bibleVersion = databaseHelper.getBibleVersionByBibleName(bibles.get(0));				
		} else {
			bibleVersion = databaseHelper.getBibleVersionByFileName(currentBibleFilename);				
			if (bibleVersion == null) {
				bibleVersion = databaseHelper.getBibleVersionByBibleName(bibles.get(0));
			}
		}
		currentBibleFilename = bibleVersion.getFileName();
		currentBibleName = bibleVersion.getBibleName();
		handler.post(new Runnable() {
			@Override
			public void run() {
				updateBibleInfo();
			}
		});
	}
	
	private void updateBibleFontSize() {
		adapter.updateFontSize(currentFontSize);
		adapter.notifyDataSetChanged();
		parallelAdapter.updateFontSize(currentFontSize);
		parallelAdapter.notifyDataSetChanged();
		
		TextView txtFootnote = (TextView) footnoteView.findViewById(R.id.txtFootnote);
		txtFootnote.setTextSize(currentFontSize);
	}
	
	private void updateBookLanguage() {
		TextView title = (TextView) findViewById(R.id.txtCurrent);
		String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
  		int book = Integer.parseInt(arrBookChapter[0]);
  		int chapter = Integer.parseInt(arrBookChapter[1]);
		
  		title.setText(arrActiveBookName[book - 1] + " " + chapter);
	}

	private void readPreference() {
		//SpeechRecognizer
		SharedPreferences preference = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		currentChapterIdx = preference.getInt(CHAPTER_INDEX, 0);
		if (currentChapterIdx < 0 || currentChapterIdx >= arrVerseCount.length) {
			currentChapterIdx = 0;
		}
		currentBibleFilename = preference.getString(POSITION_BIBLE_NAME, "");
		currentBibleFilename2 = preference.getString(POSITION_BIBLE_NAME_2, "");
		currentFontSize = preference.getInt(FONT_SIZE, 18);
		isFullScreen = preference.getBoolean(FULL_SCREEN, false);
		isParallel = preference.getBoolean(PARALLEL, false);
		
		SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		currentBookLanguage = defaultPrefs.getString(BOOK_LANGUAGE, LANG_ENGLISH);
	}
	
	private void displayBible(String bibleFilename, int chapterIndex) {
		displayBible(bibleFilename, chapterIndex, true);
	}

	private void displayBible(String bibleFilename, int chapterIndex, boolean resetScroll) {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			txtEmpty.setText(getResources().getString(R.string.sdcard_error));
			Toast.makeText(this, R.string.sdcardNotReady, Toast.LENGTH_LONG).show();
			return;
		}
		
		File sdcard = Environment.getExternalStorageDirectory();
		
		if (chapterIndex == -1) { //no bible available
			TextView current = (TextView) findViewById(R.id.txtCurrent);
			current.setText("Error");
			return;
		}
		
		File file = new File(sdcard, BIBLE_FOLDER + "/" + bibleFilename);
		String indexFileName = file.getAbsolutePath().replaceAll(".ont", ".idx");
		File fIndex = new File(indexFileName);
		
		String[] arrBookChapter = arrVerseCount[chapterIndex]
				.split(";");
		int verseCount = Integer.parseInt(arrBookChapter[2]);
		if (!isOpen)
			databaseHelper.open();
		List<Integer> bookmarkList = databaseHelper.getBookmarkVerseStartByChapterIndex(chapterIndex);
		List<Integer> highlighedList = databaseHelper.getHighlightVerseStartByChapterIndex(chapterIndex);

		verseList.clear();
		BufferedReader br = null;
		try {
			DataInputStream is = new DataInputStream(new FileInputStream(fIndex));
			is.skip(chapterIndex*4);			
			int startOffset = is.readInt();
			is.close();
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 8192);
			br.skip(startOffset);
			String line = "";
			boolean prevBreakParagraph = false;
			boolean verseNotAvailable = true;
			for (int i = 1; i <= verseCount; i++) {
				line = br.readLine();
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				boolean breakParagraph = false;
				
				line = line.replaceAll("<CL>", "\n");
				int posCM = line.indexOf("<CM>");
				if (posCM > -1) {
					if (!line.endsWith("<CM>")) {
						String afterCM = line.substring(posCM + "<CM>".length()).trim();
						if (afterCM.startsWith("<") && afterCM.endsWith(">")) {
							breakParagraph = true;
							line = line.substring(0, posCM) + afterCM;
						}
					} else {
						breakParagraph = true;
						line = line.substring(0, line.length()-"<CM>".length());
					}
				}
				
				line = line.replaceAll("<CM>", "\n\n");
				line = line.replaceAll("\n\n \n\n", "\n\n");
				line = line.replaceAll("\n\n\n\n", "\n\n");
				
				boolean bookmarked = false;
				int highlighted = 0;
				if (!highlighedList.isEmpty())
					highlighted = highlighedList.get(i-1);
				if (bookmarkList.contains(Integer.valueOf(i))) {
					bookmarked = true;
				}
				
				if (prevBreakParagraph) {
					verseList.add(new DisplayVerse(i, line, bookmarked, highlighted, true));
				} else {
					verseList.add(new DisplayVerse(i, line, bookmarked, highlighted, false));
				}
				prevBreakParagraph = breakParagraph;
				verseNotAvailable = false;
			}
			
			if (verseNotAvailable) {
				if (chapterIndex < 929) {
					txtEmpty.setText(getResources().getString(R.string.no_ot));
				} else {
					txtEmpty.setText(getResources().getString(R.string.no_nt));
				}
			}
			
			adapter.notifyDataSetChanged();
			
			if (fromBookmarks) {
				getListView().post(new Runnable() {
					@Override
					public void run() {						
						getListView().setSelection(bookmarkVerseStart-1);
					}
				});
			} else if (resetScroll) {
				getListView().post(new Runnable() {
					@Override
					public void run() {
						getListView().setSelection(0);
					}
				});
			}
			
			//update history
			boolean alreadyInHistory = false;
			for (int i=0; i < historyList.size(); i++) {
				if (historyList.get(i).intValue() == chapterIndex) {
					alreadyInHistory = true;
					break;
				}
			}
			if (!alreadyInHistory) {
				if (historyList.size() < 10) {
					historyList.add(0);
				}
				for (int i=historyList.size()-1; i > 0; i--) {
					historyList.set(i, historyList.get(i-1).intValue());
				}
				historyList.set(0, chapterIndex);
				historyAdapter.notifyDataSetChanged();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {			
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		//added parallel
		if (isParallel && currentBibleFilename2 != null && !"".equals(currentBibleFilename2)) {
			file = new File(sdcard, BIBLE_FOLDER + "/" + currentBibleFilename2);
			indexFileName = file.getAbsolutePath().replaceAll(".ont", ".idx");
			fIndex = new File(indexFileName);
			
			verseParallelList.clear();
			br = null;
			try {
				DataInputStream is = new DataInputStream(new FileInputStream(fIndex));
				is.skip(chapterIndex*4);			
				int startOffset = is.readInt();
				is.close();
				
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 8192);
				br.skip(startOffset);
				String line = "";
				boolean prevBreakParagraph = false;
				boolean verseNotAvailable = true;
				for (int i = 1; i <= verseCount; i++) {
					line = br.readLine();
					line = line.trim();
					if (line.length() == 0) {
						continue;
					}
					boolean breakParagraph = false;
					
					line = line.replaceAll("<CL>", "\n");
					int posCM = line.indexOf("<CM>");
					if (posCM > -1) {
						if (!line.endsWith("<CM>")) {
							String afterCM = line.substring(posCM + "<CM>".length()).trim();
							if (afterCM.startsWith("<") && afterCM.endsWith(">")) {
								breakParagraph = true;
								line = line.substring(0, posCM) + afterCM;
							}
						} else {
							breakParagraph = true;
							line = line.substring(0, line.length()-"<CM>".length());
						}
					}
					
					line = line.replaceAll("<CM>", "\n\n");
					line = line.replaceAll("\n\n \n\n", "\n\n");
					line = line.replaceAll("\n\n\n\n", "\n\n");
					
					boolean bookmarked = false;
					int highlight = 0;
					
					if (prevBreakParagraph) {
						verseParallelList.add(new DisplayVerse(i, line, bookmarked, highlight, true));
					} else {
						verseParallelList.add(new DisplayVerse(i, line, bookmarked, highlight, false));
					}
					prevBreakParagraph = breakParagraph;
					verseNotAvailable = false;
				}
				
				if (verseNotAvailable) {
					if (chapterIndex < 929) {
						txtEmpty2.setText(getResources().getString(R.string.no_ot));
					} else {
						txtEmpty2.setText(getResources().getString(R.string.no_nt));
					}
				}
				
				parallelAdapter.notifyDataSetChanged();
				
				final ListView listviewParallel = (ListView) findViewById(R.id.listviewParallel);			
				if (fromBookmarks) {
					listviewParallel.post(new Runnable() {
						@Override
						public void run() {						
							listviewParallel.setSelection(bookmarkVerseStart-1);
						}
					});
				} else if (resetScroll) {
					listviewParallel.post(new Runnable() {
						@Override
						public void run() {
							listviewParallel.setSelection(0);
						}
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		updateBookLanguage();
		
		if (fromBookmarks) {
			fromBookmarks = false;
		}
		saveBookNumber();
	}
	
	private void readBibleBookName() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcard = Environment.getExternalStorageDirectory();
			File bookNameFolder = new File(sdcard.getPath() + BOOKNAME_FOLDER);
			if (!bookNameFolder.isDirectory()) {
				boolean success = bookNameFolder.mkdirs();
				if (!success) {
					return;
				}
			}
			File[] bookNameFiles = bookNameFolder.listFiles();
			if (bookNameFiles.length == 0) {
				try {
					File fEnglish = new File(bookNameFolder, LANG_ENGLISH + ".bkn");
					PrintWriter outEnglish = new PrintWriter(fEnglish, "UTF-8");
					for (String bookName : arrBookName) {
						String str = bookName;
						if ("Judges".equals(bookName)) {
							str = str + ";;Judg";
						} else if ("Jude".equals(bookName)) {
							str = str + ";;Jude";
						} else if ("John".equals(bookName)) {
							str = str + ";;Jn";
						} else if ("1 John".equals(bookName)) {
							str = str + ";;1 Jn";
						} else if ("2 John".equals(bookName)) {
							str = str + ";;2 Jn";
						} else if ("3 John".equals(bookName)) {
							str = str + ";;3 Jn";
						} else if ("Philemon".equals(bookName)) {
							str = str + ";;Phm";
						}
						outEnglish.println(str);
					}
					outEnglish.flush();
					outEnglish.close();
					
					File fIndo = new File(bookNameFolder, LANG_BAHASA + ".bkn");
					PrintWriter outIndo = new PrintWriter(fIndo, "UTF-8");
					for (String bookName : arrBookNameIndo) {
						outIndo.println(bookName);
					}
					outIndo.flush();
					outIndo.close();
					
				} catch (Exception e) {
					Log.d(TAG, "Error write bookname file", e);
				} 
			}
			
			bookNameFolder = new File(sdcard.getPath() + BOOKNAME_FOLDER);
			bookNameFiles = bookNameFolder.listFiles();
			File bookNameFile = new File(bookNameFolder, currentBookLanguage + ".bkn");
			if (bookNameFile.isFile()) {
				readBookNameFile(bookNameFile);
			} else {
				loadDefaultBookName();
			}
		}
	}

	private void readBookNameFile(File bookNameFile) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(bookNameFile), "UTF-8"), 8192);
			String line = null;
			int i = 0;
			while (((line = br.readLine()) != null) && (i < 66)) {
				line = line.trim();
				String bookName = line;
				String abbr = "";
				if (bookName.startsWith("1") || bookName.startsWith("2") || bookName.startsWith("3")) {
	            	abbr = bookName.substring(0,5);
	            } else {
	            	abbr = bookName.substring(0,3);
	            }
				if (line.indexOf(";;") > -1) {
					int pos = line.indexOf(";;");
					bookName = line.substring(0, pos);
					abbr = line.substring(pos + 2);
				}
				arrActiveBookName[i] = bookName;
				arrActiveBookAbbr[i] = abbr;
				i++;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error Read Book Name " + bookNameFile, e);
			loadDefaultBookName();
		}
	}
	
	private String[] readBibleFiles() {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Log.d(TAG, "SD CARD not available");
			handler.post(new Runnable() {
				@Override
				public void run() {
					txtEmpty.setText(getResources().getString(R.string.sdcard_error));
					Toast.makeText(BiblesOfflineActivity.this, R.string.sdcardNotReady, Toast.LENGTH_LONG).show();
				}
			});
			return null;
		}
		
		File sdcard = Environment.getExternalStorageDirectory();
		File bibleFolder = new File(sdcard.getPath() + BIBLE_FOLDER);
		List<BibleVersion> bibleList = databaseHelper.getAllBibleVersion();
		String[] result = null;
		List<String> bibleNames = new ArrayList<String>();
		StringBuffer fileNames = new StringBuffer();

		if (!bibleFolder.isDirectory()) {
			boolean success = bibleFolder.mkdirs();			
			Log.d(TAG, "Creating bible directory success: " + success);
		} else {
			File[] arrFile = bibleFolder.listFiles();
			if (arrFile != null && arrFile.length > 0) {
				Log.d(TAG, "Found " + arrFile.length
						+ " file(s) in bible directory");
				for (final File bibleFile : arrFile) {
					if (!bibleFile.getName().toLowerCase().endsWith(".ont")) continue;
					fileNames.append(",'").append(bibleFile.getName()).append("'");
					boolean doIndexing = false;
					BibleVersion compareBible = new BibleVersion();
					compareBible.setFileName(bibleFile.getName());
					BibleVersion bibleOnDb = null;
					int indexBible = bibleList.indexOf(compareBible);
					if (indexBible == -1) {
						doIndexing = true;
						bibleOnDb = new BibleVersion();
						bibleOnDb.setId(-1L);
					} else {
						bibleOnDb = bibleList.get(indexBible);
						if (!bibleOnDb.getLastModified().equals(
								bibleFile.length())) {
							doIndexing = true;
						}
					}
					if (doIndexing) {
						Log.d(TAG, "Indexing " + bibleFile.getName());
						if (pd.isShowing()) {
							handler.post(new Runnable() {
		                        public void run() {
		                        		pd.setMessage("Indexing " + bibleFile.getName());
		                        }
			                });
						}
						String indexName = bibleFile.getAbsolutePath().replaceAll(".ont", ".idx");
						File fIndex = new File(indexName);

						DataOutputStream out = null;
						BufferedReader raf = null;
						InputStreamReader in = null;
						char[] bufChar = new char[8192];
						int offset = 0;	
						int startOffset = 0;
						try {
							out = new DataOutputStream(new FileOutputStream(fIndex));
							
							in = new InputStreamReader(new FileInputStream(bibleFile), "UTF-8");
							int numChar = in.read(bufChar);
							startOffset = startOffset + numChar;
							
							int j = 0;
							int verseCount = 1;
							int verseCountIdx = 0;		
							int totalVerse = Integer.valueOf(arrVerseCount[verseCountIdx].substring(arrVerseCount[verseCountIdx].lastIndexOf(";") + 1));
							
							byte[] bomUtf8 = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
							if (Character.toString(bufChar[0]).equals(new String(bomUtf8, "UTF-8"))) {
								offset = 1;
							}
							out.writeInt(offset);
							
							int prevBook = 0;
							boolean done = false;
							int eolLength = 0;
							while (numChar > 0 && !done) {
								for (int i=0; i < numChar; i++) {
									if (bufChar[i] == '\n') {
										if (eolLength == 0) {
											if (bufChar[i-1] == '\r') {
												eolLength = 2;
											} else {
												eolLength = 1;
											}
										}
										
										offset = startOffset - numChar + i;
										
										verseCount++;
										if (verseCount > totalVerse) {
											verseCount = 1;
											if (verseCountIdx < arrVerseCount.length-1) {
												final int book = Integer.valueOf(arrVerseCount[verseCountIdx].substring(0, arrVerseCount[verseCountIdx].indexOf(";")));
												if (prevBook != book) {
													prevBook = book;
													if (pd.isShowing()) {
														handler.post(new Runnable() {
									                        public void run() {
									                            pd.setMessage("Indexing " + bibleFile.getName() + " " + arrBookName[book-1]);
									                        }
										                });
													}
												}
												out.writeInt(offset + 1);
												verseCountIdx++;
												totalVerse = Integer.valueOf(arrVerseCount[verseCountIdx].substring(arrVerseCount[verseCountIdx].lastIndexOf(";") + 1));
											} else {
												done = true;
												break;
											}
										}
									}
								}																
								numChar = in.read(bufChar);
								startOffset = startOffset + numChar;
								j++;
							}
							in.close();
							in = null;
							if (!done) {
								throw new IndexOutOfBoundsException("Bible file " + bibleFile.getName()	+ " is not valid");
							}
							
							raf = new BufferedReader(new InputStreamReader(new FileInputStream(bibleFile), "UTF-8"), 8192);
							raf.skip(offset+1);
							String line = null;
							String bibleName = bibleFile.getName();
							String about=null;
							boolean startAbout = false;
							StringBuffer sbAbout = new StringBuffer();
							while ((line = raf.readLine()) != null) {
								if (line.startsWith("description=")) {
									bibleName = line.substring("description=".length());
								}
								if (line.startsWith("about=")) {
									sbAbout = sbAbout.append(line.substring("about=".length()));
									if (line.endsWith("\\")) {
										startAbout = true;
										sbAbout.delete(sbAbout.length()-1,sbAbout.length());
									}
								} else {
									if (startAbout) {
										sbAbout.append(" ").append(line);
										if (!line.endsWith("\\")) {
											startAbout = false;
										} else {
											sbAbout.delete(sbAbout.length()-1,sbAbout.length());
										}
									}
								}
							}
							if (sbAbout.length() == 0) {
								sbAbout.append(bibleName);
							}
							about = sbAbout.toString();
							
							bibleOnDb.setEolLength(eolLength);
							bibleOnDb.setFileName(bibleFile.getName());
							bibleOnDb.setLastModified(bibleFile.length());
							bibleOnDb.setBibleName(bibleName);
							bibleOnDb.setAbout(about);
							databaseHelper.saveOrUpdateBibleVersion(bibleOnDb);
							currentBibleName = bibleName;
							currentBibleFilename = bibleFile.getName();
							Log.d(TAG, "File " + bibleFile.getName()
									+ " indexed successfully");
							bibleNames.add(bibleName);
						} catch (Exception e) {
							Log.d(TAG, "Error reading file: " + bibleFile, e);
							bibleFile.delete();
						} finally {
							try {
								if (in != null) 
									in.close();
								if (out != null)
									out.close();
								if (raf != null)
									raf.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
						}
					} else {
						BibleVersion bibleVersion = bibleList.get(indexBible);
						bibleNames.add(bibleVersion.getBibleName());
						Log.d(TAG, "File " + bibleFile.getName() + " already indexed");
					}
				}
			}
		}
		if (bibleNames.size() > 0) {
			result = new String[bibleNames.size()];
			result = bibleNames.toArray(result);
			Arrays.sort(result);
		}
		databaseHelper.deleteInvalidBible(fileNames);
		return result;
	}

    @Override
	public void onClick(View v) {
		if (currentChapterIdx == -1) return;
		TextView txtVerse;
		String str;
		StringBuffer sb; 
		switch (v.getId()) {
		case R.id.btnNext:
			if (currentChapterIdx == arrVerseCount.length - 1) {
				currentChapterIdx = 0;
			} else {
				currentChapterIdx++;
			}
			displayBible(currentBibleFilename, currentChapterIdx);
			break;
		case R.id.btnPrev:
			if (currentChapterIdx == 0) {
				currentChapterIdx = arrVerseCount.length - 1;
			} else {
				currentChapterIdx--;
			}
			displayBible(currentBibleFilename, currentChapterIdx);
			break;
		case R.id.txtCurrent:
			if (bibleList == null || bibleList.size() == 0) {
				Toast.makeText(this, R.string.gotoNoBible, Toast.LENGTH_LONG).show();
				break;
			}
			gotoBrowse = true;
			lastChapterIdx = currentChapterIdx;
			startActivity(new Intent(this, GoTo.class));
			break;
        case R.id.btnFullscreen:
            LinearLayout bottomBar = (LinearLayout) findViewById(R.id.bottomBar);
            if (bottomBar.getVisibility() == View.VISIBLE) {
                bottomBar.setVisibility(View.GONE);
                isFullScreen = true;
            } else {
                bottomBar.setVisibility(View.VISIBLE);
                isFullScreen = false;
            }
            break;
        case R.id.btnMenu:
            openOptionsMenu();
            break;
        case R.id.btnListen:
            if (bibleList == null || bibleList.size() == 0) {
                Toast.makeText(this, R.string.gotoNoBible, Toast.LENGTH_LONG).show();
                break;
            }
            gotoBrowse = true;
            lastChapterIdx = currentChapterIdx;
			//finish();
            startActivity(new Intent(this, ListenActivity.class));
            break;
//				LinearLayout bottomBar = (LinearLayout) findViewById(R.id.bottomBar);
//				if (bottomBar.getVisibility() == View.VISIBLE) {
//					bottomBar.setVisibility(View.GONE);
//					isFullScreen = true;
//				} else {
//					bottomBar.setVisibility(View.VISIBLE);
//					isFullScreen = false;
//				}
		case R.id.btnZoomIn:
			currentFontSize+=1;
			updateBibleFontSize();
			break;
		case R.id.btnZoomOut:
			currentFontSize-=1;
			updateBibleFontSize();
			break;	
		case R.id.btnPlus:
			if (bookmarkVerseEnd >= bookmarkVerseMax) return;
			bookmarkVerseEnd++;
			DisplayVerse verseToAdd = verseList.get(bookmarkVerseEnd-1);			
			txtVerse = (TextView) bookmarkView.findViewById(R.id.txtVerse);			
			str = txtVerse.getText().toString();
			sb = new StringBuffer(str);
			sb.append(" ");
			sb.append(Util.parseVerse(verseToAdd.getVerse()));
			txtVerse.setText(sb.toString());
			refreshBookNameOnBookmarkDialog();
			break;
		case R.id.btnMinus:
			if (bookmarkVerseEnd <= bookmarkVerseStart) return;
			DisplayVerse verseToDelete = verseList.get(bookmarkVerseEnd-1);
			bookmarkVerseEnd--;
			int lengthDelete = Util.parseVerse(verseToDelete.getVerse()).length() + 1;
			txtVerse = (TextView) bookmarkView.findViewById(R.id.txtVerse);			
			str = txtVerse.getText().toString();
			sb = new StringBuffer(str);
			sb.delete(sb.length()-lengthDelete, sb.length());
			txtVerse.setText(sb.toString());
			refreshBookNameOnBookmarkDialog();
			break;
		case R.id.btnPlusClipboard:
			if (bookmarkVerseEnd >= bookmarkVerseMax) return;
			bookmarkVerseEnd++;
			DisplayVerse verseToAddClipboard = verseList.get(bookmarkVerseEnd-1);			
			txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);			
			str = txtVerse.getText().toString();
			sb = new StringBuffer(str);
			sb.append(" ");
			sb.append(Util.parseVerse(verseToAddClipboard.getVerse()));
			txtVerse.setText(sb.toString());
			refreshBookNameOnCopyToClipboardDialog();
			break;
		case R.id.btnMinusClipboard:
			if (bookmarkVerseEnd <= bookmarkVerseStart) return;
			DisplayVerse verseToDeleteClipboard = verseList.get(bookmarkVerseEnd-1);
			bookmarkVerseEnd--;
			lengthDelete = Util.parseVerse(verseToDeleteClipboard.getVerse()).length() + 1;
			txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);			
			str = txtVerse.getText().toString();
			sb = new StringBuffer(str);
			sb.delete(sb.length()-lengthDelete, sb.length());
			txtVerse.setText(sb.toString());
			refreshBookNameOnCopyToClipboardDialog();
			break;
		case R.id.txtBibleName:
			//startActivity(new Intent(this, BaeActivity.class));
//			if (!SyncUtils.isMyServiceRunning(RandomMonthlyTheme.class, this))
//				startService(new Intent(this, GeneralSpeechRecognizerService.class));
//			else
//				stopService(new Intent(this, GeneralSpeechRecognizerService.class));

			if (!isParallel) {
				dialogBibles.show();
			} else {
				gotoSelectParallel = true;
				startActivity(new Intent(this, SelectParallelBible.class));
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
        dismissDialogs();
		//close database
		if (isOpen)
			databaseHelper.close();
		isOpen = false;
		if (!Util.isMyServiceRunning(RandomMonthlyTheme.class, this))
			startService(new Intent(this, RandomMonthlyTheme.class));
		super.onDestroy();
	}

	@Override
	public void onClick(DialogInterface v, int buttonId) {
		if (v.equals(bookmarkDialog)) {
			if (buttonId == DialogInterface.BUTTON_POSITIVE) {
				Spinner spnCategory = (Spinner) bookmarkView.findViewById(R.id.spnCategory);
				TextView txtVerse = (TextView) bookmarkView.findViewById(R.id.txtVerse);
				Long categoryId = databaseHelper.getCategoryIdByCategoryName((String)spnCategory.getSelectedItem());
				SimpleDateFormat isoFormat = new SimpleDateFormat(DB_DATE_FORMAT);
				String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
		  		int book = Integer.parseInt(arrBookChapter[0]);
		  		int chapter = Integer.parseInt(arrBookChapter[1]);
				Bookmark bm = new Bookmark();
				bm.setCategoryId(categoryId);
				bm.setBook(book);
				bm.setChapter(chapter);
				bm.setVerseStart(bookmarkVerseStart);
//				bm.setVerseEnd(bookmarkVerseEnd);
				bm.setHighlighted(0);
				bm.setContent(txtVerse.getText().toString());
				bm.setBible(currentBibleFilename.substring(0, currentBibleFilename.length()-4));
				bm.setBookmarkDate(isoFormat.format(new Date()));
				databaseHelper.insertReplaceBookmark(bm);
				displayBible(currentBibleFilename, currentChapterIdx, false);
			}
		} else if (v.equals(copyToClipboardDialog)) {
			if (buttonId == DialogInterface.BUTTON_POSITIVE) {
				TextView txtVerse = (TextView) copyToClipboardView.findViewById(R.id.txtVerse);
				TextView txtBook = (TextView) copyToClipboardView.findViewById(R.id.txtBook);
				if (copyOrShare == 'c') {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
					clipboard.setText(txtBook.getText() + " " + txtVerse.getText());
					String str = getResources().getString(R.string.copiedSuccess);
					String msg = String.format(str, txtBook.getText());
					Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
				} else if (copyOrShare == 's') {
					Intent i=new Intent(android.content.Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_SUBJECT, txtBook.getText());
					i.putExtra(Intent.EXTRA_TEXT, txtBook.getText() + " " + txtVerse.getText());
					startActivity(Intent.createChooser(i, "Share"));
				} else if (copyOrShare == 'b') {
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					try {
						jsonObject.put("scripture", txtBook.getText());
						jsonObject.put("timestamp", System.currentTimeMillis());
						jsonObject.put("category", "bae_sent");
						jsonArray.put(jsonObject);
						if (databaseHelper.insertScriptures(jsonArray.toString())) {
							SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
							editor.putBoolean(Constants.SCRIPTURES_SYNC, false);
							Toast.makeText(this, "Sent to BAE", Toast.LENGTH_SHORT).show();
						}
						else
							Toast.makeText(this, "Already sent to BAE", Toast.LENGTH_SHORT).show();

					} catch (JSONException e) {
						e.printStackTrace();
					}
					startActivity(new Intent(this, BaeActivity.class));
				} else if (copyOrShare == 'n') {
					Intent i=new Intent(this, NoteListActivity.class);
					i.putExtra(NotePadUtils.ARG_SCRIPTURE, txtBook.getText() + " " + txtVerse.getText());
					startActivity(i);
				}
				else if (copyOrShare == 'h') {
					SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
					SimpleDateFormat isoFormat = new SimpleDateFormat(DB_DATE_FORMAT);
					String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
					int book = Integer.parseInt(arrBookChapter[0]);
					int chapter = Integer.parseInt(arrBookChapter[1]);
					Bookmark bm = new Bookmark();
					bm.setBook(book);
					bm.setChapter(chapter);
					bm.setHighlighted(1);
					bm.setVerseStart(bookmarkVerseStart);
					bm.setVerseEnd(bookmarkVerseEnd);
					bm.setContent(txtVerse.getText().toString());
					bm.setBible(currentBibleFilename.substring(0, currentBibleFilename.length()-4));
					bm.setBookmarkDate(isoFormat.format(new Date()));
					databaseHelper.insertReplaceBookmark(bm);
					for (int i = bookmarkVerseStart; i <= bookmarkVerseEnd; i++){
						editor.putInt(currentChapterIdx+""+i, 1);
					}
					editor.commit();
					displayBible(currentBibleFilename, currentChapterIdx, false);
				}
			}
		}

	}

	private void updateBibleInfo() {
		TextView txtBibleName = (TextView) findViewById(R.id.txtBibleName);
		if (!isParallel) {
			txtBibleName.setText(currentBibleName);
		} else {
			String bible1 = currentBibleFilename;
			if (bible1 != null && bible1.length() > 4) {
				bible1 = bible1.substring(0, currentBibleFilename.length()-4);
				bible1 = bible1.toUpperCase();
			}
			String bible2 = currentBibleFilename2;
			if (bible2 != null && bible2.length() > 4) {
				bible2 = bible2.substring(0, currentBibleFilename2.length()-4);
				bible2 = bible2.toUpperCase();
			}
			txtBibleName.setText(bible1 + " / " + bible2);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// Save the current position
		Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
		editor.putInt(CHAPTER_INDEX, currentChapterIdx);
		editor.putString(POSITION_BIBLE_NAME, currentBibleFilename);
		editor.putString(POSITION_BIBLE_NAME_2, currentBibleFilename2);
		editor.putString(BOOK_LANGUAGE, currentBookLanguage);
		editor.putInt(FONT_SIZE, currentFontSize);
		editor.putBoolean(FULL_SCREEN, isFullScreen);
		editor.putBoolean(PARALLEL, isParallel);
		editor.apply();
		//save history
		databaseHelper.saveHistory(historyList);
		//close database
		databaseHelper.close();
		isOpen = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		databaseHelper.open();
		isOpen = true;
		databaseHelper.getHistory(historyList);
		historyAdapter.notifyDataSetChanged();
		
		fillSpinnerCategory();
		if (gotoPrefs) {
			gotoPrefs = false;
			SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			if (!defaultPrefs.getString(BOOK_LANGUAGE, LANG_ENGLISH).equals(currentBookLanguage)) {
				currentBookLanguage = defaultPrefs.getString(BOOK_LANGUAGE, LANG_ENGLISH);
				
				File sdcard = Environment.getExternalStorageDirectory();
				File bookNameFolder = new File(sdcard.getPath() + BOOKNAME_FOLDER);
				File bookNameFile = new File(bookNameFolder, currentBookLanguage + ".bkn");
				if (bookNameFile.isFile()) {					
					readBookNameFile(bookNameFile);
				} else {
					loadDefaultBookName();
				}
				
				updateBookLanguage();
			}
		} else if (gotoDownloadBible) {
			gotoDownloadBible = false;
			this.pd = ProgressDialog.show(this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.loading), true, false);
			txtEmpty.setText(getResources().getString(R.string.no_bibles));
			new LoadingTask().execute((Object)null);
		} else if (gotoBrowse) {
			gotoBrowse = false;
			SharedPreferences preference = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
			currentChapterIdx = preference.getInt(CHAPTER_INDEX, 0);
			if (lastChapterIdx != currentChapterIdx) {
				displayBible(currentBibleFilename, currentChapterIdx);
			}
		} else if (gotoDocuments) {
			gotoDocuments = false;
			SharedPreferences preference = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
			if (isFullScreen != preference.getBoolean(FULL_SCREEN, false)) {
				LinearLayout bottomBar = (LinearLayout) findViewById(R.id.bottomBar);
				if (isFullScreen) {
					bottomBar.setVisibility(View.GONE);
				} else {
					bottomBar.setVisibility(View.VISIBLE);
				}
			}
			if (isParallel != preference.getBoolean(PARALLEL, false)) {
				applyParallel(isParallel);
			}
		} else if (gotoSelectParallel) {
			gotoSelectParallel = false;
			SharedPreferences preference = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
			currentBibleFilename = preference.getString(POSITION_BIBLE_NAME, "");
			currentBibleFilename2 = preference.getString(POSITION_BIBLE_NAME_2, "");
//			currentBibleName = preference.getString(Constants.BIBLE_NAME, "");
			if ("".equals(currentBibleFilename2)) {
				isParallel = false;
				applyParallel(isParallel);
			}
			updateBibleInfo();
			displayBible(currentBibleFilename, currentChapterIdx, false);
		}
	}

	private void loadDefaultBookName() {
		currentBookLanguage = LANG_ENGLISH;
		Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
		editor.putString(BOOK_LANGUAGE, LANG_ENGLISH);
		editor.commit();
		for (int i=0; i<66; i++) {
			arrActiveBookName[i] = arrBookName[i];
			String bookName = arrBookName[i];
			String abbr = "";
			if (bookName.startsWith("1") || bookName.startsWith("2") || bookName.startsWith("3")) {
            	abbr = bookName.substring(0,5);
            } else {
            	abbr = bookName.substring(0,3);
            }
			arrActiveBookAbbr[i] = abbr;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	private void applyParallel(boolean isParallel) {
		LinearLayout linearParallel = (LinearLayout) findViewById(R.id.linearParallel);
		int height = 0;
		LinearLayout line = (LinearLayout) findViewById(R.id.horizontalLine);
		if (isParallel) {
			height = (int) (getWindowManager().getDefaultDisplay().getHeight() * 0.45);
			line.setVisibility(View.VISIBLE);
		} else {
			line.setVisibility(View.GONE);
		}
		int width = getWindowManager().getDefaultDisplay().getWidth();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
		linearParallel.setLayoutParams(params);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.parallel:
				if (bibleList == null || bibleList.size() < 2) {
					Toast.makeText(this, R.string.needMoreTranslation, Toast.LENGTH_LONG).show();
					return true;
				}
				isParallel = !isParallel;				
				applyParallel(isParallel);
				if (isParallel) {
					displayBible(currentBibleFilename, currentChapterIdx);
				}
				updateBibleInfo();
				if (isParallel && (currentBibleFilename2 == null || "".equals(currentBibleFilename2))) {
					gotoSelectParallel = true;
					startActivity(new Intent(this, SelectParallelBible.class));
				}
				return true;
//			case R.id.contactAuthor:
//				Intent i = new Intent(Intent.ACTION_SEND);
//				i.setType("message/rfc822");
//				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"astrodextro@gmail.com"});
//				i.putExtra(Intent.EXTRA_SUBJECT, "[OpenBibles] Question");
//				i.putExtra(Intent.EXTRA_TEXT   , "");
//			    startActivity(Intent.createChooser(i, "Send mail..."));
//				return true;
			case R.id.bookmark:
				String state = Environment.getExternalStorageState();
				if (!Environment.MEDIA_MOUNTED.equals(state)) {
					Toast.makeText(this, R.string.sdcardNotReady, Toast.LENGTH_LONG).show();
					return true;
				}
				startActivity(new Intent(this, BookmarksActivity.class));
				return true;
			case R.id.find:
				Intent find = new Intent(this, FindActivity.class);
				find.putExtra(CURRENT_BIBLE, currentBibleName);
				startActivity(find);
				return true;
			case R.id.about:
				AlertDialog.Builder ad = new AlertDialog.Builder(this);
				String[] arrImport = new String[] {"About " + currentBibleName, "About PB-Bible"};
				ListView viewChooseAbout = new ListView(this);
				ad.setView(viewChooseAbout);		
				final AlertDialog dialogChooseAbout = ad.create();
				dialogChooseAbout.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
				viewChooseAbout.setAdapter(new ArrayAdapter<String>(this, R.layout.listitemmedium, arrImport));
				viewChooseAbout.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						dialogChooseAbout.dismiss();
						if (position==0) {
							Intent i = new Intent(BiblesOfflineActivity.this, AboutBibleActivity.class);
							i.putExtra(CURRENT_BIBLE, currentBibleName);
							startActivity(i);
						} else if (position==1) {
							startActivity(new Intent(BiblesOfflineActivity.this, AboutActivity.class));
						}
					}
				});
				dialogChooseAbout.show();
				return true;	
			case R.id.download:
				gotoDownloadBible = true;
				startActivity(new Intent(this, DownloadBible.class));
				return true;
			case R.id.history:
				dialogHistory.show();
				return true;
			case R.id.help:
				Intent iHelp = new Intent(this, HelpActivity.class);
				iHelp.putExtra(FONT_SIZE, currentFontSize);
				iHelp.putExtra(HELP_CONTENT, R.string.help_main);
				startActivity(iHelp);
				return true;
//			case R.id.document:
////				state = Environment.getExternalStorageState();
////				if (!Environment.MEDIA_MOUNTED.equals(state)) {
////					Toast.makeText(this, R.string.sdcardNotReady, Toast.LENGTH_LONG).show();
////					return true;
////				}
////				startActivity(new Intent(this, DocumentsActivity.class));
//				startActivity(new Intent(this, NoteListActivity.class));
//				return true;
			case R.id.settings:
				gotoPrefs = true;
				startActivity(new Intent(this, SettingsActivity.class));
				//finish();
				return true;
//			case R.id.downloadBookname:
//				gotoPrefs = true;
//				startActivity(new Intent(this, DownloadBookname.class));
//				return true;
		}
		return false;
	}

	private void refreshBookNameOnBookmarkDialog() {
		String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
  		int book = Integer.parseInt(arrBookChapter[0]);
  		int chapter = Integer.parseInt(arrBookChapter[1]);
  		bookmarkVerseMax = Integer.parseInt(arrBookChapter[2]);
  		StringBuffer sbBook = new StringBuffer();
		
//		if (currentBookLanguage.equals(Constants.LANG_BAHASA)) {
//			sbBook.append(Constants.arrBookNameIndo[book - 1]).append(" ").append(chapter);
//		} else {
//			sbBook.append(Constants.arrBookName[book - 1]).append(" ").append(chapter);
//		}
  		sbBook.append(arrActiveBookName[book - 1]).append(" ").append(chapter);
		
		sbBook.append(":").append(bookmarkVerseStart);
		if (bookmarkVerseStart != bookmarkVerseEnd) {
			sbBook.append("-").append(bookmarkVerseEnd);
		}
		
		TextView txtBook = (TextView) bookmarkView.findViewById(R.id.txtBook);
		txtBook.setText(sbBook.toString());
	}
	
	private void refreshBookNameOnCopyToClipboardDialog() {
		String[] arrBookChapter = arrVerseCount[currentChapterIdx].split(";");
  		int book = Integer.parseInt(arrBookChapter[0]);
  		int chapter = Integer.parseInt(arrBookChapter[1]);
  		bookmarkVerseMax = Integer.parseInt(arrBookChapter[2]);
  		StringBuffer sbBook = new StringBuffer();
		
//		if (currentBookLanguage.equals(Constants.LANG_BAHASA)) {
//			sbBook.append(Constants.arrBookNameIndo[book - 1]).append(" ").append(chapter);
//		} else {
//			sbBook.append(Constants.arrBookName[book - 1]).append(" ").append(chapter);
//		}
  		sbBook.append(arrActiveBookName[book - 1]).append(" ").append(chapter);
		
		sbBook.append(":").append(bookmarkVerseStart);
		if (bookmarkVerseStart != bookmarkVerseEnd) {
			sbBook.append("-").append(bookmarkVerseEnd);
		}
		
		TextView txtBook = (TextView) copyToClipboardView.findViewById(R.id.txtBook);
		txtBook.setText(sbBook.toString());
	}
	
	private void fillSpinnerCategory() {
		Spinner spnCategory = (Spinner) bookmarkView.findViewById(R.id.spnCategory);
		List<String> categoryList = databaseHelper.getBookmarkCategoryList();
		String[] arrCategory = new String[categoryList.size()];
		arrCategory = categoryList.toArray(arrCategory);
		ArrayAdapter<String> aaCategory = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, arrCategory);
		aaCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnCategory.setAdapter(aaCategory);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent == viewHistory) {			
			dialogHistory.dismiss();
			currentChapterIdx = historyList.get(position);
			displayBible(currentBibleFilename, currentChapterIdx);
		} else if (parent == viewBibles) {
			dialogBibles.dismiss();
			String bibleName = bibleList.get(position);
			if (!currentBibleName.equals(bibleName)) {
				BibleVersion bibleVersion = databaseHelper.getBibleVersionByBibleName(bibleName);
				currentBibleFilename = bibleVersion.getFileName();
				currentBibleName = bibleName;
				updateBibleInfo();
				displayBible(currentBibleFilename, currentChapterIdx, false);
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  if (isParallel) {
		  applyParallel(isParallel);
	  }
	}

	public void changeBook(char nextPrev) {
        int bookNo = Util.getBookNo(currentChapterIdx);
		if (nextPrev == 'n') {
			if (bookNo == 66)
			    bookNo = 1;
            else
                bookNo++;
		}
		else if (nextPrev == 'p') {
            if (bookNo == 1)
                bookNo = 66;
            else
                bookNo--;
        }
        //int chapterNo = Util.getChapterNo(currentChapterIdx);
//		if (chapterNo >)
        String bookChapter = bookNo + ";1;";
        for (int i = Constants.arrBookStart[bookNo - 1]; i < Constants.arrVerseCount.length; i++) {
            if (Constants.arrVerseCount[i].startsWith(bookChapter)) {
                currentChapterIdx = i;
            }
        }

        displayBible(currentBibleFilename, currentChapterIdx);
    }

	public void createKWSFile() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < voiceFriendlyBookNames.length; i++)
        {
            sb.append("<s> ").append(voiceFriendlyBookNames[i].toLowerCase()).append(" </s>")
					.append(" (").append(voiceFriendlyBookNames[i].toLowerCase()).append(")")
					.append("\n");
        }
        Util.saveTextToFile(sb.toString());
    }

//	public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
//	public int mkFolder(String folderName){ // make a folder under Environment.DIRECTORY_DCIM
//		String state = Environment.getExternalStorageState();
//		if (!Environment.MEDIA_MOUNTED.equals(state)){
//			Log.d("myAppName", "Error: external storage is unavailable");
//			return 0;
//		}
//		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//			Log.d("myAppName", "Error: external storage is read only.");
//			return 0;
//		}
//		Log.d("myAppName", "External storage is not read only or unavailable");
//
//		if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
//				Manifest.permission.WRITE_EXTERNAL_STORAGE)
//				!= PackageManager.PERMISSION_GRANTED) {
//			Log.d("myAppName", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");
//			// Should we show an explanation?
//			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//				// Show an expanation to the user *asynchronously* -- don't block
//				// this thread waiting for the user's response! After the user
//				// sees the explanation, try again to request the permission.
//
//			} else {
//
//				// No explanation needed, we can request the permission.
//
//				ActivityCompat.requestPermissions(this,
//						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//						MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
//
//				// MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
//				// app-defined int constant. The callback method gets the
//				// result of the request.
//			}
//		}
//		File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),folderName);
//		int result = 0;
//		if (folder.exists()) {
//			Log.d("myAppName","folder exist:"+folder.toString());
//			result = 2; // folder exist
//		}else{
//			try {
//				if (folder.mkdir()) {
//					Log.d("myAppName", "folder created:" + folder.toString());
//					result = 1; // folder created
//				} else {
//					Log.d("myAppName", "creat folder fails:" + folder.toString());
//					result = 0; // creat folder fails
//				}
//			}catch (Exception ecp){
//				ecp.printStackTrace();
//			}
//		}
//		return result;
//	}
//
//	@Override
//	public void onRequestPermissionsResult(int requestCode,
//										   String permissions[], int[] grantResults) {
//		switch (requestCode) {
//			case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
//				// If request is cancelled, the result arrays are empty.
//				if (grantResults.length > 0
//						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//					// permission was granted, yay! Do the
//					// contacts-related task you need to do.
//
//				} else {
//
//					// permission denied, boo! Disable the
//					// functionality that depends on this permission.
//				}
//				return;
//			}
//
//			// other 'case' lines to check for other
//			// permissions this app might request
//		}
//	}

//    public void checkVoiceRecognition() {
//        // Check if voice recognition is present
//        PackageManager pm = getPackageManager();
//        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
//                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
//        if (activities.size() == 0) {
//            btnSpeak.setEnabled(false);
////            btnSpeak.setText("Voice recognizer not present");
//            Toast.makeText(this, "Voice recognizer not present",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void speak(View view) {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//
//        // Specify the calling package to identify your application
//        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
//                .getPackage().getName());
//
//        // Display an hint to the user about what he should say.
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello");
//
//        // Given an hint to the recognizer about what the user is going to say
//        //There are two form of language model available
//        //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
//        //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
//
//        // If number of Matches is not selected then return show toast message
////        if (msTextMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
////            Toast.makeText(this, "Please select No. of Matches from spinner",
////                    Toast.LENGTH_SHORT).show();
////            return;
////        }
//
//        //int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem()
//               // .toString());
//        // Specify how many results you want to receive. The results will be
//        // sorted where the first result is the one with higher confidence.
//        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 7);
//        //Start the Voice recognizer activity for the result.
//        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
//
//            //If Voice recognition is successful then it returns RESULT_OK
//            if(resultCode == RESULT_OK) {
//
//                ArrayList<String> textMatchList = data
//                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//
//                if (!textMatchList.isEmpty()) {
//                    // If first Match contains the 'search' word
//                    // Then start web search.
//                    if (textMatchList.get(0).contains("search")) {
//
//                        String searchQuery = textMatchList.get(0);
//                        searchQuery = searchQuery.replace("search","");
//                        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
//                        search.putExtra(SearchManager.QUERY, searchQuery);
//                        startActivity(search);
//                    } else {
////                        // populate the Matches
////                        mlvTextMatches
////                                .setAdapter(new ArrayAdapter<String>(this,
////                                        android.R.layout.simple_list_item_1,
////                                        textMatchList));
//                    }
//
////                    for (String book:
////                            voiceFriendlyBookNames) {
////                        if (textMatchList.contains(book))
////                            showToastErrorMessage("You selected "+book);
////                    }
//
//                }
//                //Result code for various error.
//            }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
//                showToastErrorMessage("Audio Error");
//            }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
//                showToastErrorMessage("Client Error");
//            }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
//                showToastErrorMessage("Network Error");
//            }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
//                showToastErrorMessage("No Match");
//            }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
//                showToastErrorMessage("Server Error");
//            }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//    /**
//     * Helper method to show the toast message
//     **/
//    void showToastErrorMessage(String message){
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = (event.getAction() & MotionEvent.ACTION_MASK);

		switch (action) {
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				mPtrCount++;
				if (mPtrCount == 1 && mPrimStartTouchEventY == -1 && mPrimStartTouchEventY == -1) {
					mPrimStartTouchEventX = event.getX(0);
					mPrimStartTouchEventY = event.getY(0);
                    Toast.makeText(this, String.format("POINTER ONE X = %.5f, Y = %.5f", mPrimStartTouchEventX, mPrimStartTouchEventY), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", String.format("POINTER ONE X = %.5f, Y = %.5f", mPrimStartTouchEventX, mPrimStartTouchEventY));
				}
				if (mPtrCount == 2) {
					// Starting distance between fingers
					mSecStartTouchEventX = event.getX(1);
					mSecStartTouchEventY = event.getY(1);
					mPrimSecStartTouchDistance = distance(event, 0, 1);
                    Toast.makeText(this, String.format("POINTER TWO X = %.5f, Y = %.5f", mSecStartTouchEventX, mSecStartTouchEventY), Toast.LENGTH_SHORT).show();
					Log.d("TAG", String.format("POINTER TWO X = %.5f, Y = %.5f", mSecStartTouchEventX, mSecStartTouchEventY));
				}

				break;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
				mPtrCount--;
				if (mPtrCount < 2) {
					mSecStartTouchEventX = -1;
					mSecStartTouchEventY = -1;
				}
				if (mPtrCount < 1) {
					mPrimStartTouchEventX = -1;
					mPrimStartTouchEventY = -1;
				}
				break;

			case MotionEvent.ACTION_MOVE:
				boolean isPrimMoving = isScrollGesture(event, 0, mPrimStartTouchEventX, mPrimStartTouchEventY);
				boolean isSecMoving = (mPtrCount > 1 && isScrollGesture(event, 1, mSecStartTouchEventX, mSecStartTouchEventY));

				// There is a chance that the gesture may be a scroll
				if (mPtrCount > 1 && isPinchGesture(event)) {
                    Toast.makeText(this, "PINCH OUCH", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "PINCH! OUCH!");

				} else if (isPrimMoving || isSecMoving) {
					// A 1 finger or 2 finger scroll.
					if (isPrimMoving && isSecMoving) {
						Log.d("TAG", "Two finger scroll");
					} else {
						Log.d("TAG", "One finger scroll");
					}
				}
				break;
		}

		return true;
	}

	private boolean isScrollGesture(MotionEvent event, int ptrIndex, float originalX, float originalY){
		float moveX = Math.abs(event.getX(ptrIndex) - originalX);
		float moveY = Math.abs(event.getY(ptrIndex) - originalY);

		if (moveX > mViewScaledTouchSlop || moveY > mViewScaledTouchSlop) {
			return true;
		}
		return false;
	}

	private boolean isPinchGesture(MotionEvent event) {
		if (event.getPointerCount() == 2) {
			final float distanceCurrent = distance(event, 0, 1);
			final float diffPrimX = mPrimStartTouchEventX - event.getX(0);
			final float diffPrimY = mPrimStartTouchEventY - event.getY(0);
			final float diffSecX = mSecStartTouchEventX - event.getX(1);
			final float diffSecY = mSecStartTouchEventY - event.getY(1);

			if (// if the distance between the two fingers has increased past
				// our threshold
					Math.abs(distanceCurrent - mPrimSecStartTouchDistance) > mViewScaledTouchSlop
							// and the fingers are moving in opposing directions
							&& (diffPrimY * diffSecY) <= 0
							&& (diffPrimX * diffSecX) <= 0) {
				// mPinchClamp = false; // don't clamp initially
				return true;
			}
		}

		return false;
	}

	private float distance(MotionEvent event, int first, int second) {
		if (event.getPointerCount() >= 2) {
			final float x = event.getX(first) - event.getX(second);
			final float y = event.getY(first) - event.getY(second);

			return (float) Math.sqrt(x * x + y * y);
		} else {
			return 0;
		}
	}
	class MyPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		public MyPinchListener() {
			super();
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			super.onScaleEnd(detector);
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Toast.makeText(BiblesOfflineActivity.this, "PINCHED", Toast.LENGTH_LONG).show();
			Log.d("TAG", "PINCH! OUCH!");
			return true;
		}
	}

}