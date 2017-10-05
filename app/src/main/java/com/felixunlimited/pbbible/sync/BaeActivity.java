package com.felixunlimited.pbbible.sync;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.felixunlimited.pbbible.R;

public class BaeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bae);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class BaeSentFrag extends Fragment {


    }
    public static class BaeReceivedFrag extends Fragment {

    }

    public class BaeAdapter extends CursorAdapter {

        public BaeAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }
    }
}
