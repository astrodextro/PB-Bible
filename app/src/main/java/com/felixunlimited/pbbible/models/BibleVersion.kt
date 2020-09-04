package com.felixunlimited.pbbible.models

class BibleVersion {
    var id: Long? = null
    var fileName: String? = null
    var lastModified: Long? = null
    var bibleName: String? = null
    var eolLength: Int? = null
    var about: String? = null
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = (prime * result
                + if (fileName == null) 0 else fileName.hashCode())
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val other1 = other as BibleVersion
        return if (fileName == null) {
            other1.fileName == null
        } else fileName == other1.fileName
    }
}