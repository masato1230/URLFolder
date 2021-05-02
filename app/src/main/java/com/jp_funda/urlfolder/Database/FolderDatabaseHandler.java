package com.jp_funda.urlfolder.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.jp_funda.urlfolder.Models.Folder;
import com.jp_funda.urlfolder.Models.Url;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FolderDatabaseHandler extends SQLiteOpenHelper {
    private Context context;
    private UrlDatabaseHandler urlDB;

    public FolderDatabaseHandler(@Nullable Context context) {
        super(context, FolderConstants.DB_NAME, null, FolderConstants.DB_VERSION);
        this.context = context;
        this.urlDB = new UrlDatabaseHandler(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FOLDER_TABLE = "CREATE TABLE " + FolderConstants.TABLE_NAME + "("
                + FolderConstants.KEY_ID + " INTEGER PRIMARY KEY,"
                + FolderConstants.KEY_TITLE + " TEXT,"
                + FolderConstants.KEY_COLOR_INT + " INTEGER,"
                + FolderConstants.KEY_PARENT_ID + " INTEGER,"
                + FolderConstants.KEY_MEMO + " TEXT,"
                + FolderConstants.KEY_CREATED_DATE + " TEXT,"
                + FolderConstants.KEY_IS_SECRET + " INTEGER,"
                + FolderConstants.KEY_IS_ROOT + " INTEGER,"
                + FolderConstants.KEY_PASSWORD + " TEXT,"
                + FolderConstants.KEY_URL_IDS + " TEXT,"
                + FolderConstants.KEY_CHILD_IDS + " TEXT"
                + ");";
        db.execSQL(CREATE_FOLDER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FolderConstants.TABLE_NAME);
        onCreate(db);
    }

    // add
    public long addFolder(Folder folder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FolderConstants.KEY_TITLE, folder.getTitle());
        values.put(FolderConstants.KEY_COLOR_INT, folder.getColorInt());
        values.put(FolderConstants.KEY_PARENT_ID, folder.getParentId());
        values.put(FolderConstants.KEY_MEMO, folder.getMemo());
        values.put(FolderConstants.KEY_CREATED_DATE, FolderConstants.dateFormat.format(folder.getCreatedDate()));
        values.put(FolderConstants.KEY_IS_SECRET, folder.isSecret() ? 1 : 0);
        values.put(FolderConstants.KEY_IS_ROOT, folder.isRoot() ? 1 : 0);
        values.put(FolderConstants.KEY_PASSWORD, folder.getPassword());
        StringBuilder urlIdsStringBuilder = new StringBuilder();
        if (folder.getUrls() != null && folder.getUrls().size() > 1) {
            for (Url url: folder.getUrls()) {
                String urlId = String.valueOf(url.getId());
                urlIdsStringBuilder.append(urlId + ",");
            }
            urlIdsStringBuilder.setLength(urlIdsStringBuilder.length()-1); // 余分なコンマを削除
            values.put(FolderConstants.KEY_URL_IDS, urlIdsStringBuilder.toString());
        }
        StringBuilder childIdsStringBuilder = new StringBuilder();
        if (folder.getChildFolders() != null && folder.getUrls().size() > 1) {
            for (Folder childFolder: folder.getChildFolders()) {
                String childId = String.valueOf(childFolder.getId());
                childIdsStringBuilder.append(childId + ",");
            }
            childIdsStringBuilder.setLength(childIdsStringBuilder.length()-1);
            values.put(FolderConstants.KEY_CHILD_IDS, childIdsStringBuilder.toString());
        }
        return db.insert(FolderConstants.TABLE_NAME, null, values);
    }

    // get one
    public Folder getOneFolder(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Folder folder = new Folder();

        Cursor cursor = db.query(
                FolderConstants.TABLE_NAME,
                new String[] {
                        FolderConstants.KEY_ID,
                        FolderConstants.KEY_TITLE,
                        FolderConstants.KEY_COLOR_INT,
                        FolderConstants.KEY_PARENT_ID,
                        FolderConstants.KEY_MEMO,
                        FolderConstants.KEY_CREATED_DATE,
                        FolderConstants.KEY_IS_SECRET,
                        FolderConstants.KEY_IS_ROOT,
                        FolderConstants.KEY_PASSWORD,
                        FolderConstants.KEY_URL_IDS,
                        FolderConstants.KEY_CHILD_IDS
                },
                FolderConstants.KEY_ID + "=?",
                new String[] {String.valueOf(id)},
                null,  null,  null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        folder.setId(id);
        folder.setTitle(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_TITLE)));
        folder.setColorInt(cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_COLOR_INT)));
        folder.setParentId(cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_PARENT_ID)));
        folder.setMemo(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_MEMO)));
        try {
            folder.setCreatedDate(FolderConstants.dateFormat.parse(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_CREATED_DATE))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        folder.setSecret(1 == cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_IS_SECRET)));
        folder.setRoot(1 == cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_IS_ROOT)));
        folder.setPassword(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_PASSWORD)));
        folder.setUrls(urlDB.getForOneFolder(id));
        // childFolders
        List<Folder> childFolders = new ArrayList<>();
        String childFolderIdsString = cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_CHILD_IDS));
        if (childFolderIdsString != null) {
            String[] childFolderStringSplit = childFolderIdsString.split(",");
            for (String childFolderIdString: childFolderStringSplit) {
                Folder childFolder = this.getOneFolder(Integer.parseInt(childFolderIdString));
                childFolders.add(childFolder);
            }
            folder.setChildFolders(childFolders);
        }

        cursor.close();
        return folder;
    }

    // get all
    public List<Folder> getAllFolder() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Folder> folderList = new ArrayList<>();

        Cursor cursor = db.query(
                FolderConstants.TABLE_NAME,
                new String[] {
                        FolderConstants.KEY_ID,
                        FolderConstants.KEY_TITLE,
                        FolderConstants.KEY_COLOR_INT,
                        FolderConstants.KEY_PARENT_ID,
                        FolderConstants.KEY_MEMO,
                        FolderConstants.KEY_CREATED_DATE,
                        FolderConstants.KEY_IS_SECRET,
                        FolderConstants.KEY_IS_ROOT,
                        FolderConstants.KEY_PASSWORD,
                        FolderConstants.KEY_URL_IDS,
                        FolderConstants.KEY_CHILD_IDS
                }, null, null, null,  null,  null);

        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();
        do {
            Folder folder = new Folder();
            folder.setId(cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_ID)));
            folder.setTitle(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_TITLE)));
            folder.setColorInt(cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_COLOR_INT)));
            folder.setParentId(cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_PARENT_ID)));
            folder.setMemo(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_MEMO)));
            try {
                folder.setCreatedDate(FolderConstants.dateFormat.parse(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_CREATED_DATE))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            folder.setSecret(1 == cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_IS_SECRET)));
            folder.setRoot(1 == cursor.getInt(cursor.getColumnIndex(FolderConstants.KEY_IS_ROOT)));
            folder.setPassword(cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_PASSWORD)));
            folder.setUrls(urlDB.getForOneFolder(folder.getId()));
            // childFolders
            List<Folder> childFolders = new ArrayList<>();
            String childFolderIdsString = cursor.getString(cursor.getColumnIndex(FolderConstants.KEY_CHILD_IDS));
            if (childFolderIdsString != null) {
                String[] childFolderStringSplit = childFolderIdsString.split(",");
                for (String childFolderIdString: childFolderStringSplit) {
                    Folder childFolder = this.getOneFolder(Integer.parseInt(childFolderIdString));
                    childFolders.add(childFolder);
                }
                folder.setChildFolders(childFolders);
            }

            folderList.add(folder);
        } while (cursor.moveToNext());

        cursor.close();
        return folderList;
    }

    // update
    public int updateFolder(Folder folder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FolderConstants.KEY_TITLE, folder.getTitle());
        values.put(FolderConstants.KEY_COLOR_INT, folder.getColorInt());
        values.put(FolderConstants.KEY_PARENT_ID, folder.getParentId());
        values.put(FolderConstants.KEY_MEMO, folder.getMemo());
        values.put(FolderConstants.KEY_CREATED_DATE, FolderConstants.dateFormat.format(folder.getCreatedDate()));
        values.put(FolderConstants.KEY_IS_SECRET, folder.isSecret() ? 1 : 0);
        values.put(FolderConstants.KEY_IS_ROOT, folder.isRoot() ? 1 : 0);
        values.put(FolderConstants.KEY_PASSWORD, folder.getPassword());
        StringBuilder urlIdsStringBuilder = new StringBuilder();
        if (folder.getUrls() != null && folder.getUrls().size() > 1) {
            for (Url url: folder.getUrls()) {
                String urlId = String.valueOf(url.getId());
                urlIdsStringBuilder.append(urlId + ",");
            }
            urlIdsStringBuilder.setLength(urlIdsStringBuilder.length()-1); // 余分なコンマを削除
            values.put(FolderConstants.KEY_URL_IDS, urlIdsStringBuilder.toString());
        }
        StringBuilder childIdsStringBuilder = new StringBuilder();
        if (folder.getChildFolders() != null && folder.getUrls().size() > 1) {
            for (Folder childFolder: folder.getChildFolders()) {
                String childId = String.valueOf(childFolder.getId());
                childIdsStringBuilder.append(childId + ",");
            }
            childIdsStringBuilder.setLength(childIdsStringBuilder.length()-1);
            values.put(FolderConstants.KEY_CHILD_IDS, childIdsStringBuilder.toString());
        }
        return db.update(FolderConstants.TABLE_NAME, values, FolderConstants.KEY_ID + "=?", new String[] {String.valueOf(folder.getId())});
    }

    // delete
    public void deleteFolder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete urls included in the deleting folder
        List<Url> deletingUrls = urlDB.getForOneFolder(id);
        for (Url url: deletingUrls) {
            urlDB.deleteUrl(url.getId());
        }

        // delete the folder data
        db.delete(FolderConstants.TABLE_NAME, FolderConstants.KEY_ID + "=?", new String[] {String.valueOf(id)});
    }

    // get count
}
