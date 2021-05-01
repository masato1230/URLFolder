package com.jp_funda.urlfolder.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.lang.UCharacter;

import androidx.annotation.Nullable;

import com.jp_funda.urlfolder.Models.Folder;
import com.jp_funda.urlfolder.Models.Url;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UrlDatabaseHandler extends SQLiteOpenHelper {
    private Context context;

    public UrlDatabaseHandler(@Nullable Context context) {
        super(context, UrlConstants.DB_NAME, null, UrlConstants.DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_URL_TABLE = "CREATE TABLE " + UrlConstants.TABLE_NAME + "("
                + UrlConstants.KEY_ID + " INTEGER PRIMARY KEY,"
                + UrlConstants.KEY_TITLE + " TEXT,"
                + UrlConstants.KEY_URL + " TEXT,"
                + UrlConstants.KEY_MEMO + " TEXT,"
                + UrlConstants.KEY_ADDED_DATE + " TEXT,"
                + UrlConstants.KEY_BROWSING_DATE + " TEXT,"
                + UrlConstants.KEY_FOLDER_ID + " INTEER,"
                + UrlConstants.KEY_BROWSER_ID + " INTEGER"
                + ");";
        db.execSQL(CREATE_URL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UrlConstants.TABLE_NAME);
        onCreate(db);
    }

    // add
    public long addUrl(Url url) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UrlConstants.KEY_TITLE, url.getTitle());
        values.put(UrlConstants.KEY_URL, url.getUrl());
        values.put(UrlConstants.KEY_MEMO, url.getMemo());
        values.put(UrlConstants.KEY_ADDED_DATE, UrlConstants.dateFormat.format(url.getAddedDate()));
        values.put(UrlConstants.KEY_BROWSING_DATE, UrlConstants.dateFormat.format(url.getBrowsingDate()));
        values.put(UrlConstants.KEY_FOLDER_ID, url.getFolderId());
        values.put(UrlConstants.KEY_BROWSER_ID, url.getBrowserId());

        return db.insert(UrlConstants.TABLE_NAME, null, values);
    }

    // get one
    public Url getOneUrl(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Url url = new Url();

        Cursor cursor = db.query(
                UrlConstants.TABLE_NAME,
                new String[] {
                        UrlConstants.KEY_ID,
                        UrlConstants.KEY_TITLE,
                        UrlConstants.KEY_URL,
                        UrlConstants.KEY_MEMO,
                        UrlConstants.KEY_ADDED_DATE,
                        UrlConstants.KEY_BROWSING_DATE,
                        UrlConstants.KEY_FOLDER_ID,
                        UrlConstants.KEY_BROWSER_ID
                },
                UrlConstants.KEY_ID + "=?",
                new String[] {String.valueOf(id)},
                null, null, null
                );

        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();

        url.setId(cursor.getInt(cursor.getColumnIndex(UrlConstants.KEY_ID)));
        url.setTitle(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_TITLE)));
        url.setUrl(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_URL)));
        url.setMemo(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_MEMO)));
        try {
            url.setAddedDate(UrlConstants.dateFormat.parse(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_ADDED_DATE))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            url.setBrowsingDate(UrlConstants.dateFormat.parse(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_BROWSING_DATE))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        url.setFolderId(cursor.getInt(cursor.getColumnIndex(UrlConstants.KEY_FOLDER_ID)));
        url.setBrowserId(cursor.getInt(cursor.getColumnIndex(UrlConstants.KEY_BROWSER_ID)));

        cursor.close();
        return url;
    }

    // get for folder
    public List<Url> getForOneFolder(int folderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Url> urlList = new ArrayList<>();

        Cursor cursor = db.query(
                UrlConstants.TABLE_NAME,
                new String[] {
                        UrlConstants.KEY_ID,
                        UrlConstants.KEY_TITLE,
                        UrlConstants.KEY_URL,
                        UrlConstants.KEY_MEMO,
                        UrlConstants.KEY_ADDED_DATE,
                        UrlConstants.KEY_BROWSING_DATE,
                        UrlConstants.KEY_FOLDER_ID,
                        UrlConstants.KEY_BROWSER_ID
                },
                UrlConstants.KEY_FOLDER_ID + "=?",
                new String[] {String.valueOf(folderId)},
                null, null, null
                );
        if (cursor.moveToFirst()) {
            do {
                Url url = new Url();
                url.setId(cursor.getInt(cursor.getColumnIndex(UrlConstants.KEY_ID)));
                url.setTitle(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_TITLE)));
                url.setUrl(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_URL)));
                url.setMemo(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_MEMO)));
                try {
                    url.setAddedDate(UrlConstants.dateFormat.parse(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_ADDED_DATE))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    url.setBrowsingDate(UrlConstants.dateFormat.parse(cursor.getString(cursor.getColumnIndex(UrlConstants.KEY_BROWSING_DATE))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                url.setFolderId(cursor.getInt(cursor.getColumnIndex(UrlConstants.KEY_FOLDER_ID)));
                url.setBrowserId(cursor.getInt(cursor.getColumnIndex(UrlConstants.KEY_BROWSER_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return urlList;
    }

    // update
    public int update(Url url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(UrlConstants.KEY_TITLE, url.getTitle());
        values.put(UrlConstants.KEY_URL, url.getUrl());
        values.put(UrlConstants.KEY_MEMO, url.getMemo());
        values.put(UrlConstants.KEY_ADDED_DATE, UrlConstants.dateFormat.format(url.getAddedDate()));
        values.put(UrlConstants.KEY_BROWSING_DATE, UrlConstants.dateFormat.format(url.getBrowsingDate()));
        values.put(UrlConstants.KEY_FOLDER_ID, url.getFolderId());
        values.put(UrlConstants.KEY_BROWSER_ID, url.getBrowserId());

        return db.update(
                UrlConstants.TABLE_NAME,
                values,
                UrlConstants.KEY_ID + "=?",
                new String[] {String.valueOf(url.getId())}
                );
    }

    // delete
    public void deleteUrl(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(UrlConstants.TABLE_NAME, UrlConstants.KEY_ID + "=?",new String[]{String.valueOf(id)});
    }

    // get count
    // get all
}
