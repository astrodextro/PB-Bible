package com.felixunlimited.pbbible.speech;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.felixunlimited.pbbible.Constants;
import com.felixunlimited.pbbible.DisplayVerse;
import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.Util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.felixunlimited.pbbible.Constants.BIBLE_FOLDER;
import static com.felixunlimited.pbbible.Constants.BOOK_LANGUAGE;
import static com.felixunlimited.pbbible.Constants.LANG_ENGLISH;
import static com.felixunlimited.pbbible.Constants.POSITION_BIBLE_NAME;
import static com.felixunlimited.pbbible.Constants.CHAPTER_INDEX;
import static com.felixunlimited.pbbible.Constants.PREFERENCE_NAME;
import static com.felixunlimited.pbbible.Constants.arrVerseCount;

public class Listen extends Activity implements AdapterView.OnItemSelectedListener,
        TextToSpeech.OnInitListener,
        View.OnClickListener{
    Spinner spnBook;
    Spinner spnChapter;
    Spinner spnVerseStart;
    Spinner spnVerseEnd;
    Spinner spnLocale;
    Spinner spnVoice;
    Button btnPlayStop;
    TextView txtStatus;
    Switch switcAutoPlaey;
    Switch switchRepeat;
    ArrayList<String> listBooks;
    ArrayList<String> listChapters;
    ArrayList<String> listVerses;
    ArrayList<Locale> listLocales;
    ArrayList<Voice> listVoices;

    private String currentBibleFilename;
    private String currentBookLanguage;

    DisplayVerse verse;
    private List<DisplayVerse> verseList = new ArrayList<DisplayVerse>();

    public enum State {
        Null,
        Playing,
        Paused,
        Ready,
        Stopped
    };
    State ttsState;
    TextToSpeech tts;

    Handler handler;

    final String TAG = "Listen Activity";
    public int currentBook;
    public int currentChapter;
    public int currentVerseStart;
    public int currentVerseEnd;
    public int currentVerseMax;
    public int currentVerse;
    public int initialBook;
    public int currentChapterIdx;
    public Locale currentLocale;
    public Voice currebtVoice;
    final int MY_DATA_CHECK_CODE = 121;
    final String FINISHED_UTTERANCE_ID = "FINISHED";
    boolean isFinished = false;
    boolean isFromCode = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen);
        handler = new Handler(Looper.getMainLooper());
        readPreference();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        checkTTSAvailability();
        populateVerseList(currentBibleFilename, currentChapterIdx);
        instantiateSpinners();
        initializeLists();
        createAdapters();
        instantiateButtons();
    }


    private void readPreference() {
        //SpeechRecognizer
        SharedPreferences preference = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        currentChapterIdx = preference.getInt(CHAPTER_INDEX, 0);
        if (currentChapterIdx < 0 || currentChapterIdx >= arrVerseCount.length) {
            currentChapterIdx = 0;
        }
        currentBibleFilename = preference.getString(POSITION_BIBLE_NAME, "");

        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentBookLanguage = defaultPrefs.getString(BOOK_LANGUAGE, LANG_ENGLISH);
    }
    public boolean isDoneSpeaking (ReentrantLock reentrantLock) {
        boolean isDone = false;
        if (!tts.isSpeaking()) {
            try {
                done.signal();
                isDone = true;
            }
            finally {
                reentrantLock.unlock();
            }
        }
        return isDone;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
//                tts = new TextToSpeech(this, this, "com.google.android.tts");
                tts = new TextToSpeech(this, this);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceID) {
                        if (utteranceID.equals(FINISHED_UTTERANCE_ID)) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ttsState = State.Playing;
                                    txtStatus.setText("Playing");
                                    btnPlayStop.setText("STOP");
                                }
                            });
                        }
                    }

                    @Override
                    public void onDone(String utteranceID) {
                        if (utteranceID.equals(FINISHED_UTTERANCE_ID)) {
                            if (ttsState == State.Stopped)
                                return;
                            if (currentVerse != currentVerseEnd) {
                                currentVerse++;
                                speak();
                                return;
                            }

                            else if (switcAutoPlaey.isChecked()) {
                                if (currentVerseEnd != currentVerseMax) {
                                    currentVerse++;
                                    currentVerseEnd = currentVerseMax;
                                }
                                else {
                                    currentChapterIdx++;
                                    if (currentChapterIdx > Constants.arrVerseCount.length - 1)
                                        currentChapterIdx = 0;
                                    populateVerseList(currentBibleFilename, currentChapterIdx);
                                    initializeLists();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            createAdapters();
                                            isFromCode = true;
                                        }
                                    });
                                    currentVerse = 1;
                                }
                                speak();
                            }
                            else if (switchRepeat.isChecked()) {
                                currentVerse = currentVerseStart;
                                speak();
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ttsState = State.Stopped;
                                    txtStatus.setText("Done");
                                    btnPlayStop.setText("PLAY");
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String s) {
                        Toast.makeText(Listen.this, "error "+ s, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    public void checkTTSAvailability () {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    public void instantiateButtons () {
        txtStatus = (TextView) findViewById(R.id.statusText);
        switcAutoPlaey = (Switch) findViewById(R.id.autoplay);
        switcAutoPlaey.setChecked(false);
        switcAutoPlaey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    switchRepeat.setChecked(false);
                    ((ViewGroup)spnVerseEnd.getParent()).setVisibility(View.GONE);
                    spnVerseEnd.setSelection(currentVerseMax-1);
                }
                else {
                    if (((ViewGroup)spnVerseEnd.getParent()).getVisibility() == View.GONE)
                        ((ViewGroup)spnVerseEnd.getParent()).setVisibility(View.VISIBLE);
                }
            }
        });
        switchRepeat = (Switch) findViewById(R.id.repeat);
        switchRepeat.setChecked(false);
        switchRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    switcAutoPlaey.setChecked(false);
                    if (((ViewGroup)spnVerseEnd.getParent()).getVisibility() == View.GONE)
                        ((ViewGroup)spnVerseEnd.getParent()).setVisibility(View.VISIBLE);
                }
            }
        });
