package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 11/02/2018.
 *
 * This class, once it has been selected as the client's currentInterpreter, parses the
 * subsequent output from the SpeechRecognizer and, if it detects a valid sequence of keywords,
 * creates a selection and passes it to the client's SpeakerFacade.
 */

public class ReadInterpreter extends AbstractInterpreter {

    public static String TAG;
    public static InterpreterSelector SELECTOR = InterpreterSelector.READ_INTERPRETER;
    public static String SELECTOR_KEYWORD = SELECTOR.toString();
    private String LAST_SENTENCE = "LAST SENTENCE";
    private String LAST_PARAGRAPH = "LAST PARAGRAPH";

    public static final ReadInterpreter instance = new ReadInterpreter();
    public static ReadInterpreter getInstance() {
        return instance;
    }

    private ReadInterpreter() {
        TAG = getClass().getSimpleName();
        keywordResults = new KeywordResults();
    }
    private KeywordResults keywordResults;
    private InterpreterClient client;

    public void configure(InterpreterClient client) {
        this.client = client;
    }

    /**
     * First prunes the results from the SpeechRecognizer, discarding everything up to and
     * including the selector keyword. Then if a new keyword has been found, it updates the
     * KeywordResults. Finally, if it detects a keyword or sequence thereof that represents a valid
     * instruction, it creates a selection based on the instruction and sends a String representation
     * of that selection to the clients SpeakerFacade.
     * @param resultsFromRecognizer Results returned from the client's SpeakerFacade.
     * @param results Keyword results object (subclass of Results)
     */
    @Override
    public void interpret(String resultsFromRecognizer, Results results) {
        String prunedResultsFromRecognizer = keywordResults.splitStringOnKeyword(
                resultsFromRecognizer, SELECTOR_KEYWORD);
        if (keywordResults.isNewResult(prunedResultsFromRecognizer, SELECTOR)) {
            keywordResults.updateCurrentResultsWordList(
                    prunedResultsFromRecognizer, SELECTOR
            );
        }
        String lastWord = keywordResults.getLastWord().getWordString();
        String lastTwoWords = keywordResults.getLastNWordsAsString(2);

        if (lastWord.toUpperCase().equals("EVERYTHING")) {
            //Log.d(TAG, "'EVERYTHING' heard .... reading");
            //Log.d(TAG, resultsFromRecognizer);
            client.getSpeaker().addSpeechToQueue(client.getDocument().returnEntireDocumentAsString());
            client.onFinishedInterpreting();
        }

        if (lastTwoWords.toUpperCase().equals(LAST_SENTENCE)) {
            Sentence s = client.getDocument().getCurrentParagraph().getCurrentSentence();
            Selection currentSelection = new Selection();
            currentSelection.addSentence(s);
            client.getSpeaker().addSpeechToQueue(currentSelection.toString());
        }

        if (lastTwoWords.toUpperCase().equals(LAST_PARAGRAPH)) {
            Paragraph p = client.getDocument().getCurrentParagraph();
            Selection selection = new Selection();
            selection.addParagraph(p);
            client.getSpeaker().addSpeechToQueue(selection.toString());
        }
    }
}
