package com.felixunlimited.pbbible.models

class DisplayVerse {
    var chapterIndex = 0
    var verseNumber: Int
    var verse: String
    var isBookmark: Boolean
    private var highlight: Int
    var isInsertLineBreak: Boolean

    constructor(verseNumber: Int, verse: String, bookmark: Boolean, highlighC: Int, insertLineBreak: Boolean) {
        this.verseNumber = verseNumber
        this.verse = verse
        highlight = highlighC
        isBookmark = bookmark
        isInsertLineBreak = insertLineBreak
    }

    constructor(verseNumber: Int, verse: String, bookmark: Boolean, highlight: Int, insertLineBreak: Boolean, chapterIndex: Int) {
        this.verseNumber = verseNumber
        this.verse = verse
        this.highlight = highlight
        isBookmark = bookmark
        isInsertLineBreak = insertLineBreak
        this.chapterIndex = chapterIndex
    }
}