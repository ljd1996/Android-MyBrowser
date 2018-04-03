package com.example.admin.mybrowser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by admin on 2017/3/2.
 */

public class MySQLiteopenHelper extends SQLiteOpenHelper {

    public String HISTORY_TABLE_NAME="history_table";
    public String COLLECT_TABLE_NAME="collect_table";
    public String DOWNLOAD_TABLE_NAME ="download_table";

    public MySQLiteopenHelper(Context context, String name,  int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+HISTORY_TABLE_NAME+"(_id integer auto_increment primary key,name ,url )");
        db.execSQL("create table "+COLLECT_TABLE_NAME+"(_id integer auto_increment primary key,name ,url )");
        db.execSQL("create table "+ DOWNLOAD_TABLE_NAME +"(_id integer auto_increment primary key,downloadID )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
