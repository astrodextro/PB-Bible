package com.felixunlimited.pbbible.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.R;
import android.widget.TextView;

import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.ui.activities.BrowseBibleActivity;
import com.felixunlimited.pbbible.ui.adapters.BrowseBibleAdapter;
import com.felixunlimited.pbbible.utils.Util;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


public class BrowseChapterFragment extends Fragment {
    public BrowseChapterFragment() {
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
            int bookNumer = BrowseBibleActivity.bookNo;
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
        BrowseBibleActivity.chapterNo = getContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(Constants.POSITION_CHAPTER, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isResumed = true;
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_browse_chapter, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.browseChapter);
        int bookNumer = BrowseBibleActivity.bookNo;
        gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createChaptersList(bookNumer), "chapter"));
        if (!getUserVisibleHint())
            isResumed = false;
        return rootView;
    }
}