package com.work.catch_camera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "camera.db", factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createStr ="CREATE TABLE cameraLog " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "db_image BLOB, " +
                "db_location TEXT, " +
                "db_position TEXT, " +
                "db_date TEXT)";


        db.execSQL(createStr);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cameraLog");
        onCreate(db);
    }

    public void insert(byte[] image, String location, String position, String date)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("db_image", image);
        cv.put("db_location", "홍익대학교");
        cv.put("db_position", position);
        cv.put("db_date", date);
        database.insert("cameraLog",null,cv);
    }

    public ArrayList<MainData> getAllContents(){
        ArrayList<MainData> allList = new ArrayList<>();

        String selectQuery = "SELECT * FROM cameraLog";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery,null);

        if(cursor.moveToLast())
        {
            do {
                MainData contact = new MainData();
                contact.setImage(cursor.getBlob(1));
                contact.setLocation(cursor.getString(2)); ;
                contact.setPosition(cursor.getString(3));
                contact.setDate(cursor.getString(4));
                allList.add(contact);
            } while(cursor.moveToPrevious());
        }

        return allList;
    }
}
