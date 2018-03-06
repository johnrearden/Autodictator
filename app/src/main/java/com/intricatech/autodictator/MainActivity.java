package com.intricatech.autodictator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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

public class MainActivity extends AppCompatActivity
        implements RecognitionListener,
                   InterpreterClient,
                   SpeechObserver{

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
    public static final String DOC_PREFS = "DOCUMENT_PREFERENCES";
    public static final String LAST_USED_DOC_ID = "LAST_USED_DOC_ID";
    public static final long SOUND_METER_SAMPLING_DELAY = 100;

    private final int REQUEST_SPEECH_RECOGNIZER = 1000;
    private final String SPEAK_PROMPT = "Speak now, earthling!";
    private String TAG;

    private TextView dictationOutputTV;
    // todo add support for touch editing with a textwatcher on the textview.
    private TextView interpreterSelectorTV;
    private TextView masterStateIndicatorTV;
    private TextView isSpeakingIndicatorTV;
    private TextView isListeningIndicatorTV;
    private Button startDictationButton;
    private Button deleteDocFromDBButton;

    private SpeechRecognizer recognizer;
    private boolean recognizerReady = true;
    private boolean isListening;
    private boolean isSpeaking;

    private SpeakerFacade speaker;
    private Storable storage;
    private AbstractInterpreter currentInterpreter;
    private InterpreterSelector interpreterSelector;
    private Results results;
    private Document document;
    private SoundMeter soundMeter;
    private ListeningIndicatorHum indicatorHum;
    private Handler uiUpdateHandler;
    private Handler soundLevelHandler;
    private SharedPreferences docPrefs;
    private SharedPreferences.Editor docPrefsEditor;

    private long startListeningTime;
    private long startDeadTime;

    private MasterState masterState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getClass().getSimpleName();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configure singleton interpreters.
        StandardInterpreter.getInstance().configure(this);
        ReadInterpreter.getInstance().configure(this);

        // Create objects.
        StorageFacade.initialize(getApplicationContext());
        storage = StorageFacade.getInstance();
        currentInterpreter = StandardInterpreter.getInstance();
        interpreterSelector = InterpreterSelector.STANDARD_INTERPRETER;
        currentInterpreter = interpreterSelector.getInterpreter();
        results = new Results();
        uiUpdateHandler = new Handler();
        soundLevelHandler = new Handler();
        speaker = new TextSpeaker(getApplicationContext(), uiUpdateHandler);
        ((SpeechDirector)speaker).register(this);
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);
        docPrefs = getSharedPreferences(DOC_PREFS, MODE_PRIVATE);
        docPrefsEditor = docPrefs.edit();
        soundMeter = new SoundMeter();

        // Initialize GUI elements.
        dictationOutputTV = findViewById(R.id.dictation_output);
        interpreterSelectorTV = findViewById(R.id.interpreter_selector);
        masterStateIndicatorTV = findViewById(R.id.master_state_indicator);
        isSpeakingIndicatorTV = findViewById(R.id.isspeaking_indicator);
        isListeningIndicatorTV = findViewById(R.id.is_listening_indicator);
        updateInterpreterSelectorTV();
        updateIsSpeakingIndicator(false);

        startDictationButton = findViewById(R.id.start_dictation_button);
        startDictationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenForVoiceInBackground();
            }
        });
        deleteDocFromDBButton = findViewById(R.id.delete_doc_from_db);
        deleteDocFromDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storage.deleteDocument(document.getDocumentID());
                { // todo - remove duplicated code - see onResume()
                    long docID = storage.createNewDocument("New Doc");
                    document = new Document(docID);
                    docPrefsEditor.putLong(LAST_USED_DOC_ID, docID);
                    docPrefsEditor.commit();
                }
                results = new Results();
                updateDictationOutputTextView(results);
            }
        });

        switchToDictating();
    }

    @Override
    protected void onResume() {
        super.onResume();

        indicatorHum = new ListeningIndicatorHum(getApplicationContext());

        ((StorageFacade)storage).logAllDatabaseTables();
        if (storage.doAnyDocumentsExist() == false) {
            long docID = storage.createNewDocument("New Doc");
            document = new Document(docID);
            docPrefsEditor.putLong(LAST_USED_DOC_ID, docID);
            docPrefsEditor.commit();
            //Log.d(TAG, "No documents in database, created new Document (ID == " + docID + ")");
        } else {
            long lastUsedDocID = docPrefs.getLong(LAST_USED_DOC_ID, 1);
            document = storage.retrieveEntireDocument(lastUsedDocID);
            updateDictationOutputTextView(results);
            //Log.d(TAG, "Retrieved last used document from database (ID == " + lastUsedDocID + ")");
        }
        Log.d(TAG, document.toString());
        soundMeter.start();
        soundLevelHandler.postDelayed(pollSoundLevelTask, SOUND_METER_SAMPLING_DELAY);
        isListening = false;
        isSpeaking = false;
        updateIsListeningIndicatorTV(isListening);
        updateIsSpeakingIndicator(isSpeaking);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause invoked");
        Log.d(TAG, document.toString());
        if (recognizer != null) {
            recognizer.destroy();
        }
        storage.storeEntireDocument(document);
        docPrefsEditor.putLong(LAST_USED_DOC_ID, document.getDocumentID());
        docPrefsEditor.commit();
        soundLevelHandler.removeCallbacks(pollSoundLevelTask);
        soundMeter.stop();
        indicatorHum.onPause();
    }

    private void listenForVoiceInBackground() {
        onListeningStarted();
        Log.d(TAG, "listenForVoiceInBackground() invoked");
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(this);
        }
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

    private void updateDictationOutputTextView(Results results) {
        StringBuilder sb = new StringBuilder();
        sb.append(document.returnEntireDocumentAsString());
        for (Word word : results.getWordList()) {
            if (word.getType() != WordType.KEYWORD && word.getType() != WordType.IGNORED) {
                sb.append(word.getWordString() + " ");
            }
        }
        dictationOutputTV.setText(/*cumulativeResults.toString() + " " + */sb.toString());
    }

    void updateIsSpeakingIndicator(boolean isSpeaking) {
        Log.d(TAG, "updateIsSpeakingIndicator() invoked");
        if (isSpeaking) {
            isSpeakingIndicatorTV.setText("Speaking");
            isSpeakingIndicatorTV.setBackgroundColor(Color.RED);
        } else {
            isSpeakingIndicatorTV.setText("Quiescent");
            isSpeakingIndicatorTV.setBackgroundColor(Color.GREEN);

        }
    }

    private void updateInterpreterSelectorTV() {
        interpreterSelectorTV.setText(interpreterSelector.toString().toUpperCase());
        interpreterSelectorTV.setBackgroundColor(interpreterSelector.color);
    }

    private void updateIsListeningIndicatorTV(boolean isListening) {
        if (isListening) {
            isListeningIndicatorTV.setText("isListening : true");
        } else {
            isListeningIndicatorTV.setText("isListening : false");
        }
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
        onListeningStopped();
        String toastText = "Error : (" + error + ") " + ERROR_CODES[error - 1];
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        switch (error) {
            case SpeechRecognizer.ERROR_CLIENT:
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                Log.d(TAG, "onError : " + error);
                murderMalfunctioningRecognizerAndDisposeOfTheBody();
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: {
                Log.d(TAG, "onError : " + error);
                double time = (double)(System.nanoTime() - startListeningTime) / 1000000;
                Log.d(TAG, "Time before timeout : " + String.format("%.2f", time));
                murderMalfunctioningRecognizerAndDisposeOfTheBody();
                break;
            }
            case SpeechRecognizer.ERROR_NO_MATCH:
                murderMalfunctioningRecognizerAndDisposeOfTheBody();
                //listenForVoiceInBackground();
                break;
            default : {
                Log.d(TAG, "onError : " + error);
                Log.d(TAG, "Collosal motherfucking other type error occurred. Apols.");
                murderMalfunctioningRecognizerAndDisposeOfTheBody();
            }
        }

        // todo Diagnose problems with the calling intent by removing extras one-by-one - provide toast and report to indicate remedial action needed.
    }

    @Override
    public void onResults(Bundle recognizerOutput) {
        Log.d(TAG, "onResults() invoked");
        onListeningStopped();
        processResults(recognizerOutput);
        document.commitResults(results, storage);

        // Start listening again immediately.
        //listenForVoiceInBackground();
    }

    @Override
    public void onPartialResults(Bundle recognizerOutput) {
        Log.d(TAG, "onPartialResults() invoked");
        processResults(recognizerOutput);
    }

    private void processResults(Bundle recognizerOutput) {
        List<String> res = recognizerOutput.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        currentInterpreter.interpret(res.get(0), results);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private void murderMalfunctioningRecognizerAndDisposeOfTheBody() {
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

    public MasterState getMasterState() {
        return masterState;
    }

    @Override
    public void onSpeechStarted() {
        isSpeaking = true;
        updateIsSpeakingIndicator(isSpeaking);

        isListening = false;
        updateIsListeningIndicatorTV(isListening);
        indicatorHum.pause();

        /*recognizer.stopListening();*/ // Not working as per API, so.....
        document.commitResults(results, storage);
        murderMalfunctioningRecognizerAndDisposeOfTheBody();
    }

    @Override
    public void onSpeechEnded() {
        isSpeaking = false;
        updateIsSpeakingIndicator(isSpeaking);
        soundMeter.start();
        /*listenForVoiceInBackground();*/
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
    public Document getDocument() {
        return document;
    }

    @Override
    public void onFinishedInterpreting() {
        interpreterSelector = InterpreterSelector.STANDARD_INTERPRETER;
        currentInterpreter = interpreterSelector.getInterpreter();
        switchToDictating();
        updateInterpreterSelectorTV();
    }

    @Override
    public void onNewWordFound() {
        Log.d(TAG, results.getPreviousRecognizerOutput());

        // Check last 2 words for match with the master keywords:
        String lastTwoWords = this.results.getLastNWordsAsString(2);
        if (AbstractInterpreter.shouldSwitchToEditMode(lastTwoWords)) {
            switchToEditing();
            results.ignoreLastNumberOfWords(2);
        }

        String keywordCandidate = this.results.getLastWord().getWordString();
        updateDictationOutputTextView(this.results);
        InterpreterSelector appropriateIntSel = interpreterSelector.queryAppropriateInterpreterSelector(
                keywordCandidate,
                currentInterpreter);
        if (appropriateIntSel != interpreterSelector) {
            changeInterpreter(appropriateIntSel);
        }
    }

    @Override
    public SpeakerFacade getSpeaker() {
        return speaker;
    }

    private Runnable pollSoundLevelTask = new Runnable() {
        @Override
        public void run() {
            if (isListening == false && isSpeaking == false && soundMeter.isOverThreshold()) {
                Log.d(TAG, "listener fired by sound!");
                listenForVoiceInBackground();
            }

            soundLevelHandler.postDelayed(pollSoundLevelTask, SOUND_METER_SAMPLING_DELAY);
        }
    };

    private void onListeningStarted() {
        isListening = true;
        updateIsListeningIndicatorTV(isListening);
        soundMeter.stop();
        indicatorHum.play();
        Log.d(TAG, "onListeningStarted() invoked");
    }

    private void onListeningStopped() {
        isListening = false;
        updateIsListeningIndicatorTV(isListening);
        soundMeter.start();
        indicatorHum.pause();
    }

}
