package com.intricatech.autodictator;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private final int REQUEST_SPEECH_RECOGNIZER = 1000;
    private final String SPEAK_PROMPT = "Speak now, earthling!";
    private String TAG;

    private TextView dictationOutputTV;
    private TextView interpreterSelectorTV;
    private TextView masterStateIndicatorTV;
    private Button startDictationButton;
    private StringBuilder cumulativeResults;

    private SpeechRecognizer recognizer;
    private boolean recognizerReady = true;

    private SpeakerFacade speaker;
    private AbstractInterpreter currentInterpreter;
    private InterpreterSelector interpreterSelector;
    private ResultsUnderEvaluation resultsUnderEvaluation;

    private long startListeningTime;
    private long startDeadTime;

    enum MasterState {
        EDITING,
        DICTATING
    }
    private MasterState masterState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getClass().getSimpleName();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Create objects.
        currentInterpreter = StandardInterpreter.getInstance();
        interpreterSelector = InterpreterSelector.STANDARD_INTERPRETER;
        resultsUnderEvaluation = new ResultsUnderEvaluation();


        // Initialize GUI elements.
        dictationOutputTV = (TextView) findViewById(R.id.dictation_output);
        interpreterSelectorTV = (TextView) findViewById(R.id.interpreter_selector);
        masterStateIndicatorTV = (TextView) findViewById(R.id.master_state_indicator);
        updateInterpreterSelectorTV();

        startDictationButton = (Button) findViewById(R.id.start_dictation_button);
        startDictationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenForVoiceInBackground();
            }
        });
        speaker = new TextSpeaker(getApplicationContext());

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);

        switchToDictating();

        cumulativeResults = new StringBuilder();

        ReadInterpreter.getInstance().configure(speaker);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause invoked");
        unmute_MUSIC_STREAM();
        if (recognizer != null) {
            recognizer.destroy();
        }
    }

    private void listenForVoiceInBackground() {

        // Default to DICTATING each time the listener is restarted.
        changeInterpreter(InterpreterSelector.STANDARD_INTERPRETER);
        switchToDictating();

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(this);
        }
        Log.d(TAG, "listenForVoiceInBackground() invoked");
        //mute_MUSIC_STREAM();
        if (recognizerReady) {
            Log.d(TAG, "recognizer is ready");

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, SPEAK_PROMPT);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            recognizer.startListening(intent);
        }
    }

    private void updateDictationOutputTextView(ResultsUnderEvaluation resultsUnderEvaluation) {
        StringBuilder sb = new StringBuilder();
        for (Word word : resultsUnderEvaluation.getCurrentResultsWordList()) {
            if (word.getType() != WordType.KEYWORD) {
                sb.append(word.getWordString() + " ");
            }
        }
        dictationOutputTV.setText(cumulativeResults.toString() + " " + sb.toString());
    }

    private void updateInterpreterSelectorTV() {
        interpreterSelectorTV.setText(interpreterSelector.toString().toUpperCase());
        interpreterSelectorTV.setBackgroundColor(interpreterSelector.color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        recognizerReady = true;
        startListeningTime = System.nanoTime();
        Log.d(TAG, "onReadyForSpeech");
        double time = (double)(System.nanoTime() - startDeadTime) / 1000000;
        Log.d(TAG, "Dead time : " + String.format("%.2f", time));
    }

    @Override
    public void onBeginningOfSpeech() {        Log.d(TAG, "onBeginningOfSpeech");

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "onBufferReceived");

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        startDeadTime = System.nanoTime();
        switch (error) {
            case SpeechRecognizer.ERROR_CLIENT:
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                Log.d(TAG, "onError : " + error);
                recoverFromError();
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: {
                Log.d(TAG, "onError : " + error);
                double time = (double)(System.nanoTime() - startListeningTime) / 1000000;
                Log.d(TAG, "Time before timeout : " + String.format("%.2f", time));
                recoverFromError();
                break;
            }
            default : {
                Log.d(TAG, "onError : " + error);
                Log.d(TAG, "Collosal motherfucking other type error occurred. Apols.");
                recoverFromError();
            }
        }


    }

    @Override
    public void onResults(Bundle results) {
        processResults(results);

        for (Word word : resultsUnderEvaluation.getCurrentResultsWordList()) {
            if (word.getType() != WordType.KEYWORD) {
                cumulativeResults.append(word.getWordString() + " ");
            }
        }

        // Start listening again immediately.
        listenForVoiceInBackground();
/*
        unmute_MUSIC_STREAM();
*/
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        processResults(partialResults);
    }

    private void processResults(Bundle results) {
        List<String> res = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        boolean newWordAdded = currentInterpreter.interpret(
                resultsUnderEvaluation,
                res.get(0),
                masterState);
        if (newWordAdded) {
            Log.d(TAG, res.get(0));

            // Check last 2 words for match with the master keywords:
            String lastTwoWords = resultsUnderEvaluation.getLastTwoWordsAsString();
            if (AbstractInterpreter.shouldSwitchToEditMode(lastTwoWords)) {
                switchToEditing();
            }

            String keywordCandidate = resultsUnderEvaluation.getLastWord().getWordString();
            updateDictationOutputTextView(resultsUnderEvaluation);
            InterpreterSelector appropriateIntSel = interpreterSelector.queryAppropriateInterpreterSelector(
                    keywordCandidate,
                    currentInterpreter);
            if (appropriateIntSel != interpreterSelector) {
                changeInterpreter(appropriateIntSel);
            }
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private void mute_MUSIC_STREAM() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                0
        );
    }

    private void unmute_MUSIC_STREAM() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_UNMUTE,
                0
        );
    }

    private void recoverFromError() {
        recognizer.cancel();
        recognizer.destroy();
        recognizer = null;
    }

    private void switchToEditing() {
        masterState = MasterState.EDITING;
        masterStateIndicatorTV.setText(masterState.toString());
    }

    private void switchToDictating() {
        masterState = MasterState.DICTATING;
        masterStateIndicatorTV.setText(masterState.toString());
    }

    private void changeInterpreter(InterpreterSelector newSelector) {
        interpreterSelector = newSelector;
        updateInterpreterSelectorTV();
        currentInterpreter = newSelector.getInterpreter();
        Log.d(TAG, "Interpreter has changed : " + currentInterpreter.getClass());
    }
}
