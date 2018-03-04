package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 21/02/2018.
 */

public class DBContract {

    private DBContract(){}

    public static final String DATABASE_NAME = "autodictator_database";
    public static final int DATABASE_VERSION = 1;

    /**
     * This static class provides hard-coded string queries to create and delete the document_list
     * table. Non-instantiable.
     */
    public static class DocumentTable {

        // Table name:
        public static final String TABLE_NAME = "document_list";

        // Column names:
        public static final String _ID = "_id";
        public static final String NAME = "name";
        public static final String TOTAL_SENTENCES = "total_sentences";
        public static final String LAST_SENTENCE_ID = "last_sentence_id";

        // Table creator.
        public static final String CREATE_TABLE = "CREATE TABLE "
                + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + NAME + " STRING, "
                + TOTAL_SENTENCES + " INTEGER, "
                + LAST_SENTENCE_ID + " INTEGER"
                + ")";

        // Table destroyer.
        public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * This static class provides hard-coded string queries to create and delete the sentence_list
     * table. Non-instantiable.
     */
    public static class SentenceTable {

        // Table name:
        public static final String TABLE_NAME_PREFIX = "document_";

        // Column names:
        public static final String _ID = "_id";
        public static final String WORD_STRING = "word_string";
        public static final String PARAGRAPH_INDEX = "paragraph_index";
        public static final String POSITION_IN_PARAGRAPH = "position_in_paragraph";

        // Table creator.
        public static final String getCreateTableString(long docID) {
            String CREATE_TABLE = "CREATE TABLE "
                    + getTableName(docID) + " ("
                    + _ID + " INTEGER PRIMARY KEY, "
                    + WORD_STRING + " STRING, "
                    + PARAGRAPH_INDEX + " INTEGER, "
                    + POSITION_IN_PARAGRAPH + " INTEGER"
                    + ")";
            return CREATE_TABLE;
        }

        // Table destroyer.
        public static final String getDropTableString(long docID) {
            return "DROP TABLE IF EXISTS " + getTableName(docID);
        }

        // Table name provider.
        public static final String getTableName(long docID) {
            return TABLE_NAME_PREFIX + String.valueOf(docID);
        }
    }
}
