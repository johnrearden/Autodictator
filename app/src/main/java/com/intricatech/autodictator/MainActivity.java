package com.intricatech.autodictator;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    public static final String[] ERROR_CODES = new String[] {
            "ERROR_NETWORK_TIMEOUT (1)",
            "ERROR_NETWORK (2)",
            "ERROR_AUDIO (3)",
            "ERROR_SERVER (4)",
            "ERROR_CLIENT (5)",
            "ERROR_SPEECH_TIMEOUT (6)",
            "ERROR_NO_MATCH (7)",
            "ERROR_RECOGNIZER_BUSY (8)",
            "ERROR_INSUFFICIENT_PERMISSIONS (9)",
    };

    private final int REQUEST_SPEECH_RECOGNIZER = 1000;
    private final String SPEAK_PROMPT = "Speak now, earthling!";
    private String TAG;

    private TextView dictationOutputTV;
    // todo add support for touch editing with a textwatcher on the textview.
    private TextView interpreterSelectorTV;
    private TextView masterStateIndicatorTV;
    private TextView isSpeakingIndicatorTV;
    private Button startDictationButton;
    private StringBuilder cumulativeResults;

    private SpeechRecognizer recognizer;
    private boolean recognizerReady = true;

    private SpeakerFacade speaker;
    private StorageInterface storage;
    private AbstractInterpreter currentInterpreter;
    private InterpreterSelector interpreterSelector;
    private ResultsUnderEvaluation resultsUnderEvaluation;
    private Document document;

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
        StorageFacade.initialize(getApplicationContext());
        storage = StorageFacade.getInstance();
        currentInterpreter = StandardInterpreter.getInstance();
        interpreterSelector = InterpreterSelector.STANDARD_INTERPRETER;
        resultsUnderEvaluation = new ResultsUnderEvaluation();
        document = storage.retrieveEntireDocument(0);

        // Initialize GUI elements.
        dictationOutputTV = findViewById(R.id.dictation_output);
        interpreterSelectorTV = findViewById(R.id.interpreter_selector);
        masterStateIndicatorTV = findViewById(R.id.master_state_indicator);
        isSpeakingIndicatorTV = findViewById(R.id.isspeaking_indicator);
        updateInterpreterSelectorTV();

        startDictationButton = (Button) findViewById(R.id.start_dictation_button);
        startDictationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenForVoiceInBackground();
            }
        });

        speaker = new TextSpeaker(getApplicationContext());
        ReadInterpreter.configure(speaker);
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);


        switchToDictating();

        cumulativeResults = new StringBuilder();


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause invoked");
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
        sb.append(document.returnEntireDocumentAsString());
        for (Word word : resultsUnderEvaluation.getCurrentResultsWordList()) {
            if (word.getType() != WordType.KEYWORD) {
                sb.append(word.getWordString() + " ");
            }
        }
        dictationOutputTV.setText(/*cumulativeResults.toString() + " " + */sb.toString());
    }

    void updateIsSpeakingIndicator(boolean isSpeaking) {
        if (isSpeaking) {
            isSpeakingIndicatorTV.setBackgroundColor(Color.RED);
            isSpeakingIndicatorTV.setText("Speaking");
        } else {
            isSpeakingIndicatorTV.setBackgroundColor(Color.GREEN);
            isSpeakingIndicatorTV.setText("Quiescent");
        }
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
        String toastText = "Error : (" + error + ") " + ERROR_CODES[error - 1];
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
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
            case SpeechRecognizer.ERROR_NO_MATCH:
                recoverFromError();
                listenForVoiceInBackground();
            default : {
                Log.d(TAG, "onError : " + error);
                Log.d(TAG, "Collosal motherfucking other type error occurred. Apols.");
                recoverFromError();
            }
        }

        // todo Diagnose problems with the calling intent by removing extras one-by-one - provide toast and report to indicate remedial action needed.
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "onResults() invoked");

        processResults(results);

        for (Word word : resultsUnderEvaluation.getCurrentResultsWordList()) {
            if (word.getType() != WordType.KEYWORD) {
                cumulativeResults.append(word.getWordString() + " ");
            }
        }

        document.commitResults(resultsUnderEvaluation, storage);

        // Start listening again immediately.
        listenForVoiceInBackground();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults() invoked");
        processResults(partialResults);
    }

    private void processResults(Bundle results) {
        updateIsSpeakingIndicator(speaker.isSpeaking());
        List<String> res = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        boolean newWordAdded = currentInterpreter.interpret(
                document,
                resultsUnderEvaluation,
                res.get(0),
                masterState,
                speaker.isSpeaking());
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
