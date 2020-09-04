///* ====================================================================
// * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions
// * are met:
// *
// * 1. Redistributions of source code must retain the above copyright
// *    notice, this list of conditions and the following disclaimer.
// *
// * 2. Redistributions in binary form must reproduce the above copyright
// *    notice, this list of conditions and the following disclaimer in
// *    the documentation and/or other materials provided with the
// *    distribution.
// *
// * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
// * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
// * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
// * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// *
// * ====================================================================
// */
//
//package com.felixunlimited.pbbible.speech;
//
//import android.app.Activity;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.felixunlimited.pbbible.R;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//
//import edu.cmu.pocketsphinx.Assets;
//import edu.cmu.pocketsphinx.Hypothesis;
//import edu.cmu.pocketsphinx.RecognitionListener;
//import edu.cmu.pocketsphinx.SpeechRecognizer;
//import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
//
//import static android.widget.Toast.makeText;
//
//public class PocketSphinxActivity extends Activity implements RecognitionListener {
//
//    /* Named searches allow to quickly reconfigure the decoder */
//    private static final String KWS_SEARCH = "wakeup";
//    private static final String FORECAST_SEARCH = "forecast";
//    private static final String DIGITS_SEARCH = "digits";
//    private static final String BOOKS_SEARCH = "books";
//    private static final String PHONE_SEARCH = "phones";
//    private static final String MENU_SEARCH = "menu";
//
//    /* Keyword we are looking for to activate menu */
//    private static final String KEYPHRASE = "oh my";
//
//    private SpeechRecognizer recognizer;
//    private HashMap<String, Integer> captions;
//
//    @Override
//    public void onCreate(Bundle state) {
//        super.onCreate(state);
//        // Prepare the data for UI
//        captions = new HashMap<String, Integer>();
//        captions.put(KWS_SEARCH, R.string.kws_caption);
//        captions.put(MENU_SEARCH, R.string.menu_caption);
//        captions.put(DIGITS_SEARCH, R.string.digits_caption);
//        captions.put(PHONE_SEARCH, R.string.phone_caption);
//        captions.put(FORECAST_SEARCH, R.string.forecast_caption);
//        setContentView(R.layout.pocketsphinx);
//        ((TextView) findViewById(R.id.caption_text))
//                .setText("Preparing the recognizer");
//
//        runRecognizerSetup();
//    }
//
//    private void runRecognizerSetup() {
//        // Recognizer initialization is a time-consuming and it involves IO,
//        // so we execute it in async task
//        new AsyncTask<Void, Void, Exception>() {
//            @Override
//            protected Exception doInBackground(Void... params) {
//                try {
//                    Assets assets = new Assets(PocketSphinxActivity.this);
//                    File assetDir = assets.syncAssets();
//                    //convertFileToLowerCase(new File(getFilesDir(), "4415.dic"));
//                    setupRecognizer(assetDir);
//                } catch (IOException e) {
//                    return e;
//                }
//                return null;
//            }
//
//            public void convertFileToLowerCase(File file) {
//                FileInputStream fin = null;
//                try {
//                    fin = new FileInputStream(file);
//                    InputStreamReader tmp = new InputStreamReader(fin);
//                    BufferedReader reader = new BufferedReader(tmp);
//                    String str = "";
//                    StringBuilder buf = new StringBuilder();
//                    while ((str = reader.readLine()) != null) {
//                        String book = str.split("\t")[0];
//                        //str.replaceAll(book, book.toLowerCase()+"56");
//                        buf.append(str).append("\n");
//                        int i  = buf.indexOf(book);
//                        int j = book.length();
//                        buf.replace(i, j-1, book.toLowerCase());
//                    }
//                    fin.close();
//                    FileWriter fileWriter = new FileWriter(file, false);
//                    fileWriter.write(buf.toString());
//                    fileWriter.close();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            protected void onPostExecute(Exception result) {
//                if (result != null) {
//                    ((TextView) findViewById(R.id.caption_text))
//                            .setText("Failed to init recognizer " + result);
//                } else {
//                    switchSearch(PHONE_SEARCH);
//                }
//            }
//        }.execute();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        if (recognizer != null) {
//            recognizer.cancel();
//            recognizer.shutdown();
//        }
//    }
//
//    /**
//     * In partial result we get quick updates about current hypothesis. In
//     * keyword spotting mode we can react here, in other modes we need to wait
//     * for final result in onResult.
//     */
//    String previousText = "";
//    @Override
//    public void onPartialResult(Hypothesis hypothesis) {
//        if (hypothesis == null)
//            return;
//
//        String text = hypothesis.getHypstr();
//        previousText = hypothesis.getHypstr();
//
////        if (text.equals(KEYPHRASE))
////            switchSearch(MENU_SEARCH);
//////        else if (text.equals(DIGITS_SEARCH))
//////            switchSearch(DIGITS_SEARCH);
////        else if (text.equals(PHONE_SEARCH))
////            switchSearch(PHONE_SEARCH);
////        else if (text.equals(FORECAST_SEARCH))
////            switchSearch(FORECAST_SEARCH);
////        else
//            ((TextView) findViewById(R.id.result_text)).setText(text+":"+hypothesis.getBestScore()+",");
//        //Toast.makeText(this, text+":"+hypothesis.getBestScore()+",", Toast.LENGTH_SHORT).show();
//    }
//
//    /**
//     * This callback is called when we stop the recognizer.
//     */
//    @Override
//    public void onResult(Hypothesis hypothesis) {
//        ((TextView) findViewById(R.id.result_text)).setText("");
//        if (hypothesis != null) {
//            String text = hypothesis.getHypstr();
//            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onBeginningOfSpeech() {
//    }
//
//    /**
//     * We stop recognizer here to get a final result
//     */
//    @Override
//    public void onEndOfSpeech() {
//        if (!recognizer.getSearchName().equals(PHONE_SEARCH))
//            switchSearch(PHONE_SEARCH);
//    }
//
//    private void switchSearch(String searchName) {
//        recognizer.stop();
//
//        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
//        if (searchName.equals(PHONE_SEARCH))
//            recognizer.startListening(searchName);
//        else
//            recognizer.startListening(searchName, 10000);
//
//        String caption = getResources().getString(captions.get(searchName));
//        ((TextView) findViewById(R.id.caption_text)).setText(caption);
//    }
//
//    private void setupRecognizer(File assetsDir) throws IOException {
//        // The recognizer can be configured to perform multiple searches
//        // of different kind and switch between them
//
//        recognizer = SpeechRecognizerSetup.defaultSetup()
//                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
//                .setDictionary(new File(assetsDir, "4415.dic"))
////                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
//
//                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
//
//                .getRecognizer();
//        recognizer.addListener(this);
//
//        /** In your application you might not need to add all those searches.
//         * They are added here for demonstration. You can leave just one.
//         */
//
//        // Create keyword-activation search fro a list of keywords.
//        File booksKWS = new File(assetsDir, "books.kws");
//        recognizer.addKeywordSearch(KWS_SEARCH, booksKWS);
//
//        // Create keyword-activation search.
////        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
//
//        // Create grammar-based search for selection between demos
////        File menuGrammar = new File(assetsDir, "menu.gram");
////        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
////
////        // Create grammar-based search for digit recognition
////        File digitsGrammar = new File(assetsDir, "digits.gram");
////        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
//////        File booksGrammar = new File(assetsDir, "books.gram");
//////        recognizer.addGrammarSearch(BOOKS_SEARCH, booksGrammar);
////
////        // Create language model search
////        File languageModel = new File(assetsDir, "weather.dmp");
////        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
////
//        // Phonetic search
////        File phoneticModel = new File(assetsDir, "en-phone.dmp");
////        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
//        File phoneticModel = new File(assetsDir, "4415.lm");
//        recognizer.addNgramSearch(PHONE_SEARCH, phoneticModel);
//    }
//
//    @Override
//    public void onError(Exception error) {
//        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
//    }
//
//    @Override
//    public void onTimeout() {
//        switchSearch(PHONE_SEARCH);
//    }
//}
