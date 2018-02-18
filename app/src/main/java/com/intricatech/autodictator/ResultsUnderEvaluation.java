package com.intricatech.autodictator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 08/02/2018.
 */

public class ResultsUnderEvaluation {

    private String cumulativeCurrentResults;
    private List<Word> currentResultsWordList;

    public ResultsUnderEvaluation() {
        currentResultsWordList = new LinkedList<>();
    }

    public String getCumulativeCurrentResults() {
        return cumulativeCurrentResults;
    }

    public void overwriteCurrentResults(String cumulativeCurrentResults) {
        this.cumulativeCurrentResults = cumulativeCurrentResults;
    }

    public List<Word> getCurrentResultsWordList() {
        return currentResultsWordList;
    }

    public Word getLastWord() {
        return currentResultsWordList.get(currentResultsWordList.size() - 1);
    }

    public String getLastTwoWordsAsString() {
        int size = currentResultsWordList.size();
        if (size < 2) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(currentResultsWordList.get(size - 2).getWordString());
            sb.append(" ");
            sb.append(currentResultsWordList.get(size - 1).getWordString());
            return sb.toString();
        }
    }

    public void disregardLastTwoWords() {
        int size = currentResultsWordList.size();
        currentResultsWordList.get(size - 2).declareKeyword(true);
        currentResultsWordList.get(size - 1).declareKeyword(true);

    }
}
