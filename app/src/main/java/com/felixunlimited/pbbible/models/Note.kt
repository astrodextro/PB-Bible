package com.felixunlimited.pbbible.models

/**
 * Note object.
 */
class Note {
    @JvmField
    var created = ""
    @JvmField
    var title = ""
    @JvmField
    var speaker = ""
    @JvmField
    var content = ""
    @JvmField
    var modified = ""
    @JvmField
    var rawNote = ""

    constructor() {
        created = System.currentTimeMillis().toString()
    }

    constructor(created: String, title: String, speaker: String, content: String, modified: String, rawNote: String) {
        this.created = created
        this.title = title
        this.speaker = speaker
        this.content = content
        this.modified = modified
        this.rawNote = rawNote
    }

    override fun toString(): String {
        return content
    }

    fun toStringArray(): Array<String?> {
        val stringArray = arrayOfNulls<String>(5)
        stringArray[0] = created
        stringArray[1] = title
        stringArray[2] = speaker
        stringArray[3] = content
        stringArray[4] = modified
        return stringArray
    }

    companion object {
        @JvmStatic
        fun extractNoteFromRaw(rawNote: String?): Note {
            val created: String
            val modified: String
            val title: String
            val speaker: String
            val content: String
            var note = Note("", "", "", "", "", "")
            if (rawNote != null) {
                created = rawNote.substring(rawNote.indexOf("<created>") + 9, rawNote.indexOf("</created>"))
                modified = rawNote.substring(rawNote.indexOf("<modified>") + 10, rawNote.indexOf("</modified>"))
                title = rawNote.substring(rawNote.indexOf("<title>") + 7, rawNote.indexOf("</title>"))
                speaker = rawNote.substring(rawNote.indexOf("<speaker>") + 9, rawNote.indexOf("</speaker>"))
                content = rawNote.substring(rawNote.indexOf("<content>") + 9, rawNote.indexOf("</content>"))
                note = Note(created, title, speaker, content, modified, rawNote)
            }
            return note
        }

        @JvmStatic
        fun getRaFromNotewNote(note: Note?): String {
            var rawNote = """
                   <created></created>
                   <title></title>
                   <speaker></speaker>
                   <content></content>
                   <modified></modified>

                   """.trimIndent()
            if (note != null) rawNote = "<created>" + note.created + "</created>" +
                    "<title>" + note.title + "</title>" +
                    "<speaker>" + note.speaker + "</speaker>" +
                    "<content>" + note.content + "</content>" +
                    "<modified>" + note.modified + "</modified>"
            return rawNote
        }
    }
}