//        switcAutoPlaey.setVisibility(View.GONE);
//        switchRepeat.setVisibility(View.GONE);
        btnPlayStop = (Button) findViewById(R.id.btnPlayStop);
        btnPlayStop.setOnClickListener(this);
        if (ttsState == State.Ready || ttsState == State.Paused)
            btnPlayStop.setText("Read scripture");
        else if (ttsState == State.Playing)
            btnPlayStop.setText("Stop reading");
    }

    public void instantiateSpinners () {
        spnBook = (Spinner) findViewById(R.id.spn_book);
        spnChapter = (Spinner) findViewById(R.id.spn_chapter);
        spnVerseStart = (Spinner) findViewById(R.id.spn_verseStart);
        spnVerseEnd = (Spinner) findViewById(R.id.spn_VerseEnd);
//        spnLocale = (Spinner) findViewById(R.id.spn_Locale);
//        spnVoice = (Spinner) findViewById(R.id.spn_Voice);

        spnBook.setOnItemSelectedListener(this);
        spnChapter.setOnItemSelectedListener(this);
        spnVerseStart.setOnItemSelectedListener(this);
        spnVerseEnd.setOnItemSelectedListener(this);
//        spnLocale.setOnItemSelectedListener(this);
//        spnVoice.setOnItemSelectedListener(this);
    }

    public void initializeLists() {
        String[] arrBookChapterVerse = Constants.arrVerseCount[currentChapterIdx].split(";");
        currentBook = Integer.parseInt(arrBookChapterVerse[0]);
        currentChapter = Integer.parseInt(arrBookChapterVerse[1]);
        initialBook = currentBook;
        //currentVerseMax = Integer.parseInt(arrBookChapterVerse[2]);

        listBooks = Util.createBooksList();
        listChapters = Util.createChaptersList(currentBook);
        listVerses = Util.createVersesList(currentBook, currentChapter);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            listLocales = (ArrayList<Locale>) tts.getAvailableLanguages();
//            listVoices = (ArrayList<Voice>) tts.getVoices();
//        }
//        else
//        {
//            Locale[] items = new Locale[]{Locale.FRENCH,Locale.UK, Locale.US};
//            Collections.addAll(listLocales, items);
//            spnVoice.setVisibility(View.GONE);
//        }
    }

    public void createAdapters () {
        isFromCode = true;
        ArrayAdapter<String> adapterBooks = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listBooks);
        ArrayAdapter<String> adapterChapters = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listChapters);
        ArrayAdapter<String> adapterVerses = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listVerses);
//        ArrayAdapter<Locale> adapterLocales = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listLocales);
//        ArrayAdapter<Voice> adapterVoices = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listVoices);

        adapterBooks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterChapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterVerses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapterLocales.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapterVoices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBook.setAdapter(adapterBooks);
//        if (currentChapter == listChapters.size()) {
//        }
        spnChapter.setAdapter(adapterChapters);
        spnVerseStart.setAdapter(adapterVerses);
        spnVerseEnd.setAdapter(adapterVerses);
//        spnLocale.setAdapter(adapterLocales);
//        spnVoice.setAdapter(adapterVoices);

        spnBook.setSelection(currentBook-1);
        spnChapter.setSelection(currentChapter-1);
        isFromCode = false;
    }

