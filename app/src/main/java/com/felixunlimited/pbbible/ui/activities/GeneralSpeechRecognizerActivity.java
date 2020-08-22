package com.felixunlimited.pbbible.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.TextView;

import com.felixunlimited.pbbible.R;

import java.util.ArrayList;

public class GeneralSpeechRecognizerActivity extends Activity implements RecognitionListener {

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_speech_recognizer);
        mSpeechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

//        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        mSpeechRecognizer.setRecognitionListener(this);
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle results) {
        if ((results != null)
                && results.containsKey(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)) {
            ArrayList<String> heard =
                    results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
            float[] scores = results.getFloatArray(android.speech.SpeechRecognizer.CONFIDENCE_SCORES);

            if (heard != null) {
                for (int i = 0; i < heard.size(); i++) {
                    if (scores != null) {
                        ((TextView) findViewById(R.id.full_result_text)).setText(heard.get(i)+":"+scores[i]+", ");
                    }

                }
            }
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        if ((partialResults != null) && partialResults.containsKey(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)) {
            ArrayList<String> heard = partialResults.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
            float[] scores = partialResults.getFloatArray(android.speech.SpeechRecognizer.CONFIDENCE_SCORES);

            if (heard != null) {
                for (int i = 0; i < heard.size(); i++) {
                    if (scores != null) {
                        ((TextView) findViewById(R.id.partial_result_text)).setText(heard.get(i)+":"+scores[i]+", ");
                    }

                }
            }
        }

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
        }
    }
}
