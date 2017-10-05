package com.felixunlimited.pbbible.browse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.Util;

public class BrowseBook extends Fragment {

    public BrowseBook() {
        // Required empty public constructor
    }
    GridView gridView;
    boolean isResumed = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser & gridView != null) {
            gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createBooksList(), "book"));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isResumed = true;
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_browse_book, container, false);
        gridView = rootView.findViewById(R.id.browseBook);
        gridView.setAdapter(new BrowseBibleAdapter(getContext(), Util.createBooksList(), "book"));
        return rootView;
    }

}
