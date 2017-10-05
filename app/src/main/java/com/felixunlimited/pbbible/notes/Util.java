package com.felixunlimited.pbbible.notes;

import android.content.Context;
import android.os.Environment;

import com.felixunlimited.pbbible.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Util {


    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_OPEN_NOTE_FROM_LIST = "open_note_from_list";
    public static final String ARG_SCRIPTURE = "scripture";
    public static final String ARG_TWO_PANE = "twoPane";
    public static final String ARG_NOTE_BEING_EDITED = "note_being_edited";
    public static final String ARG_NEW_NOTE_FROM_READING = "new_note_from_reading";
    public static File NOTES_DIR = new File(Environment.getExternalStorageDirectory(), Constants.NOTES_FOLDER);

    public static int getCount (File dir) {
        if (!dir.exists())
            dir.mkdirs();
        assert dir == null;
        return dir.listFiles().length;
    }

    public static Note getNoteFromFile(File noteFile) {
        if (noteFile.exists()) {
            try {
                FileInputStream fin = new FileInputStream(noteFile);
                InputStreamReader tmp = new InputStreamReader(fin);
                BufferedReader reader = new BufferedReader(tmp);
                String str = "";
                StringBuilder buf = new StringBuilder();
                while ((str = reader.readLine()) != null)
                    buf.append(str).append("\n");
                fin.close();
                return Note.extractNoteFromRaw(buf.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void saveFromVoice (Context context, String scripture) {
        String dateToday = getDate(System.currentTimeMillis());
        if (!NOTES_DIR.exists())
            NOTES_DIR.mkdirs();
        File mostRecentFile = getMostRecentFile(NOTES_DIR.getAbsolutePath());
        String latestNoteDate = "";
        if (mostRecentFile != null)
            latestNoteDate = getDate(getTimestampFromNoteFile(mostRecentFile));
        File noteFile;
        Note note;
        if (dateToday.equals(latestNoteDate)){
            noteFile = mostRecentFile;
            note = getNoteFromFile(mostRecentFile);
        }
        else {
            noteFile = new File(NOTES_DIR, String.valueOf(System.currentTimeMillis()) + ".nt");
            note = new Note(context);
            note.title = dateToday;
        }

        assert note != null;
        note.content += "\n"+scripture;
        saveNoteToFile(note);
    }

    public static void deleteNote(Note note) {
        File noteFile = new File(NOTES_DIR, "/"+note.created+".nt");
        if (noteFile.exists()) {
            noteFile.delete();
        }
    }

    public static void deleteNotes(Note[] notes) {
        for (Note note :
                notes) {
            deleteNote(note);
        }
    }

    public static long getTimestampFromNoteFile (File file) {
        return Long.parseLong(file.getName().replace(".nt", ""));
    }

    public static File getMostRecentFile (String dir) {
        File fl = new File(dir);
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }

    public static String getDate (long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        Date resultdate = new Date(timeStamp);
        return sdf.format(resultdate);
    }

    public static boolean fileExists(Context context,String filename) {
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    public static String firstLine(String content) {
        String[] lines = content.split("\\n");
        if (lines[0].isEmpty())
            return lines[1];
        return lines[0];
    }

    public static String formatDate(long timeStamp) {
        //String result = "";
        SimpleDateFormat sdf;
        Date resultDate = new Date(timeStamp);
        String today = getDate(System.currentTimeMillis());
        String fileDate = getDate(timeStamp);
        if (today == fileDate) {
            sdf = new SimpleDateFormat("hh:mm ttt");
        }
        else
            sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(resultDate);
    }

    public static void saveNoteToFile(Note note) {
        note.modified = String.valueOf(System.currentTimeMillis());
        if (!NOTES_DIR.exists())
            NOTES_DIR.mkdirs();
        //if (mNote[INDEX_CREATED])
        String fileContent = Note.getRaFromNotewNote(note);
        try {
            FileWriter fileWriter = new FileWriter(NOTES_DIR +"/"+ note.created+".nt", false);
            fileWriter.write(fileContent);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
