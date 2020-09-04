package com.felixunlimited.pbbible.models

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import android.util.Log
import com.felixunlimited.pbbible.BookmarkDateAscComparator
import com.felixunlimited.pbbible.BookmarkDateDescComparator
import com.felixunlimited.pbbible.utils.Util
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

class DatabaseHelper(context: Context?) {
    private var db: SQLiteDatabase? = null
    private val openHelper: OpenHelper
    private var isDbOpen = false
    fun close() {
        try {
            db!!.close()
        } finally {
            isDbOpen = false
        }
    }

    @Throws(SQLiteException::class)
    fun open() {
        if (!isDbOpen || db != null && !db!!.isOpen) {
            db = try {
                openHelper.writableDatabase
            } catch (ex: SQLiteException) {
                openHelper.readableDatabase
            }
            isDbOpen = true
        }
    }

    fun saveOrUpdateBibleVersion(r: BibleVersion) {
        if (r.id == -1L) {
            val insertSql = "INSERT INTO bible_version(file_name, last_modified, bible_name, eol_length, about) values (?,?,?,?,?)"
            db!!.execSQL(insertSql, arrayOf<Any?>(r.fileName, r.lastModified, r.bibleName, r.eolLength, r.about))
            val selectId = "SELECT last_insert_rowid()"
            val c = db!!.rawQuery(selectId, null)
            if (c.moveToNext()) {
                r.id = c.getLong(0)
            }
            c.close()
        } else {
            val insertSql = "UPDATE bible_version set last_modified=?, bible_name=?, eol_length=?, about=? where id=?"
            db!!.execSQL(insertSql, arrayOf<Any?>(r.lastModified, r.bibleName, r.eolLength, r.about, r.id))
        }
    }

    fun getBibleVersionByFileName(fileName: String): BibleVersion? {
        val cursor = db!!.rawQuery("SELECT id, file_name, last_modified, bible_name, eol_length FROM bible_version WHERE file_name=?", arrayOf(fileName))
        var result: BibleVersion? = null
        while (cursor.moveToNext()) {
            result = BibleVersion()
            result.id = cursor.getLong(0)
            result.fileName = cursor.getString(1)
            result.lastModified = cursor.getLong(2)
            result.bibleName = cursor.getString(3)
            result.eolLength = cursor.getInt(4)
        }
        cursor.close()
        return result
    }

    fun getBibleVersionByBibleName(bibleName: String?): BibleVersion? {
        val cursor = db!!.rawQuery("SELECT id, file_name, last_modified, bible_name, eol_length FROM bible_version WHERE bible_name=?", arrayOf(bibleName))
        var result: BibleVersion? = null
        while (cursor.moveToNext()) {
            result = BibleVersion()
            result.id = cursor.getLong(0)
            result.fileName = cursor.getString(1)
            result.lastModified = cursor.getLong(2)
            result.bibleName = cursor.getString(3)
            result.eolLength = cursor.getInt(4)
        }
        cursor.close()
        return result
    }

    fun getAboutByBibleName(bibleName: String): String? {
        val cursor = db!!.rawQuery("SELECT about FROM bible_version WHERE bible_name=?", arrayOf(bibleName))
        var result: String? = null
        while (cursor.moveToNext()) {
            result = cursor.getString(0)
        }
        cursor.close()
        return result
    }

