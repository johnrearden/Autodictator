package com.intricatech.autodictator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intricatech.autodictator.DBContract.DocumentTable;
import static com.intricatech.autodictator.DBContract.SentenceTable;

/**
 * Created by Bolgbolg on 18/02/2018. A singleton class that provides an interface for
 * handling persistent storage.
 */

public class StorageFacade implements Storable {

    private DBHelper helper;
    private SQLiteDatabase database;
    private static String TAG;

    private static StorageFacade instance;
    public static void initialize(Context context) {
        instance = new StorageFacade(context);
    }
    public static StorageFacade getInstance() {
        return instance;
    }

    private StorageFacade(Context context) {
        TAG = getClass().getSimpleName();
        helper = new DBHelper(context);
        database = helper.getWritableDatabase();
        Log.d(TAG, "private constructor invoked, database == null : " + String.valueOf(database == null));
    }

    @Override
    public long storeSentence(Sentence sentence, long documentID) {
        // First, check if the sentence is already saved in the database.
        long storageId = sentence.getStorageID();
        String[] cols = new String[]{SentenceTable._ID};
        String selection = SentenceTable._ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(storageId)};
        Cursor cursor = database.query(
                SentenceTable.getTableName(documentID),
                cols,
                selection,
                selectionArgs,
                null, null, null);
        if (cursor.moveToFirst() == true) { // Sentence is already in database.
            return storageId;
        } else {
            ContentValues values = new ContentValues();
            values.put(SentenceTable.WORD_STRING, sentence.getSentenceAsString());
            values.put(SentenceTable.PARAGRAPH_INDEX, sentence.getParagraphIndex());
            values.put(SentenceTable.POSITION_IN_PARAGRAPH, sentence.getIndexOfAppearance());

            long id = database.replace(SentenceTable.getTableName(documentID), null, values);

            return id;
        }

    }

    @Override
    public void updateSentence(Sentence sentence, long documentID) {

    }

    @Override
    public Document retrieveEntireDocument(long documentID) {
        Document doc = new Document(documentID);
        String[] cols = new String[]{
                SentenceTable._ID, SentenceTable.WORD_STRING, SentenceTable.PARAGRAPH_INDEX, SentenceTable.POSITION_IN_PARAGRAPH};
        String paragraphOrder = SentenceTable.PARAGRAPH_INDEX + " ASC";
        Cursor cursor = database.query(
                SentenceTable.getTableName(documentID),
                cols,
                null, null, null, null,
                paragraphOrder,
                null);

        Map<Integer, Paragraph> paragraphMap = new HashMap<>();
        while(cursor.moveToNext()) {

            // Get relevant results from the cursor.
            int posInPara = cursor.getInt(cursor.getColumnIndexOrThrow(SentenceTable.POSITION_IN_PARAGRAPH));
            int paragraphIndex = cursor.getInt(cursor.getColumnIndexOrThrow(SentenceTable.PARAGRAPH_INDEX));
            long storageID = cursor.getLong(cursor.getColumnIndexOrThrow(SentenceTable._ID));
            String wordString = cursor.getString(cursor.getColumnIndexOrThrow(SentenceTable.WORD_STRING));
            List<Word> wordList = Document.getWordStringAsList(wordString);

            // Create a new sentence, and check to see if its paragraphIndex corresponds to a paragraph
            // in the paragraphMap. If not, also create a new Paragraph with this index and add it
            // to the map. Add the sentence to its parent paragraph.
            Sentence s = new Sentence(posInPara);
            s.setStorageID(storageID);
            for (Word word : wordList) {
                s.addWord(word);
            }
            if (!paragraphMap.containsKey(paragraphIndex)) {
                paragraphMap.put(paragraphIndex, new Paragraph(paragraphIndex));
            }
            paragraphMap.get(paragraphIndex).addSentence(s);
        }
        // Add all entries in the paragraphMap to the Document.
        for (Integer i : paragraphMap.keySet()) {
            doc.addParagraph(paragraphMap.get(i));
        }
        cursor.close();
        return doc;
    }

    @Override
    public Sentence retrieveSentence(long sentenceID, long documentID) {
        return null;
    }

    @Override
    public long createNewDocument(String docName) {
        // Add the document to the document_list table.
        ContentValues values = new ContentValues();
        values.put(DocumentTable.NAME, docName);
        values.put(DocumentTable.TOTAL_SENTENCES, 0);
        values.put(DocumentTable.LAST_SENTENCE_ID, 0);
        long docID = database.insert(DocumentTable.TABLE_NAME, null, values);

        // Create a new sentence_table for the document.
        database.execSQL(SentenceTable.getCreateTableString(docID));

        return docID;
    }

    @Override
    public void deleteDocument(long docID) {
        // First, delete the SentenceTable for this document.
        database.execSQL(SentenceTable.getDropTableString(docID));

        // Second, delete the corresponding entry in the document_list table.
        String where = DocumentTable._ID + " =?";
        String[] whereArgs = new String[]{String.valueOf(docID)};
        database.delete(DocumentTable.TABLE_NAME, where, whereArgs);
    }

    @Override
    public void storeEntireDocument(Document document) {
        for (Paragraph paragraph : document.getParagraphList()) {
            for (Sentence sentence : paragraph.getSentenceList()) {
                storeSentence(sentence, document.getDocumentID());
            }
        };
    }

    @Override
    public boolean doAnyDocumentsExist() {
        boolean someDocExists;
        String query = "SELECT COUNT(*) FROM " + DocumentTable.TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.getInt(0) == 0) {
            someDocExists = false;
        } else someDocExists = true;
        cursor.close();
        return someDocExists;
    }

    public void logAllDatabaseTables() {
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        Log.d(TAG, "Existing tables :");
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                Log.d(TAG, "... table : " + c.getString(0));
                c.moveToNext();
            }
        }
    }
}
