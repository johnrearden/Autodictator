package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 18/02/2018.
 */

public interface Storable {

    /**
     * Stores a sentence and returns a unique sentence ID. This method is allowed to overwrite
     * a sentence in the store with the same ID as the sentence provided as parameter, without
     * warning.
     * @param sentence The sentence to be stored.
     * @return A unique integer ID value.
     */

    /**
     * Automatically overwrites any existing sentence with the same id.
     * @param sentence
     * @return
     */
    public long storeSentence(Sentence sentence, long documentID);

    public void updateSentence(Sentence sentence, long documentID);

    public Sentence retrieveSentence(long sentenceID, long documentID);

    public long createNewDocument(String docName);

    public void deleteDocument(long docID);

    public void storeEntireDocument(Document document);

    public Document retrieveEntireDocument(long documentID);

    public boolean doAnyDocumentsExist();
}
