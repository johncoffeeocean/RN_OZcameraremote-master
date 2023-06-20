package com.oz.ozcameraremote;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tutlane on 06-01-2018.
 */

public class DbHandler extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "urls.db";
    private static final String TABLE_NAME = "tbl_ulrs";
    private static final String KEY_ID = "_id";
    private static final String KEY_URL = "url";
    private static final String KEY_NAME = "name";
    public DbHandler(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_URL + " TEXT"+ ")";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public void insertUrl(String name, String url){
        if (name.isEmpty()) return;
        if (url.isEmpty()) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_NAME, name);
        cValues.put(KEY_URL, url);
        long newRowId = db.insert(TABLE_NAME,null, cValues);
        db.close();
    }
    // Get User Details
    @SuppressLint("Range")
    public Cursor GetUrls(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> urlList = new ArrayList<>();
        String query = "SELECT * FROM "+ TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);
        return cursor;
//        while (cursor.moveToNext()){
//            HashMap<String,String> user = new HashMap<>();
//            user.put("id",cursor.getString(cursor.getColumnIndex(KEY_ID)));
//            user.put("url",cursor.getString(cursor.getColumnIndex(KEY_URL)));
//            urlList.add(user);
//        }
//        return  urlList;
    }
    // Get User Details based on userid
    @SuppressLint("Range")
    public Cursor GetUrlById(long userid){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        String query = "SELECT * FROM "+ TABLE_NAME;
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_NAME, KEY_URL}, KEY_ID+ "=?",new String[]{String.valueOf(userid)},null, null, null, null);
//        if (cursor.moveToNext()){
//            HashMap<String,String> user = new HashMap<>();
//            user.put("name",cursor.getString(cursor.getColumnIndex(KEY_URL)));
//            userList.add(user);
//        }
        return  cursor;
    }
    // Delete User Details
    public void DeleteUrl(long userid){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID+" = ?",new String[]{String.valueOf(userid)});
        db.close();
    }
    // Update User Details
    public int UpdateUrl(String name, String url, long id){
        if (name.isEmpty()) return 0;
        if (url.isEmpty()) return 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cVals = new ContentValues();
        cVals.put(KEY_URL, url);
        cVals.put(KEY_NAME, name);
        int count = db.update(TABLE_NAME, cVals, KEY_ID+" = ?", new String[]{String.valueOf(id)});
        return  count;
    }
}