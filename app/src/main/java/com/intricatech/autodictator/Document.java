package com.intricatech.autodictator;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Document {

    public static String TAG;
    public static final char[] SENTENCE_TERMINATORS = new char[]{'.', '!', '?'};

    public static final char[] PUNCTUATION_MARKS_WITHIN_SENTENCES = new char[]{',', ':', ';'};

    private List<Paragraph> paragraphList;
    private long documentID;

    private Sentence currentSentence;
    private Paragraph currentParagraph;
    private int currentParagraphIndex;

    public Document(long docID) {
        TAG = getClass().getSimpleName();
        this.documentID = docID;
        paragraphList = new LinkedList<>();
        currentParagraphIndex = 0;
        currentSentence = null;
        currentParagraph = null;
    }

    public void addParagraph(Paragraph paragraph) {
        paragraphList.add(paragraph);
    }

    public void commitResults(Results results, Storable storage) {

        // Separate out the punctuation marks from the words themselves, and clear the source
        // array to get ready for the next set of results
        List<Word> punctuatedList = parsePunctuation(results.getWordList());
        results.clearWordList();

        // Remove ignored words (heard while TextSpeaker is speaking), and keywords.
        ListIterator<Word> iter = punctuatedList.listIterator();
        while(iter.hasNext()) {
            Word word = iter.next();
            if (word.getType() == WordType.IGNORED || word.getType() == WordType.KEYWORD) {
                iter.remove();
            }
        }
        Log.d(TAG, "post-removal list : " + Results.getWordListAsString(punctuatedList));

        if (currentParagraph == null) {
            currentParagraph = new Paragraph(paragraphList.size());
            addParagraph(currentParagraph);
        }
        if (currentSentence == null) {
            currentSentence = new Sentence(currentParagraph.getIndex());
            currentSentence.setIndexOfAppearance(0);
        }

        // Add the punctuatedList to the Document word by word. Each time a sentence-ending
        // punctuation mark is encountered, the sentence is committed to storage and a new sentence
        // is started.
        for (Word word : punctuatedList) {
            currentSentence.addWord(word);
            if (word.getType() == WordType.PUNC_END_SENTENCE) {
                currentParagraph.addSentence(currentSentence);
                storeSentence(storage, currentSentence, documentID);
                int currentIndex = currentSentence.getIndexOfAppearance();
                currentSentence = new Sentence(currentParagraph.getIndex());
                currentIndex++;
                currentSentence.setIndexOfAppearance(currentIndex);
            }
        }

        // If the current sentence is not complete, store it.
        if (punctuatedList.size() > 0) {
            WordType typeOfLastWord = punctuatedList.get(punctuatedList.size() - 1).getType();
            if (typeOfLastWord != WordType.PUNC_END_SENTENCE) {
                storeSentence(storage, currentSentence, documentID);
            }
        }
    }



    public String returnEntireDocumentAsString() {
        StringBuilder sb = new StringBuilder();
        for (Paragraph p : paragraphList) {
            for (Sentence sentence : p.getSentenceList()) {
                sb.append(getSentenceAsString(sentence));
            }
        }

        return sb.toString();
    }

    public static List<Word> parsePunctuation(List<Word> suppliedList) {

        List<Word> tempList = new LinkedList();
        for (Word suppliedWord : suppliedList) {

            String s = suppliedWord.getWordString();
            //Log.d(TAG, "word == " + s);

            int l = s.length();
            if (l == 0) {
                Log.d(TAG, "Word found with length of 0. WTF");
                continue;
            }
            char lastChar = s.charAt(l - 1);
            boolean lastCharIsPunctuationWithinSentence = charIsPunctuationWithinSentence(lastChar);
            boolean lastCharIsSentenceEnder = characterIsSentenceEnder(lastChar);

            // Check that the word does not have a punctuation mark as its last character.
            if (!lastCharIsPunctuationWithinSentence && !lastCharIsSentenceEnder) {
                tempList.add(suppliedWord);
            } else {
                WordType punctuationWordType = lastCharIsSentenceEnder ?
                        WordType.PUNC_END_SENTENCE : WordType.PUNC_WITHIN_SENTENCE;
                String wordOnly = s.substring(0, l - 1);
                String punctuationMark = s.substring(l - 1);
                Word w1 = new Word(wordOnly, WordType.NORMAL);
                Word w2 = new Word(punctuationMark, punctuationWordType);
                tempList.add(w1);
                tempList.add(w2);
            }
        }
        return tempList;
    }

    public static boolean characterIsSentenceEnder(char character) {
        for (char c : SENTENCE_TERMINATORS) {
            if (character == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean charIsPunctuationWithinSentence(char character) {
        for (char c : PUNCTUATION_MARKS_WITHIN_SENTENCES) {
            if (character == c) {
                return true;
            }
        }
        return false;
    }

    public static String getSentenceAsString(Sentence sentence) {

        if (sentence.getWordList().size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        List<Word> wordList = sentence.getWordList();

        // Capitalize and add first word.
        String firstWord = wordList.get(0).getWordString();
        String firstLetter = firstWord.substring(0, 1).toUpperCase();
        String restOfWord = firstWord.substring(1);
        sb.append(firstLetter + restOfWord);

        // Add any further words.
        if (wordList.size() > 1) {
            for (int i = 1; i < wordList.size(); i++) {
                Word w = wordList.get(i);
                WordType type = w.getType();
                if (type == WordType.PUNC_END_SENTENCE || type == WordType.PUNC_WITHIN_SENTENCE) {
                    sb.append(w.getWordString());
                } else {
                    sb.append(" " + w.getWordString());
                }
            }
        }

        // Append a trailing space.
        sb.append(" ");

        return sb.toString();
    }

    public static List<Word> getWordStringAsList(String wordString) {
        List<Word> wordList = new LinkedList<>();
        String[] splitterArray = wordString.split(" ");
        for (String s : splitterArray) {
            wordList.add(new Word(s, WordType.NORMAL));
        }

        // Parse the wordList for punctuation marks, treating these as words in their own right.
        wordList = parsePunctuation(wordList);

        return wordList;

    }

    public List<Paragraph> getParagraphList() {
        return paragraphList;
    }

    public long getDocumentID() {
        return documentID;
    }

    /**
     * Utility method which combines storing the sentence and afterwards setting the
     * sentence's storageID based on the return value of the storeSentence() call.
     * @param storage
     * @param sentence
     * @param documentID
     */
    private void storeSentence(Storable storage, Sentence sentence, long documentID) {
        long storageId = storage.storeSentence(sentence, documentID);
        sentence.setStorageID(storageId);
    }

    public Paragraph getCurrentParagraph() {
        return paragraphList.get(paragraphList.size() - 1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ID : " + documentID + "\n");
        for (Paragraph p : paragraphList) {
            sb.append(p.toString() + "\n");
        }
        return sb.toString() + "\n";
    }
}
