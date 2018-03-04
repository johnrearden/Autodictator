package com.intricatech.autodictator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.intricatech.autodictator.DBContract.DATABASE_NAME;
import static com.intricatech.autodictator.DBContract.DATABASE_VERSION;
import static com.intricatech.autodictator.DBContract.DocumentTable;

/**
 * Created by Bolgbolg on 21/02/2018.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = "DBHelper";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "constructor invoked");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DocumentTable.CREATE_TABLE);
        Log.d(TAG, DocumentTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
