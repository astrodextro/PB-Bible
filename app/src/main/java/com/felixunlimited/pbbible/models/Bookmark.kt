package com.felixunlimited.pbbible.models

class Bookmark {
    var id: Long? = null
    var categoryId: Long? = null
    var book: Int? = null
    var chapter: Int? = null
    var verseStart: Int? = null
    var verseEnd: Int? = null
    var content: String? = null
    var bible: String? = null
    var bookmarkDate: String? = null
    var highlighted = 0

    //not persist
    var categoryName: String? = null
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (book == null) 0 else book.hashCode()
        result = prime * result + if (chapter == null) 0 else chapter.hashCode()
        result = (prime * result
                + if (verseStart == null) 0 else verseStart.hashCode())
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val other1 = other as Bookmark
        if (book == null) {
            if (other1.book != null) return false
        } else if (book != other1.book) return false
        if (chapter == null) {
            if (other1.chapter != null) return false
        } else if (chapter != other1.chapter) return false
        if (verseStart == null) {
            if (other1.verseStart != null) return false
        } else if (verseStart != other1.verseStart) return false
        return true
    }
}