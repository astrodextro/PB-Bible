package com.felixunlimited.pbbible.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import com.felixunlimited.pbbible.R;

public class GoToBible extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_gridview);

        GridView gridView = (GridView) findViewById(R.id.browseGrid);
	}

//    public class BrowseBibleAdapter extends BaseAdapter {
//        private Context mContext;
//
//        public BrowseBibleAdapter(Context c) {
//            mContext = c;
//        }
//
//        public int getCount() {
//            return mThumbIds.length;
//        }
//
//        public Object getItem(int position) {
//            return null;
//        }
//
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        // create a new ImageView for each item referenced by the Adapter
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ImageView imageView;
//            if (convertView == null) {
//                // if it's not recycled, initialize some attributes
//                imageView = new ImageView(mContext);
//                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                imageView.setPadding(8, 8, 8, 8);
//            } else {
//                imageView = (ImageView) convertView;
//            }
//
//            imageView.setImageResource(mThumbIds[position]);
//            return imageView;
//        }
//    }
}
