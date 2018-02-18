package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 18/02/2018.
 */

public interface StorageInterface {

    /**
     * Stores a sentence and returns a unique sentence ID. This method is allowed to overwrite
     * a sentence in the store with the same ID as the sentence provided as parameter, without
     * warning.
     * @param sentence The sentence to be stored.
     * @return A unique integer ID value.
     */
    public int storeSentence(Sentence sentence);

    public Document retrieveEntireDocument(int documentID);
}
