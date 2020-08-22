package com.felixunlimited.pbbible.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.felixunlimited.pbbible.ui.activities.BiblesOfflineActivity;
import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.ui.activities.BrowseBibleActivity;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class BrowseBibleAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mContent;
    private String mFrom;

    public BrowseBibleAdapter(Context c, ArrayList<String> mContent, String mFrom) {
        mContext = c;
        this.mContent = mContent;
        this.mFrom = mFrom;
    }

    public int getCount() {
        return mContent.size();
    }

    public Object getItem(int position) {
        return mContent.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }


    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Button button;
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            button = new Button(mContext);
            if (mFrom.equals("book")) {
                button.setLayoutParams(new GridView.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT));
//                if ((position+1) == BrowseBibleActivity.bookNo)
//                    button.setBackgroundColor(Color.BLUE);
            }
            else {
                button.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                if (mFrom.equals("chapter") && (position+1) == BrowseBibleActivity.chapterNo)
//                    button.setBackgroundColor(Color.BLUE);
//                if (mFrom.equals("verse") && (position+1) == BrowseBibleActivity.verseNo)
//                    button.setBackgroundColor(Color.BLUE);
            }
            button.setMaxLines(1);
        } else {
            button = (Button) convertView;
        }

//        if (mContext.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).getInt(Constants.CHAPTER_INDEX, 1)==position+1)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                button.setBackground(Color.RED);
//            }

        button.setText(mContent.get(position).replaceAll(" ", ""));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                int goBook = sharedPreferences.getInt(Constants.POSITION_BOOK, 1);
                int goChapter = sharedPreferences.getInt(Constants.POSITION_CHAPTER, 1);
                int goVerse = position+1;
                ViewPager viewPager = BrowseBibleActivity.viewPager;
                switch (mFrom) {
                    case "book":
                        BrowseBibleActivity.bookNo = position+1;
                        BrowseBibleActivity.chapterNo = 1;
                        BrowseBibleActivity.verseNo = 1;
                        editor.putInt(Constants.POSITION_BOOK, position+1);
                        editor.putInt(Constants.POSITION_CHAPTER, 1);
                        editor.putInt(Constants.POSITION_VERSE, 1);
                        viewPager.setCurrentItem(1, true);
                        break;
                    case "chapter":
                        editor.putInt(Constants.POSITION_CHAPTER, position+1);
                        editor.putInt(Constants.POSITION_VERSE, 1);
                        BrowseBibleActivity.chapterNo = position+1;
                        BrowseBibleActivity.verseNo = 1;
                        viewPager.setCurrentItem(2, true);
                        break;
                    default:
                        BrowseBibleActivity.verseNo = position+1;
                        int currentChapterIdx = 0;
                        int currentVerseIdx = 0;
                        if (goBook > 0 || goChapter > 0) {
                            String bookChapter = goBook + ";" + goChapter;
                            for (int i = Constants.arrBookStart[goBook - 1]; i < Constants.arrVerseCount.length; i++) {
                                if (Constants.arrVerseCount[i].startsWith(bookChapter)) {
                                    currentChapterIdx = i;
                                    String[] arrBookVerse = Constants.arrVerseCount[i].split(";");
                                    if (goVerse > Integer.parseInt(arrBookVerse[2]))
                                        currentVerseIdx = 0;
                                    else
                                        currentVerseIdx = goVerse;
                                    break;
                                }
                            }
                        }

                        editor.putInt(Constants.CHAPTER_INDEX, currentChapterIdx);
                        editor.putInt(Constants.POSITION_VERSE, currentVerseIdx);
                        editor.apply();
                        Intent showBibleActivity = new Intent(mContext, BiblesOfflineActivity.class);
                        showBibleActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        showBibleActivity.putExtra(Constants.FROM_BOOKMARKS, true);
                        showBibleActivity.putExtra(Constants.BOOKMARK_VERSE_START, currentVerseIdx);
                        mContext.startActivity(showBibleActivity);

                        break;
                }
                editor.apply();
            }
        });
        return button;
    }

    // references to our images
}
