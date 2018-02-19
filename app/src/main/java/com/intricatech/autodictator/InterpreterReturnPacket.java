package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 19/02/2018.
 */

public class InterpreterReturnPacket {

    public final boolean resultsUnderEvaluationChanged;
    public final boolean finishedInterpreting;

    public InterpreterReturnPacket(boolean resultsChanged, boolean finishedInterpreting) {
        this.resultsUnderEvaluationChanged = resultsChanged;
        this.finishedInterpreting = finishedInterpreting;
    }
}
