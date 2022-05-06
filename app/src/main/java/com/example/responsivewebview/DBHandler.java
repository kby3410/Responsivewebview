package com.example.responsivewebview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler {
    SQLiteOpenHelper mHelper = null;
    SQLiteDatabase mDB = null;

    public DBHandler(Context context) {
        mHelper = new DBHelper(context);
    }

    public static DBHandler open(Context context) {
        return new DBHandler(context);
    }

    public Cursor select() {
        mDB = mHelper.getReadableDatabase();
        String sql_query = "SELECT * FROM LauncherDB";
        Cursor c = mDB.rawQuery(sql_query, null);
        //c.moveToFirst();
        return c;
    }

    public void insert(String http, String name, Integer check_number){
        mDB = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("http",http);
        values.put("name",name);
        values.put("check_number",check_number);
        mDB.insertWithOnConflict("LauncherDB",null,values,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void update(String http, String name, Integer check_number){
        mDB = mHelper.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put("http",http);
        //values.put("name",name);
        values.put("check_number",check_number);
        mDB.update("LauncherDB",values,"name=?", new String[]{name});
       // mDB.update("LauncherDB",values,"http = ?",new String[]{http});
        //return mDB.update("LauncherDB",values,"name=?",new String[]{name}) > 0;
        //mDB.update("LauncherDB",values,"check_number = ?",new String[]{String.valueOf(check_number)});
    }

    public void delete(String name){
        mDB = mHelper.getWritableDatabase();
        mDB.delete("LauncherDB", "name = ?", new String[]{name});
    }
}