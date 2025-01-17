package com.felixunlimited.pbbible;

import com.felixunlimited.pbbible.models.Bookmark;
import com.felixunlimited.pbbible.models.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class BookmarkDateAscComparator implements Comparator<Bookmark> {
	SimpleDateFormat fmtDbDate = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
    
	@Override
	public int compare(Bookmark b1, Bookmark b2) {
		try {
			Date bookmarkDate1 = fmtDbDate.parse(b1.getBookmarkDate());
			Date bookmarkDate2 = fmtDbDate.parse(b2.getBookmarkDate());
			if (bookmarkDate1.after(bookmarkDate2))
				return 1;
			else 
				return -1;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
