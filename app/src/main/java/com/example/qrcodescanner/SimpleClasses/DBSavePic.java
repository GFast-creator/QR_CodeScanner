package com.example.qrcodescanner.SimpleClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.TreeSet;

public class DBSavePic {

    public static long savePicToDB(Context context, Bitmap bitmap,
                                   boolean generated, String text,
                                   int format, int textFormat, String json){
        SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
        db.beginTransaction();
        long newId;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ContentValues cv = new ContentValues();

            cv.put("bitmap", byteArray);
            cv.put("text", text);
            cv.put("format",format);
            cv.put("textFormat",textFormat);
            cv.put("extra",json);
            String s = generated?"generatedPic":"pictures";
            newId = db.insert(s,null,cv);
            Log.e("dbSaveId", String.valueOf(newId));

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return newId;
    }

    public static void idUpdate(Context context, String tableName){
        SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName,null);

        int i = 1;
        if (cursor.moveToFirst()) {
            do {
                ContentValues cv = new ContentValues();
                cv.put("id", i);
                db.update(tableName, cv,"id = " + cursor.getInt(cursor.getColumnIndex("id")), null);
                i++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public static void deleteItems (Context context, String tableName, TreeSet<Integer> itemsId){
        SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
        /*for (int id:itemsId) {
            db.delete(tableName,"id = " + (id + 1),null);
        }*/
        //int[] list = new int[itemsId.size()];
        //int g = 0;
        StringBuilder s = new StringBuilder();
        for (int i : itemsId){
            //list[g] = i + 1;
            s.append(i + 1).append(", ");
            //g++;
        }
        s.delete(s.length()-2,s.length());
        String sql = "delete from " + tableName + " where id in (" + s + ")";
        db.execSQL(sql);
        //db.delete(tableName, "id = ?", s.toString());
        db.close();
    }

    public static void deleteLastItem (Context context, String tableName){
        try {

            SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);
            long l = c.getCount();
            db.delete(tableName, "id = " + l, null);
            c.close();
            db.close();
        } catch (Exception e){

        }
    }

}