//    public void setLanguage (Locale locale) {
//        tts.setLanguage(locale);
//    }
    public void changeBook (int bookNumber) {
//        if (tts != null)
//            if (tts.isSpeaking())      {
//                ttsState = State.Stopped;
//                tts.stop();
//        }

        listChapters = Util.createChaptersList(bookNumber);
//        listVerses = Util.createVersesList(bookNumber, 1);
        ArrayAdapter<String> adapterChapters = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listChapters);
//        ArrayAdapter<String> adapterVerses = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listVerses);

        adapterChapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapterVerses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        isFromCode = false;

        spnChapter.setAdapter(adapterChapters);
//        spnVerseStart.setAdapter(adapterVerses);
//        spnVerseEnd.setAdapter(adapterVerses);

        currentBook = bookNumber;
        if (currentBook != initialBook) {
            initialBook = currentBook;
            currentChapter = 1;
        }
        spnChapter.setSelection(currentChapter-1);
//        currentVerseMax = listVerses.size();

//        currentChapterIdx = Util.getChapterIdx(currentBook, currentChapter);
//
//        populateVerseList(currentBibleFilename, currentChapterIdx);
    }

    public void changeChapter (int bookNumber, int chapter) {
//        if (tts != null)
//            if (tts.isSpeaking()) {
//                tts.stop();
//                ttsState = State.Stopped;
//            }

        listVerses = Util.createVersesList(bookNumber, chapter);
        ArrayAdapter<String> adapterVerses = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listVerses);

        adapterVerses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        isFromCode = false;

        spnVerseStart.setAdapter(adapterVerses);
        spnVerseEnd.setAdapter(adapterVerses);

        currentBook = bookNumber;
        if (chapter == 1 && currentBook != initialBook)
            currentChapter = chapter;
//        currentVerseMax = listVerses.size();

        currentChapterIdx = Util.getChapterIdx(currentBook, currentChapter);
        populateVerseList(currentBibleFilename, currentChapterIdx);
    }

    public void setVoice() {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spn_book)
            changeBook(position+1);
        else if (parent.getId() == R.id.spn_chapter)
            changeChapter(currentBook, position+1);
        else if (ttsState != State.Playing){
            if (Integer.parseInt(spnVerseStart.getSelectedItem().toString()) > Integer.parseInt(spnVerseEnd.getSelectedItem().toString()))
            {
                spnVerseStart.setSelection(spnVerseStart.getSelectedItemPosition());
                spnVerseEnd.setSelection(spnVerseStart.getSelectedItemPosition());
            }

            currentVerseStart = spnVerseStart.getSelectedItemPosition() + 1;
            if (switcAutoPlaey.isChecked())
                currentVerseEnd = currentVerseMax;
            else
                currentVerseEnd = spnVerseEnd.getSelectedItemPosition() + 1;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (tts == null) {
                onDestroy();
                return;
            }

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsState = State.Null;
                Log.e("TTS", "This Language is not supported");
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show();
            }
            else {
                ttsState = State.Ready;
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
            Toast.makeText(this, "Initilization Failed!", Toast.LENGTH_SHORT).show();
            ttsState = State.Null;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnPlayStop && btnPlayStop.getText().equals("PLAY")) {
            currentVerse = currentVerseStart;
            speak();
        }
        else if (view == btnPlayStop && btnPlayStop.getText().equals("STOP")) {
            ttsState = State.Stopped;
            if (tts.isSpeaking())
                tts.stop();
            ttsState = State.Stopped;
            txtStatus.setText("Stopped");
            btnPlayStop.setText("PLAY");
            spnVerseStart.setSelection(currentVerse-1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null)
            if (tts.isSpeaking())
                tts.stop();
        tts = null;
    }

    public void speak(){
       // isFinished = false;
//        if (switcAutoPlaey.isChecked())
//            currentVerseEnd = currentVerseMax;
        StringBuilder sbintro = new StringBuilder();
        sbintro.append("Reeding ").append(Constants.voiceFriendlyBookNames[currentBook-1]).append(" Chapter ")
                .append(currentChapter);
        if (currentVerseStart == currentVerseEnd)
            sbintro.append(" Verse ").append(currentVerseStart);
        else
            sbintro.append(" Verses ").append(currentVerseStart)
                    .append(" to ").append(currentVerseEnd);
        String intro = sbintro.toString();

//        ttsState = State.Playing;
//        txtStatus.setText("Playing...");
//        btnPlayStop.setText("STOP");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Log.v(TAG, "Speak new API");
//            Bundle bundle = new Bundle();
//            bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
//            bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, FINISHED_UTTERANCE_ID);
//            if (currentVerseStart == currentVerse) {
//                tts.speak(intro, TextToSpeech.QUEUE_ADD, null, null);
//                tts.playSilentUtterance(750, TextToSpeech.QUEUE_ADD, null);
//                tts.speak(Util.parseVerse(verseList.get(currentVerse-1).getVerse()), TextToSpeech.QUEUE_ADD, bundle, null);
//            }
//            else{
//                tts.playSilentUtterance(50, TextToSpeech.QUEUE_ADD, null);
//                tts.speak(Util.parseVerse(verseList.get(currentVerse-1).getVerse()), TextToSpeech.QUEUE_ADD, bundle, null);
//                }
//           // isFinished = true;
//        } else {
            Log.v(TAG, "Speak old API");
            HashMap<String, String> param = new HashMap<>();
            param.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
            param.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, FINISHED_UTTERANCE_ID);
        String text = Util.parseVerse(verseList.get(currentVerse - 1).getVerse());
        if (currentVerseStart == currentVerse) {
                tts.speak(intro, TextToSpeech.QUEUE_ADD, null);
                tts.playSilence(750, TextToSpeech.QUEUE_ADD, null);
                tts.speak(text, TextToSpeech.QUEUE_ADD, param);
            }
            else{
                tts.playSilence(50, TextToSpeech.QUEUE_ADD, null);
                tts.speak(text, TextToSpeech.QUEUE_ADD, param);
//                    lock.lock();
//                    try {
//                        done.await();
//                        isDoneSpeaking(lock);
//                    }catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } finally {
//                        lock.unlock();
//                    }
                }
            //isFinished = true;
