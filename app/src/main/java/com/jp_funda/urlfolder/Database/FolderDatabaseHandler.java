package com.jp_funda.urlfolder.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.jp_funda.urlfolder.Models.Folder;
import com.jp_funda.urlfolder.Models.Url;

import java.util.Date;
import java.util.List;

public class FolderDatabaseHandler extends SQLiteOpenHelper {
    private Context context;

    public FolderDatabaseHandler(@Nullable Context context) {
        super(context, FolderConstants.DB_NAME, null, FolderConstants.DB_VERSION);
        this.context = context;
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
        if (folder.getUrls() != null) {
            for (Url url: folder.getUrls()) {
                String urlId = String.valueOf(url.getId());
                urlIdsStringBuilder.append(urlId + ",");
            }
            urlIdsStringBuilder.setLength(urlIdsStringBuilder.length()-1); // 余分なコンマを削除
            values.put(FolderConstants.KEY_URL_IDS, urlIdsStringBuilder.toString());
        }
        StringBuilder childIdsStringBuilder = new StringBuilder();
        if (folder.getChildFolders() != null) {
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

    // get all
    // update
    // delete
    // get count
}
