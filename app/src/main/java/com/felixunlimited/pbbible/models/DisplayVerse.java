package com.felixunlimited.pbbible.models;

public class DisplayVerse {
	private int chapterIndex;
	private int verseNumber;
	private String verse;
	private boolean bookmark;
	private int highlight;
	private int highlightColor;
	private boolean insertLineBreak;
	
	public DisplayVerse(int verseNumber, String verse, boolean bookmark, int highlighC, boolean insertLineBreak) {
		this.verseNumber = verseNumber;
		this.verse = verse;
		this.highlight = highlighC;
		this.bookmark = bookmark;
		this.insertLineBreak = insertLineBreak;
	}
	
	public DisplayVerse(int verseNumber, String verse, boolean bookmark, int highlight, boolean insertLineBreak, int chapterIndex) {
		this.verseNumber = verseNumber;
		this.verse = verse;
		this.highlight = highlight;
		this.bookmark = bookmark;
		this.insertLineBreak = insertLineBreak;
		this.chapterIndex = chapterIndex;
	}
	
	public int getVerseNumber() {
		return verseNumber;
	}
	public void setVerseNumber(int verseNumber) {
		this.verseNumber = verseNumber;
	}
	public String getVerse() {
		return verse;
	}
	public void setVerse(String verse) {
		this.verse = verse;
	}
	public boolean isBookmark() {
		return bookmark;
	}
	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}
	public int getHighlight() { return highlight;};
	public void setHighlight(int highlight) {this.highlight = highlight;}
	public boolean isInsertLineBreak() {
		return insertLineBreak;
	}

	public void setInsertLineBreak(boolean insertLineBreak) {
		this.insertLineBreak = insertLineBreak;
	}

	public int getChapterIndex() {
		return chapterIndex;
	}

	public void setChapterIndex(int chapterIndex) {
		this.chapterIndex = chapterIndex;
	}
	
	
	
	
}
