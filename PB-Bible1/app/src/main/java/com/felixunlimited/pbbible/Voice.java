package com.felixunlimited.pbbible;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Voice extends Activity {
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
    private String currentBookLanguage;
    private int currentChapterIdx;
    private int currentVerseIdx;
    private String[] matches;
    Button btnSay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setTheme(this, R.style.AppBaseTheme_Dialog_Light);
        setContentView(R.layout.activity_voice);
        readPreference();
        btnSay = (Button) findViewById(R.id.btnSay);
        btnSay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkVoiceRecognition();
            }
        });
        checkVoiceRecognition();
    }

    private void readPreference() {
        SharedPreferences preference = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        currentBookLanguage = preference.getString(Constants.BOOK_LANGUAGE, Constants.LANG_ENGLISH);
        currentChapterIdx = preference.getInt(Constants.POSITION_CHAPTER, 0);
        currentVerseIdx = preference.getInt(Constants.POSITION_VERSE, 0);
    }

    public void checkVoiceRecognition() {
        // Check if voice recognition is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            showToastErrorMessage("Voice recognizer not present");
            finish();
        }
        else
            speak();
    }

    public void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "eg, say: 'Matthew chapter 3 verse 7'");

        // Given an hint to the recognizer about what the user is going to say
        //There are two form of language model available
        //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
        //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

        //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        //Start the Voice recognizer activity for the result.
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
    long start;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)

            //If Voice recognition is successful then it returns RESULT_OK
            if(resultCode == RESULT_OK) {

                ArrayList<String> textMatchList = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                showToastMessage("matches:"+textMatchList.size());
                for (String word :
                        textMatchList) {
                    showToastMessage(word);
                }

                if (!textMatchList.isEmpty()) {
                    matches = new String[textMatchList.size()];
                    matches = textMatchList.toArray(matches);
                    // If first Match contains the 'search' word
                    // Then start web search.
//                    if (textMatchList.get(0).contains("search")) {
//                        String searchQuery = textMatchList.get(0);
//                        searchQuery = searchQuery.replace("search","");
//                        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
//                        search.putExtra(SearchManager.QUERY, searchQuery);
//                        startActivity(search);
//                    }

                    int n = 0;
                    start = System.currentTimeMillis();
                    outerLoop:
                    //iterate through matches, each match is a sentence
                    for (String match : matches) {
                        match = match.toLowerCase();
                        // iterate through the words of a match (sentence)
                        for (int i = 0; i < Constants.voiceFriendlyBookNames.length; i++) {
                            //check if matched sentence contains particular book
                            if (match.contains(Constants.voiceFriendlyBookNames[i].toLowerCase())) {
                                showToastMessage("matched");
                                goToBook(Constants.arrBookName[i], com.felixunlimited.pbbible.Util.getChapterAndVerse(match));
                                break outerLoop;
                            }
                            else if (match.contains("first")) {
                                for (int j = 0; j < Constants.booksWithMultipleVariants.length; j++) {
                                    String nextWord = match.split("first ")[1].split(" ")[0];
                                    if (nextWord.equals(Constants.booksWithMultipleVariants[j])) {
                                        showToastMessage("matched");
                                        goToBook("1 "+Constants.booksWithMultipleVariants[j], com.felixunlimited.pbbible.Util.getChapterAndVerse(match.split(Constants.booksWithMultipleVariants[j])[1]));
                                        break outerLoop;
                                    }
                                }
                            }
                            else if (match.contains("1st")) {
                                for (int j = 0; j < Constants.booksWithMultipleVariants.length; j++) {
                                    String nextWord = match.split("1st ")[1].split(" ")[0];
                                    if (nextWord.equals(Constants.booksWithMultipleVariants[j])) {
                                        showToastMessage("matched");
                                        goToBook("1 "+Constants.booksWithMultipleVariants[j], com.felixunlimited.pbbible.Util.getChapterAndVerse(match.split(Constants.booksWithMultipleVariants[j])[1]));
                                        break outerLoop;
                                    }
                                }
                            }
                            else if (match.contains("second")) {
                                for (int j = 0; j < Constants.booksWithMultipleVariants.length; j++) {
                                    String nextWord = match.split("second ")[1].split(" ")[0];
                                    if (nextWord.equals(Constants.booksWithMultipleVariants[j])) {
                                        showToastMessage("matched");
                                        goToBook("2 "+Constants.booksWithMultipleVariants[j], com.felixunlimited.pbbible.Util.getChapterAndVerse(match.split(Constants.booksWithMultipleVariants[j])[1]));
                                        break outerLoop;
                                    }
                                }
                            }
                            else if (match.contains("2nd")) {
                                for (int j = 0; j < Constants.booksWithMultipleVariants.length; j++) {
                                    String nextWord = match.split("2nd ")[1].split(" ")[0];
                                    if (nextWord.equals(Constants.booksWithMultipleVariants[j])) {
                                        showToastMessage("matched");
                                        goToBook("2 "+Constants.booksWithMultipleVariants[j], com.felixunlimited.pbbible.Util.getChapterAndVerse(match.split(Constants.booksWithMultipleVariants[j])[1]));
                                        break outerLoop;
                                    }
                                }
                            }
                            else if (match.contains("third john") || match.contains("3rd john")) {
                                showToastMessage("matched");
                                goToBook("3 John", com.felixunlimited.pbbible.Util.getChapterAndVerse(match.replace("third john", "").replace("3rd john", "")));
                                break outerLoop;
                            }
                            n++;
                        }
                    }

                    if (n == 66 * matches.length)
                        displayMatches(matches);
                }
            // Result code for various error.
            }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
                showToastErrorMessage("Audio Error");
            }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
                showToastErrorMessage("Client Error");
            }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
                showToastErrorMessage("Network Error");
            }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
                showToastErrorMessage("No Match");
            }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
                showToastErrorMessage("Server Error");
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displayMatches(String[] matches)
    {
        showToastMessage("You probably said");
        for (String match : matches) {
            showToastMessage(match);
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        startActivity(new Intent(this, BiblesOffline.class));
//    }

    void goToBook(String searchBook, int[] chapterAndVerse){
        //showToastMessage("Iteration time: "+(System.currentTimeMillis() - start));
        if (searchBook.length() == 0) {
            showToastErrorMessage("Try again. Please speak clearly");
        }

        int goChapter = 0;
        int goVerse = 0;
        int goBook = 0;

        if (chapterAndVerse.length > 0)
            goChapter = chapterAndVerse[0];
        if (chapterAndVerse.length > 1)
            goVerse = chapterAndVerse[1];

        //showToastMessage("Book: "+searchBook+" chapter: "+goChapter+" Verse: "+goVerse);
        if (searchBook.length() > 0) {
            searchBook = searchBook.replaceAll(" ", "");
            searchBook = searchBook.toLowerCase();

            String[] firstSearch = null;
            String[] secondSearch = null;

            firstSearch = Constants.arrActiveBookName;
            secondSearch = Constants.arrActiveBookAbbr;

            for (int i = 0; i < firstSearch.length; i++) {
                String book = firstSearch[i].toLowerCase();
                book = book.replaceAll(" ", "");
                if (book.startsWith(searchBook)) {
                    goBook = i + 1;
                    break;
                }
            }

            if (goBook == 0) {
                for (int i = 0; i < secondSearch.length; i++) {
                    String book = secondSearch[i].toLowerCase();
                    book = book.replaceAll(" ", "");
                    if (book.startsWith(searchBook)) {
                        goBook = i + 1;
                        break;
                    }
                }
            }
        }

        if (goBook > 0 || goChapter > 0) {
            if (goBook == 0) {
                String[] arrBookChapter = Constants.arrVerseCount[currentChapterIdx].split(";");
                goBook = Integer.parseInt(arrBookChapter[0]);
            }
            if (goChapter == 0) {
                goChapter = 1;
            }
            if (goVerse == 0) {
                goVerse = 1;
            }
            String bookChapter = goBook + ";" + goChapter;
            for (int i = Constants.arrBookStart[goBook - 1]; i < Constants.arrVerseCount.length; i++) {
                if (Constants.arrVerseCount[i].startsWith(bookChapter)) {
                    currentChapterIdx = i;
                    String[] arrBookVerse = Constants.arrVerseCount[i].split(";");
                    if (goVerse > Integer.parseInt(arrBookVerse[2]))
                        currentVerseIdx = 0;
                    else
                        currentVerseIdx = goVerse;

                    break;
                }
            }
        }

        searchBook = Constants.arrActiveBookName[goBook-1];
        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putInt(Constants.POSITION_CHAPTER, currentChapterIdx);
        editor.putInt(Constants.POSITION_VERSE, currentVerseIdx);
        editor.apply();
        com.felixunlimited.pbbible.notes.Util.saveFromVoice(this, searchBook+" "+goChapter+":"+goVerse+"");
        Intent showBibleActivity = new Intent(this, BiblesOffline.class);
        showBibleActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        showBibleActivity.putExtra(Constants.FROM_BOOKMARKS, true);
        showBibleActivity.putExtra(Constants.BOOKMARK_VERSE_START, currentVerseIdx);
        startActivity(showBibleActivity);

        finish();
    }
    /**
     * Helper method to show the toast message
     **/
    void showToastErrorMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
    void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
//import java.util.ArrayList;
//
//import android.os.Bundle;
//import android.app.Activity;
//import android.content.Intent;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ListView;
//import android.speech.RecognizerIntent;
//import android.support.v4.app.NavUtils;
//
//public class Voice extends Activity implements OnClickListener {
//
//    public ListView mList;
//    public Button speakButton;
//
//    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_voice);
//
//        speakButton = (Button) findViewById(R.id.btn_speak);
//        speakButton.setOnClickListener(this);
//
//        voiceinputbuttons();
//    }
//
//    public void informationMenu() {
//        speakButton.setText("Success");
//        //startActivity(new Intent("android.intent.action.INFOSCREEN"));
//    }
//
//    public void voiceinputbuttons() {
//        //speakButton = (Button) findViewById(R.id.btn_speak);
//        mList = (ListView) findViewById(R.id.list);
//    }
//
//    public void startVoiceRecognitionActivity() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
//                "Speech recognition demo");
//        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
//    }
//
//    public void onClick(View v) {
//        // TODO Auto-generated method stub
//        startVoiceRecognitionActivity();
//        speakButton.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
//            speakButton.setVisibility(View.VISIBLE);
//            // Fill the list view with the strings the recognizer thought it
//            // could have heard
//            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//            mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches));
//            // matches is the result of voice input. It is a list of what the
//            // user possibly said.
//            // Using an if statement for the keyword you want to use allows the
//            // use of any activity if keywords match
//            // it is possible to set up multiple keywords to use the same
//            // activity so more than one word will allow the user
//            // to use the activity (makes it so the user doesn't have to
//            // memorize words from a list)
//            // to use an activity from the voice input information simply use
//            // the following format;
//            // if (matches.contains("keyword here") { startActivity(new
//            // Intent("name.of.manifest.ACTIVITY")
//
//            if (matches.contains("information")) {
//                informationMenu();
//            }
//        }
//    }
//}
//
