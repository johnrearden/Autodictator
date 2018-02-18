package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public interface InterpreterState {

    /**
     * Concrete interpreters implement this method.
     * @param resultsUnderEvaluation
     * @param resultsFromRecognizer
     * @return TRUE if the resultsUnderEvaluation has been changed, FALSE otherwise.
     */
    public boolean interpret(
            Document document,
            ResultsUnderEvaluation resultsUnderEvaluation,
            String resultsFromRecognizer,
            MainActivity.MasterState masterState,
            boolean isSpeaking);
}
