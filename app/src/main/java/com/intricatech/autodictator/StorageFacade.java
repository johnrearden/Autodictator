package com.intricatech.autodictator;

import android.content.Context;

/**
 * Created by Bolgbolg on 18/02/2018. A singleton class that provides an interface for
 * handling persistant storage.
 */

public class StorageFacade implements StorageInterface {

    private static StorageFacade instance;

    public static void initialize(Context context) {
        instance = new StorageFacade(context);
    }

    public static StorageFacade getInstance() {
        return instance;
    }

    private StorageFacade(Context context) {

    }

    @Override
    public int storeSentence(Sentence sentence) {

        // First, check if a sentence with matching ID exists in storage. If not, then add this
        // sentence and return its newly generated ID.

        return 0;
    }

    @Override
    public Document retrieveEntireDocument(int documentID) {
        return new Document();
    }
}
