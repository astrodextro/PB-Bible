package com.felixunlimited.pbbible.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.ui.activities.BrowseBibleActivity;
import com.felixunlimited.pbbible.ui.adapters.BrowseBibleAdapter;
import com.felixunlimited.pbbible.utils.Util;

public class BrowseVerseFragment extends Fragment {
    public BrowseVerseFragment() {
        // Required empty public constructor
    }

    GridView gridView;

    boolean isResumed = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed) {
            View rootView = getView();
            int bookNumer = BrowseBibleActivity.bookNo;
            int chapter = BrowseBibleActivity.chapterNo;
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
        BrowseBibleActivity.verseNo = getContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(Constants.POSITION_VERSE, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isResumed = true;
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_browse_verse, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.browseVerse);
        int bookNumber = BrowseBibleActivity.bookNo;
        int chapter = BrowseBibleActivity.chapterNo;
        gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createVersesList(bookNumber, chapter), "verse"));
        return rootView;
    }

}
