package com.adnagu.ytuyemek;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    SQLiteDatabase db;

    public Database(Context context) {
        super(context, "YTUYemek", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE meal (id INTEGER PRIMARY KEY, lunch TEXT, lunch_meal TEXT, dinner TEXT, dinner_meal TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS meal");
        onCreate(db);
    }

    public void resetTables() {
        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS meal");
        onCreate(db);
    }

    public void insertMeal(int id, String lunch, String dinner) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);

        if(null != lunch) {
            int index = lunch.indexOf("<br/>") + 5;
            values.put("lunch_meal", lunch.substring(index, lunch.indexOf("<br/>", index)).toLowerCase().trim());

            if(null != dinner) {
                index = dinner.indexOf("<br/>") + 5;
                //Log.d("ADN","Dinner index:"+index+" meal:"+dinner.substring(index, dinner.indexOf("<br/>", index)).toLowerCase());
                values.put("dinner_meal", dinner.substring(index, dinner.indexOf("<br/>", index)).toLowerCase().trim());
            } else values.put("dinner_meal",(String) null);
        } else values.put("lunch_meal",(String) null);

        values.put("lunch", lunch);
        values.put("dinner", dinner);
        db.insert("meal", null, values);
    }

    public ArrayList<Meal> getItems() {
        ArrayList<Meal> meals = new ArrayList<Meal>();

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id,lunch,dinner FROM meal", null);

        if (cursor.moveToFirst()) {
            do {
                meals.add(new Meal(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }

        return meals;
    }

    public Cursor getLunchMeal(int id) {
        db = this.getReadableDatabase();
        return db.rawQuery("SELECT lunch_meal FROM meal WHERE meal.id = "+id, null);
    }

    public Cursor getDinnerMeal(int id) {
        db = this.getReadableDatabase();
        return db.rawQuery("SELECT dinner_meal FROM meal WHERE meal.id = "+id, null);
    }

    public int getMealCount() {
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM meal", null);
        return cursor.moveToFirst() ? cursor.getInt(0) : 0;
    }
}