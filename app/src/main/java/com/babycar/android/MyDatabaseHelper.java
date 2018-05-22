package com.babycar.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    //todo:建表操作
    public static final String CREATE_INFORMATION = "create table information ("
            + "id integer primary key autoincrement,"
            + "degree integer)";

    private Context mContext;
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }

    //创建数据库
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_INFORMATION);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    //todo：升级数据库
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
