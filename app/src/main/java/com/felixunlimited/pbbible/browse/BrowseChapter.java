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

public class BrowseChapter extends Fragment {
    public BrowseChapter() {
        // Required empty public constructor
    }

    GridView gridView;

    boolean isResumed = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
//            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rootView = getView();
            int bookNumer = BrowseBible.bookNo;
            GridView gridView = null;
            if (rootView != null) {
                gridView = (GridView) rootView.findViewById(R.id.browseChapter);
                gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createChaptersList(bookNumer), "chapter"));
            }
        }
        else {
            isResumed = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrowseBible.chapterNo = getContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(Constants.POSITION_CHAPTER, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isResumed = true;
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_browse_chapter, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.browseChapter);
        int bookNumer = BrowseBible.bookNo;
        gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createChaptersList(bookNumer), "chapter"));
        if (!getUserVisibleHint())
            isResumed = false;
        return rootView;
    }

}
