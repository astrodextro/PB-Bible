package com.felixunlimited.pbbible.models;

import android.content.Context;

/**
 * Note object.
 */
public class Note {
    public String created = "";
    public String title = "";
    public String speaker = "";
    public String content = "";
    public String modified = "";
    public String rawNote = "";

    public Note (Context context) {
        this.created = String.valueOf(System.currentTimeMillis());
    }

    public Note (String created, String title, String speaker, String content, String modified, String rawNote) {
        this.created = created;
        this.title = title;
        this.speaker = speaker;
        this.content = content;
        this.modified = modified;
        this.rawNote = rawNote;
    }

    public static Note extractNoteFromRaw(String rawNote) {
        String created, modified, title, speaker, content;
        Note note = new Note("","","","","","");
        if (rawNote != null) {
            created = rawNote.substring(rawNote.indexOf("<created>")+9, rawNote.indexOf("</created>"));
            modified = rawNote.substring(rawNote.indexOf("<modified>")+10, rawNote.indexOf("</modified>"));
            title = rawNote.substring(rawNote.indexOf("<title>")+7, rawNote.indexOf("</title>"));
            speaker = rawNote.substring(rawNote.indexOf("<speaker>")+9, rawNote.indexOf("</speaker>"));
            content = rawNote.substring(rawNote.indexOf("<content>")+9, rawNote.indexOf("</content>"));
            note = new Note(created, title, speaker, content, modified, rawNote);
        }
        return note;
    }


    public static String getRaFromNotewNote (Note note)
    {
        String rawNote = "<created>"+"</created>\n"+
                "<title>"+"</title>\n"+
                "<speaker>"+"</speaker>\n"+
                "<content>"+"</content>\n"+
                "<modified>"+"</modified>\n";
        if (note != null)
            rawNote = "<created>"+note.created+"</created>"+
                "<title>"+note.title+"</title>"+
                "<speaker>"+note.speaker+"</speaker>"+
                "<content>"+note.content+"</content>"+
                "<modified>"+note.modified+"</modified>";
        return rawNote;
    }

    @Override
    public String toString() {
        return content;
    }

    public String[] toStringArray() {
        String[] stringArray = new String[5];
        stringArray[0] = created;
        stringArray[1] = title;
        stringArray[2] = speaker;
        stringArray[3] = content;
        stringArray[4] = modified;
        return stringArray;
    }
}
