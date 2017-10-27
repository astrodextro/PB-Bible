package com.felixunlimited.pbbible.browse;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.felixunlimited.pbbible.Constants;
import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.Util;

public class BrowseVerse extends Fragment {
    public BrowseVerse() {
        // Required empty public constructor
    }

    GridView gridView;

    boolean isResumed = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed) {
            View rootView = getView();
            int bookNumer = BrowseBible.bookNo;
            int chapter = BrowseBible.chapterNo;
            GridView gridView = null;
            if (rootView != null) {
                gridView = (GridView) rootView.findViewById(R.id.browseVerse);
                gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createVersesList(bookNumer, chapter), "verse"));
            }
        }
        else {
            //isResumed = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrowseBible.verseNo = getContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(Constants.POSITION_VERSE, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isResumed = true;
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_browse_verse, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.browseVerse);
        int bookNumber = BrowseBible.bookNo;
        int chapter = BrowseBible.chapterNo;
        gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createVersesList(bookNumber, chapter), "verse"));
        return rootView;
    }

}