//        }
    }

    public void populateVerseList (String bibleFilename, int chapterIndex) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            txtStatus.setText(getResources().getString(R.string.sdcard_error));
            Toast.makeText(this, R.string.sdcardNotReady, Toast.LENGTH_LONG).show();
            return;
        }

        File sdcard = Environment.getExternalStorageDirectory();

        if (chapterIndex == -1) { //no bible available
            TextView current = (TextView) findViewById(R.id.txtCurrent);
            current.setText("Error. No bible available");
            return;
        }

        File file = new File(sdcard, BIBLE_FOLDER + "/" + bibleFilename);
        String indexFileName = file.getAbsolutePath().replaceAll(".ont", ".idx");
        File fIndex = new File(indexFileName);

        String[] arrBookChapter = arrVerseCount[chapterIndex]
                .split(";");
        int verseCount = Integer.parseInt(arrBookChapter[2]);

        verseList.clear();
        BufferedReader br = null;
        try {
            DataInputStream is = new DataInputStream(new FileInputStream(fIndex));
            is.skip(chapterIndex*4);
            int startOffset = is.readInt();
            is.close();

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 8192);
            br.skip(startOffset);
            String line = "";
            boolean prevBreakParagraph = false;
            boolean verseNotAvailable = true;
            for (int i = 1; i <= verseCount; i++) {
                line = br.readLine();
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                boolean breakParagraph = false;

                line = line.replaceAll("<CL>", "\n");
                int posCM = line.indexOf("<CM>");
                if (posCM > -1) {
                    if (!line.endsWith("<CM>")) {
                        String afterCM = line.substring(posCM + "<CM>".length()).trim();
                        if (afterCM.startsWith("<") && afterCM.endsWith(">")) {
                            breakParagraph = true;
                            line = line.substring(0, posCM) + afterCM;
                        }
                    } else {
                        breakParagraph = true;
                        line = line.substring(0, line.length()-"<CM>".length());
                    }
                }
                line = line.replaceAll("<FI>", "[");
                line = line.replaceAll("<Fi>", "]");
                line = line.replaceAll("`", "'");

                line = line.replaceAll("<CM>", "\n\n");
                line = line.replaceAll("\n\n \n\n", "\n\n");
                line = line.replaceAll("\n\n\n\n", "\n\n");

                boolean bookmarked = false;
                if (prevBreakParagraph) {
                    verseList.add(new DisplayVerse(i, line, bookmarked, 0, true));
                } else {
                    verseList.add(new DisplayVerse(i, line, bookmarked,0, false));
                }
                prevBreakParagraph = breakParagraph;
                verseNotAvailable = false;
            }

            if (verseNotAvailable) {
                if (chapterIndex < 929) {
                    txtStatus.setText(getResources().getString(R.string.no_ot));
                } else {
                    txtStatus.setText(getResources().getString(R.string.no_nt));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putInt(Constants.CHAPTER_INDEX, currentChapterIdx);
        editor.apply();
        currentVerseMax = verseList.size();
        currentVerseEnd = currentVerseMax;
        currentVerseStart = 1;
        currentVerse = 1;
    }

}
