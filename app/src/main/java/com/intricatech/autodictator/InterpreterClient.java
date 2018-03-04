package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 20/02/2018.
 */

public interface InterpreterClient {

    public Document getDocument();

    public void onFinishedInterpreting();

    public void onNewWordFound();

    public SpeakerFacade getSpeaker();

    public MasterState getMasterState();

}
