package com.example.qrcodescanner.SimpleClasses;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DBHelper extends SQLiteAssetHelper {

    public DBHelper(Context context) {
        super(context, "bitmapDB.db", null, 17);
        setForcedUpgrade();
    }
}
