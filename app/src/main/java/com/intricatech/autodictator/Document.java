package com.intricatech.autodictator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Document {

    public static final char[] SENTENCE_TERMINATORS = new char[]{'.', '!', '?'};

    public static final char[] PUNCTUATION_MARKS_WITHIN_SENTENCES = new char[]{',', ':', ';'};

    private List<Paragraph> paragraphList;

    private Sentence currentSentence;
    private Paragraph currentParagraph;
    private int currentSentenceIndex;
    private int currentParagraphIndex;

    public Document() {
        paragraphList = new LinkedList<>();
        currentParagraphIndex = 0;
        currentSentenceIndex = 0;
        currentSentence = null;
        currentParagraph = null;
    }

    public void addParagraph(Paragraph paragraph) {
        paragraphList.add(paragraph);
    }

    public void commitResults(ResultsUnderEvaluation results, StorageInterface storage) {

        // Separate out the punctuation marks from the words themselves.
        List<Word> punctuatedList = parsePunctuation(results.getCurrentResultsWordList());

        // Remove ignored words (heard while TextSpeaker is speaking.
        for (Word word : punctuatedList) {
            if (word.getType() == WordType.IGNORED) {
                punctuatedList.remove(word);
            }
        }

        if (currentParagraph == null) {
            currentParagraph = new Paragraph();
            addParagraph(currentParagraph);
        }
        if (currentSentence == null) {
            currentSentence = new Sentence();
        }

        // Add the punctuatedList to the Document word by word. Each time a sentence-ending
        // punctuation mark is encountered, the sentence is committed to storage and a new sentence
        // is started.
        for (Word word : punctuatedList) {
            currentSentence.addWord(word);
            if (word.getType() == WordType.PUNC_END_SENTENCE) {
                currentParagraph.addSentence(currentSentence);
                storage.storeSentence(currentSentence);
                currentSentence = new Sentence();
                currentSentenceIndex++;
            }
        }

        // If the current sentence is not complete, store it.
        WordType typeOfLastWord = punctuatedList.get(punctuatedList.size() - 1).getType();
        if (typeOfLastWord != WordType.PUNC_END_SENTENCE) {
            storage.storeSentence(currentSentence);
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

    public void startNewParagraph() {

    }

    public static List<Word> parsePunctuation(List<Word> suppliedList) {
        List<Word> tempList = new LinkedList();
        for (Word suppliedWord : suppliedList) {

            String s = suppliedWord.getWordString();
            int l = s.length();
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

}
