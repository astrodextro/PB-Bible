package com.felixunlimited.pbbible.browse;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.felixunlimited.pbbible.R;

import java.util.ArrayList;
import java.util.List;

public class BrowseBible extends AppCompatActivity /*implements View.OnClickListener, TextView.OnEditorActionListener*/ /*implements SearchView.OnQueryTextListener*/ {

    public static ViewPager viewPager;
    public static int bookNo = 1, chapterNo = 1, verseNo = 1;
    private EditText edtBook;
    private Button btnBrowse;
    private ImageButton btnTTalk;

    private String currentBookLanguage;
    private int currentChapterIdx;
    EditText searchEditText;
    SearchView searchView;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.browse_menu, menu);
//
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
//        searchView.setOnQueryTextListener(this);
//        searchView.setQueryHint("Type scripture eg Genesis 1");
////        searchView.setIconifiedByDefault(false);
////        searchView.setBackgroundColor(getResources().getColor(R.color.white));
////        searchView.setForeground(getResources().getColor(R.color.black));
//        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//        if (itemId == R.id.btnSpeak) {
//            startActivity(new Intent(this, Voice.class));
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Util.setTheme(this, R.style.AppBaseTheme_AppCompat_Dialog_Light);
        setContentView(R.layout.activity_browse_bible);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
//        edtBook = (EditText) findViewById(R.id.edtBook);
//        edtBook.setOnEditorActionListener(this);
//        btnTTalk = (ImageButton) findViewById(R.id.btnSpeak);
//        btnTTalk.setOnClickListener(this);

//Adding toolbar in our activity
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BrowseBook(), "Books");
        adapter.addFragment(new BrowseChapter(), "Chapters");
        adapter.addFragment(new BrowseVerse(), "Verses");
        viewPager.setAdapter(adapter);
    }

//    @Override
//    public void onClick(View v) {
//        if (v.getId() == R.id.btnBrowse) {
//            finish();
//            startActivity(new Intent(this, BrowseBible.class));
//        }
//        else if (v.getId() == R.id.btnSpeak) {
//            finish();
//            startActivity(new Intent(this, Voice.class));
//        }
//    }
//
//    @Override
//    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER )) ||
//                actionId == EditorInfo.IME_ACTION_DONE){
//            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            in.hideSoftInputFromWindow(edtBook.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//
//            String searchBook = edtBook.getText().toString().trim();
//            if (searchBook.length() == 0) {
//                finish();
//                return true;
//            }
//
//            int goChapter = 0;
//            int goBook = 0;
//            char lastChar = searchBook.charAt(searchBook.length() - 1);
//            String strChapter = "";
//            if (lastChar >= '0' && lastChar <= '9') {
//                do {
//                    strChapter = lastChar + strChapter;
//                    if (searchBook.length() > 1) {
//                        searchBook = searchBook.substring(0, searchBook.length() - 1);
//                        lastChar = searchBook.charAt(searchBook.length() - 1);
//                    } else {
//                        searchBook = "";
//                        break;
//                    }
//                } while (lastChar >= '0' && lastChar <= '9');
//                goChapter = Integer.parseInt(strChapter);
//            }
//
//            if (searchBook.length() > 0) {
//                searchBook = searchBook.replaceAll(" ", "");
//                searchBook = searchBook.toLowerCase();
//
//                String[] firstSearch = null;
//                String[] secondSearch = null;
//
////				if (currentBookLanguage.equals(Constants.LANG_BAHASA)) {
////					firstSearch = Constants.arrBookNameIndo;
////					secondSearch = Constants.arrBookName;
////				} else {
////					firstSearch = Constants.arrBookName;
////					secondSearch = Constants.arrBookNameIndo;
////				}
//                firstSearch = Constants.arrActiveBookName;
//                secondSearch = Constants.arrActiveBookAbbr;
//
//                for (int i = 0; i < firstSearch.length; i++) {
//                    String book = firstSearch[i].toLowerCase();
//                    book = book.replaceAll(" ", "");
//                    if (book.startsWith(searchBook)) {
//                        goBook = i + 1;
//                        break;
//                    }
//                }
//
//                if (goBook == 0) {
//                    for (int i = 0; i < secondSearch.length; i++) {
//                        String book = secondSearch[i].toLowerCase();
//                        book = book.replaceAll(" ", "");
//                        if (book.startsWith(searchBook)) {
//                            goBook = i + 1;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (goBook > 0 || goChapter > 0) {
//                if (goBook == 0) {
//                    String[] arrBookChapter = Constants.arrVerseCount[currentChapterIdx].split(";");
//                    goBook = Integer.parseInt(arrBookChapter[0]);
//                }
//                if (goChapter == 0) {
//                    goChapter = 1;
//                }
//                String bookChapter = goBook + ";" + goChapter;
//                for (int i = Constants.arrBookStart[goBook - 1]; i < Constants.arrVerseCount.length; i++) {
//                    if (Constants.arrVerseCount[i].startsWith(bookChapter)) {
//                        currentChapterIdx = i;
//                        break;
//                    }
//                }
//            }
//
//            SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
//            editor.putInt(Constants.CHAPTER_INDEX, currentChapterIdx);
//            editor.commit();
//            finish();
//            return true;
//        }
//        return false;
//    }