    fun getScriptures(context: Context?, category: String?): String {
        val cursor: Cursor
        cursor = if (category != null) db!!.rawQuery("SELECT * FROM scriptures WHERE category=?", arrayOf(category)) else db!!.rawQuery("SELECT * FROM scriptures", null)
        val result = JSONArray()
        var jsonObject: JSONObject
        while (cursor.moveToNext()) {
            jsonObject = JSONObject()
            try {
                jsonObject.put("id", cursor.getString(0))
                jsonObject.put("scripture", cursor.getString(1))
                jsonObject.put("category", cursor.getString(2))
                jsonObject.put("timestamp", cursor.getString(3))
                jsonObject.put("user_email", Util.getEmail(context))
                result.put(cursor.position, jsonObject)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        cursor.close()
        return result.toString()
    }

    fun insertScriptures(jsonData: String?): Boolean {
        val insertSql = "INSERT INTO scriptures(scripture, category, timestamp) values (?, ?, ?)"
        val jsonArray: JSONArray
        try {
            jsonArray = JSONArray(jsonData)
            for (i in 0 until jsonArray.length()) {
                db!!.execSQL(insertSql, arrayOf<Any>(jsonArray.getJSONObject(i).getString("scripture"),
                        jsonArray.getJSONObject(i).getString("category"),
                        jsonArray.getJSONObject(i).getString("timestamp")))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            return false
        } catch (e: SQLiteConstraintException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    val allBibleVersion: List<BibleVersion>
        get() {
            val cursor = db!!.rawQuery("SELECT id, file_name, last_modified, bible_name, eol_length FROM bible_version", null)
            val result: MutableList<BibleVersion> = ArrayList()
            while (cursor.moveToNext()) {
                val row = BibleVersion()
                row.id = cursor.getLong(0)
                row.fileName = cursor.getString(1)
                row.lastModified = cursor.getLong(2)
                row.bibleName = cursor.getString(3)
                row.eolLength = cursor.getInt(4)
                result.add(row)
            }
            cursor.close()
            return result
        }
    val bibleNameList: List<String>
        get() {
            val cursor = db!!.rawQuery("SELECT bible_name FROM bible_version ORDER BY bible_name", null)
            val result: MutableList<String> = ArrayList()
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0))
            }
            cursor.close()
            return result
        }

    fun getBibleTranslationList(bibleNameList: MutableList<String?>?, fileNameList: MutableList<String?>?) {
        if (bibleNameList == null || fileNameList == null) return
        bibleNameList.clear()
        fileNameList.clear()
        val cursor = db!!.rawQuery("SELECT bible_name, file_name FROM bible_version ORDER BY bible_name", null)
        while (cursor.moveToNext()) {
            bibleNameList.add(cursor.getString(0))
            fileNameList.add(cursor.getString(1))
        }
        cursor.close()
    }

    val bookmarkCategoryList: List<String>
        get() {
            val cursor = db!!.rawQuery("SELECT category_name FROM category ORDER BY category_name", null)
            val result: MutableList<String> = ArrayList()
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0))
            }
            cursor.close()
            return result
        }

    private class OpenHelper(context: Context?) : SQLiteOpenHelper(context, "pbbible.db", null, 6) {
        private val TAG = OpenHelper::class.java.name
        override fun onCreate(db: SQLiteDatabase) {
            Log.d(TAG, "Create database")
            db.execSQL("CREATE TABLE bible_version(id INTEGER PRIMARY KEY, file_name TEXT, last_modified INTEGER, bible_name TEXT, about TEXT, eol_length INTEGER);")
            db.execSQL("CREATE UNIQUE INDEX idx_bible_version_1 ON bible_version(file_name);")
            db.execSQL("CREATE TABLE category(id INTEGER PRIMARY KEY, category_name TEXT);")
            db.execSQL("CREATE TABLE scriptures(_id INTEGER, scripture TEXT, category TEXT, timestamp TEXT DEFAULT 0, PRIMARY KEY (scripture, category));")
            db.execSQL("CREATE UNIQUE INDEX idx_category_1 ON category(category_name);")
            db.execSQL("CREATE TABLE bookmark(id INTEGER PRIMARY KEY, highlighted INTEGER, category_id INTEGER, book INTEGER, chapter INTEGER, verse_start INTEGER, verse_end INTEGER, content TEXT, bible TEXT, bookmark_date TEXT);")
            db.execSQL("CREATE INDEX idx_bookmark_1 ON bookmark(category_id);")
            db.execSQL("CREATE INDEX idx_bookmark_2 ON bookmark(book, chapter, verse_start);")
            db.execSQL("CREATE TABLE read_history(id INTEGER PRIMARY KEY, chapter_index INTEGER);")
            db.execSQL("INSERT INTO CATEGORY(category_name) values ('" + DEFAULT_CATEGORY + "');")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.d(TAG, "Upgrade database from $oldVersion to $newVersion")
            db.execSQL("DROP TABLE IF EXISTS bible_version")
            onCreate(db)
        }
    }

    fun deleteInvalidBible(fileNames: StringBuffer) {
        if (fileNames.length == 0) {
            db!!.execSQL("DELETE FROM bible_version")
        } else {
            fileNames.delete(0, 1)
            db!!.execSQL("DELETE from bible_version where file_name not in ($fileNames)")
        }
    }

    fun insertCategory(categoryName: String): Long? {
        val insertSql = "INSERT INTO category(category_name) values (?)"
        db!!.execSQL(insertSql, arrayOf<Any>(categoryName))
        val selectId = "SELECT last_insert_rowid()"
        val c = db!!.rawQuery(selectId, null)
        var result: Long? = null
        if (c.moveToNext()) {
            result = c.getLong(0)
        }
        c.close()
        return result
    }

    fun clearAllBookmarks() {
        db!!.execSQL("DROP TABLE IF EXISTS bookmark")
        db!!.execSQL("DROP TABLE IF EXISTS category")
        db!!.execSQL("CREATE TABLE category(id INTEGER PRIMARY KEY, category_name TEXT);")
        db!!.execSQL("CREATE TABLE bookmark(id INTEGER PRIMARY KEY, highlighted INTEGER, category_id INTEGER, book INTEGER, chapter INTEGER, verse_start INTEGER, verse_end INTEGER, content TEXT, bible TEXT, bookmark_date TEXT);")
    }

    fun createBookmarkIndexes() {
        db!!.execSQL("CREATE UNIQUE INDEX idx_category_1 ON category(category_name);")
        db!!.execSQL("CREATE INDEX idx_bookmark_1 ON bookmark(category_id);")
        db!!.execSQL("CREATE INDEX idx_bookmark_2 ON bookmark(book, chapter, verse_start);")
    }

    fun insertBookmark(bm: Bookmark) {
        val insertSql = "INSERT INTO bookmark(category_id, highlighted, book, chapter, verse_start, verse_end, content, bible, bookmark_date)" +
                " values (?,?,?,?,?,?,?,?,?);"
        db!!.execSQL(insertSql, arrayOf<Any?>(bm.categoryId, bm.highlighted, bm.book, bm.chapter, bm.verseStart, bm.verseEnd,
                bm.content, bm.bible, bm.bookmarkDate))
    }

    fun getBookmarkList(result: MutableList<Bookmark?>, categoryName: String, sortBy: String, bible: String?) {
        var bible = bible
        val sb = StringBuffer()
        sb.append("SELECT b.id, b.category_id, b.highlighted, b.book, b.chapter, b.verse_start, b.verse_end, b.content, b.bible, b.bookmark_date")
                .append(" FROM bookmark b")
                .append(" INNER JOIN category c on c.id=b.category_id")
                .append(" WHERE c.category_name=?")
                .append(" ORDER BY book, chapter, verse_start")
        var bibleName = ""
        var prevOffset = 0
        var prevChapterIdx = -1
        var `is`: DataInputStream? = null
        var br: BufferedReader? = null
        var eolLength = 0
        if (bible != Constants.SHOW_BIBLE_AS_BOOKMARKED) {
            val bv = getBibleVersionByBibleName(bible)
            eolLength = bv!!.eolLength!!
            val sdcard = Environment.getExternalStorageDirectory()
            val file = File(sdcard, Constants.BIBLE_FOLDER + "/" + bv.fileName)
            val indexFileName = file.absolutePath.replace(".ont".toRegex(), ".idx")
            val fIndex = File(indexFileName)
            bibleName = file.name.replace(".ont".toRegex(), "").toLowerCase()
            try {
                `is` = DataInputStream(FileInputStream(fIndex))
                br = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"), 8192)
            } catch (e: Exception) {
                e.printStackTrace()
                bible = Constants.SHOW_BIBLE_AS_BOOKMARKED
            }
        }
        val sbVerse = StringBuffer()
        var skipLength = 0
        var startOffset = 0
        var prevVerse = 1
        val cursor = db!!.rawQuery(sb.toString(), arrayOf(categoryName))
        result.clear()
        while (cursor.moveToNext()) {
            val bm = Bookmark()
            bm.id = cursor.getLong(0)
            bm.highlighted = cursor.getInt(1)
            bm.categoryId = cursor.getLong(2)
            bm.book = cursor.getInt(3)
            bm.chapter = cursor.getInt(4)
            bm.verseStart = cursor.getInt(5)
            bm.verseEnd = cursor.getInt(6)
            if (bible != Constants.SHOW_BIBLE_AS_BOOKMARKED) {
                bm.bible = bibleName
                val chapterIdx = Constants.arrBookStart[bm.book!! - 1] + bm.chapter!! - 1
                val chapterIdxDiff = chapterIdx - prevChapterIdx
                try {
                    if (chapterIdxDiff > 0) {
                        prevChapterIdx = chapterIdx
                        `is`!!.skip((chapterIdxDiff - 1) * 4.toLong())
                        startOffset = `is`.readInt()
                        val offsetDiff = startOffset - prevOffset - skipLength
                        br!!.skip(offsetDiff.toLong())
                        prevOffset = startOffset
                        prevVerse = 1
                        skipLength = 0
                    }
                    while (prevVerse < bm.verseStart!!) {
                        prevVerse++
                        val line = br!!.readLine()
                        skipLength += line.length + eolLength
                    }
                    sbVerse.delete(0, sbVerse.length)
                    while (prevVerse <= bm.verseEnd!!) {
                        prevVerse++
                        val line = br!!.readLine()
                        sbVerse.append(line).append(" ")
                        skipLength += line.length + eolLength
                    }
                    if (sbVerse.isNotEmpty()) {
                        sbVerse.delete(sbVerse.length - 1, sbVerse.length)
                    }
                    bm.content = Util.parseVerse(sbVerse.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "Error reading bookmark bible file")
                    e.printStackTrace()
                    bible = Constants.SHOW_BIBLE_AS_BOOKMARKED
                }
            } else {
                bm.content = cursor.getString(7)
                bm.bible = cursor.getString(8)
            }
            bm.bookmarkDate = cursor.getString(9)
            result.add(bm)
        }
        cursor.close()
        if (`is` != null) {
            try {
                `is`.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        if (br != null) {
            try {
                br.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        if (sortBy == Constants.SORT_DATE_ASC) {
            Collections.sort(result, BookmarkDateAscComparator())
        } else if (sortBy == Constants.SORT_DATE_DESC) {
            Collections.sort(result, BookmarkDateDescComparator())
        }
    }

    fun getBookmarkVerseStartByChapterIndex(chapterIndex: Int): List<Int> {
        val result: MutableList<Int> = ArrayList()
        val arrBookChapter = Constants.arrVerseCount[chapterIndex]!!.split(";".toRegex()).toTypedArray()
        val cursor = db!!.rawQuery("SELECT b.verse_start FROM bookmark b" +
                " WHERE b.book=? and b.chapter=?" +
                " ORDER BY b.verse_start", arrayOf(arrBookChapter[0], arrBookChapter[1]))
        result.clear()
        while (cursor.moveToNext()) {
            result.add(cursor.getInt(0))
        }
        cursor.close()
        return result
    }

    fun getHighlightVerseStartByChapterIndex(chapterIndex: Int): List<Int> {
        val result: MutableList<Int> = ArrayList()
        val arrBookChapter = Constants.arrVerseCount[chapterIndex]!!.split(";".toRegex()).toTypedArray()
        val cursor = db!!.rawQuery("SELECT b.highlighted, b.verse_start FROM bookmark b" +
                " WHERE b.book=? and b.chapter=?" +
                " ORDER BY b.verse_start", arrayOf(arrBookChapter[0], arrBookChapter[1]))
        result.clear()
        for (i in 0 until arrBookChapter[2].toInt()) {
            cursor.moveToFirst()
            while (cursor.moveToNext()) {
                if (cursor.getInt(1) == i + 1) {
                    result.add(cursor.getInt(0))
                    break
                } else result.add(0)
            }
        }
        cursor.close()
        return result
    }

    fun getCategoryIdByCategoryName(name: String): Long? {
        val cursor = db!!.rawQuery("SELECT id FROM category WHERE category_name=?", arrayOf(name))
        var result: Long? = null
        while (cursor.moveToNext()) {
            result = cursor.getLong(0)
        }
        cursor.close()
        return result
    }

    val categoryNames: Array<String?>
        get() {
            val cursor = db!!.rawQuery("SELECT category_name FROM category ORDER BY category_name", null)
            val nameList: MutableList<String> = ArrayList()
            while (cursor.moveToNext()) {
                nameList.add(cursor.getString(0))
            }
            cursor.close()
            return nameList.toTypedArray()
        }

    fun removeBookmark(book: Int, chapter: Int, verseNumber: Int) {
        db!!.execSQL("DELETE FROM bookmark WHERE book=? AND chapter=? AND verse_start=?", arrayOf(book.toString(), chapter.toString(), verseNumber.toString()))
    }

    fun removeCategory(categoryName: String) {
        db!!.execSQL("DELETE FROM category WHERE category_name=?", arrayOf(categoryName))
    }

    fun updateCategory(categoryName: String, id: Long) {
        db!!.execSQL("UPDATE CATEGORY SET category_name=? where id=?", arrayOf(categoryName, id.toString()))
    }

    fun insertReplaceBookmark(bm: Bookmark) {
        val deleteSql = "DELETE FROM bookmark WHERE book=? AND chapter=? AND verse_start=?"
        db!!.execSQL(deleteSql, arrayOf<Any?>(bm.book, bm.chapter, bm.verseStart))
        insertBookmark(bm)
    }

    fun getBookmark(book: Int, chapter: Int, verseStart: Int): Bookmark? {
        val cursor = db!!.rawQuery("SELECT b.id, b.highlighted, b.category_id, b.book, b.chapter, b.verse_start, b.verse_end, b.content, b.bible, b.bookmark_date, " +
                " c.category_name" +
                " FROM bookmark b" +
                " INNER JOIN category c on c.id=b.category_id" +
                " WHERE b.book=? and b.chapter=? and b.verse_start=?", arrayOf(book.toString(), chapter.toString(), verseStart.toString()))
        var result: Bookmark? = null
        while (cursor.moveToNext()) {
            result = Bookmark()
            result.id = cursor.getLong(0)
            result.highlighted = cursor.getInt(1)
            result.categoryId = cursor.getLong(2)
            result.book = cursor.getInt(3)
            result.chapter = cursor.getInt(4)
            result.verseStart = cursor.getInt(5)
            result.verseEnd = cursor.getInt(6)
            result.content = cursor.getString(7)
            result.bible = cursor.getString(8)
            result.bookmarkDate = cursor.getString(9)
            result.categoryName = cursor.getString(10)
        }
        cursor.close()
        return result
    }

    fun removeAllCategoryAndBookmark() {
        clearAllBookmarks()
        createBookmarkIndexes()
        db!!.execSQL("INSERT INTO CATEGORY(category_name) values ('" + DEFAULT_CATEGORY + "');")
    }

    val allBookmarksForImport: List<Bookmark>
        get() {
            val result: MutableList<Bookmark> = ArrayList()
            val cursor = db!!.rawQuery("SELECT b.book, b.chapter, b.verse_start FROM bookmark b", null)
            while (cursor.moveToNext()) {
                val bm = Bookmark()
                bm.book = cursor.getInt(0)
                bm.chapter = cursor.getInt(1)
                bm.verseStart = cursor.getInt(2)
                result.add(bm)
            }
            cursor.close()
            return result
        }
    val allBookmarksForExport: List<Bookmark>
        get() {
            val cursor = db!!.rawQuery("SELECT b.id, b.highlighted, b.category_id, b.book, b.chapter, b.verse_start, b.verse_end, b.content, b.bible, b.bookmark_date, " +
                    " c.category_name" +
                    " FROM bookmark b" +
                    " INNER JOIN category c on c.id=b.category_id" +
                    " ORDER BY b.category_id, b.book, b.chapter, b.verse_start", null)
            val result: MutableList<Bookmark> = ArrayList()
            while (cursor.moveToNext()) {
                val bm = Bookmark()
                bm.id = cursor.getLong(0)
                bm.highlighted = cursor.getInt(1)
                bm.categoryId = cursor.getLong(2)
                bm.book = cursor.getInt(3)
                bm.chapter = cursor.getInt(4)
                bm.verseStart = cursor.getInt(5)
                bm.verseEnd = cursor.getInt(6)
                bm.content = cursor.getString(7)
                bm.bible = cursor.getString(8)
                bm.bookmarkDate = cursor.getString(9)
                bm.categoryName = cursor.getString(10)
                result.add(bm)
            }
            cursor.close()
            return result
        }
    val categoryMap: Map<String, Long>
        get() {
            val result: MutableMap<String, Long> = HashMap()
            val cursor = db!!.rawQuery("SELECT id, category_name FROM category", null)
            while (cursor.moveToNext()) {
                val categoryId = cursor.getLong(0)
                val categoryName = cursor.getString(1)
                result[categoryName] = categoryId
            }
            cursor.close()
            return result
        }

    fun getHistory(result: MutableList<Int?>) {
        result.clear()
        val cursor = db!!.rawQuery("SELECT chapter_index FROM read_history ORDER BY id", null)
        while (cursor.moveToNext()) {
            result.add(cursor.getInt(0))
        }
        cursor.close()
        if (result.size == 0) {
            result.add(0)
        }
    }

    fun saveHistory(historyList: List<Int>) {
        db!!.execSQL("DELETE FROM read_history")
        for (i in historyList.indices) {
            db!!.execSQL("INSERT INTO read_history(chapter_index) values (" + historyList[i] + ")")
        }
    }

    fun removeAllBookmarks(categoryName: String) {
        val categoryId = getCategoryIdByCategoryName(categoryName)
        db!!.execSQL("DELETE FROM bookmark WHERE category_id=$categoryId")
    }

    fun moveBookmark(newCategoryName: String, id: Long) {
        val categoryId = getCategoryIdByCategoryName(newCategoryName)
        db!!.execSQL("UPDATE bookmark set category_id=? where id=?", arrayOf(categoryId.toString(), id.toString()))
    }

    val randomBookmark: Bookmark?
        get() {
            val cursor = db!!.rawQuery("SELECT b.id, b.highlighted, b.category_id, b.book, b.chapter, b.verse_start, b.verse_end, b.content, b.bible, b.bookmark_date " +
                    " FROM bookmark b" +
                    " ORDER BY RANDOM() limit 1", null)
            var result: Bookmark? = null
            while (cursor.moveToNext()) {
                result = Bookmark()
                result.id = cursor.getLong(0)
                result.highlighted = cursor.getInt(1)
                result.categoryId = cursor.getLong(2)
                result.book = cursor.getInt(3)
                result.chapter = cursor.getInt(4)
                result.verseStart = cursor.getInt(5)
                result.verseEnd = cursor.getInt(6)
                result.content = cursor.getString(7)
                result.bible = cursor.getString(8)
                result.bookmarkDate = cursor.getString(9)
            }
            cursor.close()
            return result
        }
    val baeScriptures: Cursor
        get() = db!!.rawQuery("SELECT * FROM scriptures ORDER BY timestamp DESC", null)

    companion object {
        private const val TAG = "DatabaseHelper"
        private const val DEFAULT_CATEGORY = "Favorite"
    }

    init {
        openHelper = OpenHelper(context)
    }
}