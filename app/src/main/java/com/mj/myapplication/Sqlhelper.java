package com.mj.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class Sqlhelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;  //数据库版本号

    public Sqlhelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table valueTable ("+"id integer primary key autoincrement,"+"tempValue float,"+"humidityValue float,"+"lightValue float,"+"smokeConcValue float,"+"time text)");
        db.execSQL("create table userTable ("+"id integer primary key autoincrement,"+"name text,"+"pwd text,"+"userType text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