//    @Override
//    public boolean onQueryTextSubmit(String searchBook) {
//        searchView.clearFocus();
//        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        in.hideSoftInputFromWindow(searchView.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//
//        if (searchBook.length() == 0) {
//            finish();
//            return true;
//        }
//
//        int goChapter = 0;
//        int goBook = 0;
//        char lastChar = searchBook.charAt(searchBook.length() - 1);
//        String strChapter = "";
//        if (lastChar >= '0' && lastChar <= '9') {
//            do {
//                strChapter = lastChar + strChapter;
//                if (searchBook.length() > 1) {
//                    searchBook = searchBook.substring(0, searchBook.length() - 1);
//                    lastChar = searchBook.charAt(searchBook.length() - 1);
//                } else {
//                    searchBook = "";
//                    break;
//                }
//            } while (lastChar >= '0' && lastChar <= '9');
//            goChapter = Integer.parseInt(strChapter);
//        }
//
//        if (searchBook.length() > 0) {
//            searchBook = searchBook.replaceAll(" ", "");
//            searchBook = searchBook.toLowerCase();
//
//            String[] firstSearch = null;
//            String[] secondSearch = null;
//
////				if (currentBookLanguage.equals(Constants.LANG_BAHASA)) {
////					firstSearch = Constants.arrBookNameIndo;
////					secondSearch = Constants.arrBookName;
////				} else {
////					firstSearch = Constants.arrBookName;
////					secondSearch = Constants.arrBookNameIndo;
////				}
//            firstSearch = Constants.arrActiveBookName;
//            secondSearch = Constants.arrActiveBookAbbr;
//
//            for (int i = 0; i < firstSearch.length; i++) {
//                String book = firstSearch[i].toLowerCase();
//                book = book.replaceAll(" ", "");
//                if (book.startsWith(searchBook)) {
//                    goBook = i + 1;
//                    break;
//                }
//            }
//
//            if (goBook == 0) {
//                for (int i = 0; i < secondSearch.length; i++) {
//                    String book = secondSearch[i].toLowerCase();
//                    book = book.replaceAll(" ", "");
//                    if (book.startsWith(searchBook)) {
//                        goBook = i + 1;
//                        break;
//                    }
//                }
//            }
//        }
//
//        if (goBook > 0 || goChapter > 0) {
//            if (goBook == 0) {
//                String[] arrBookChapter = Constants.arrVerseCount[currentChapterIdx].split(";");
//                goBook = Integer.parseInt(arrBookChapter[0]);
//            }
//            if (goChapter == 0) {
//                goChapter = 1;
//            }
//            String bookChapter = goBook + ";" + goChapter;
//            for (int i = Constants.arrBookStart[goBook - 1]; i < Constants.arrVerseCount.length; i++) {
//                if (Constants.arrVerseCount[i].startsWith(bookChapter)) {
//                    currentChapterIdx = i;
//                    break;
//                }
//            }
//        }
//
//        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
//        editor.putInt(Constants.CHAPTER_INDEX, currentChapterIdx);
//        editor.commit();
//        finish();
//        return true;
////        return false;
//    }
//
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        return false;
//    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
