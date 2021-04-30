package com.jp_funda.urlfolder.Database;

import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FolderConstants {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "folderDB";
    public static final String TABLE_NAME = "folderTable";
    // table columns
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_COLOR_INT = "colorInt";
    public static final String KEY_PARENT_ID = "parentId";
    public static final String KEY_MEMO = "memo";
    public static final String KEY_CREATED_DATE = "createdDate";
    public static final String KEY_IS_SECRET = "isSecret";
    public static final String KEY_IS_ROOT = "isRoot";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_URL_IDS = "urlIds";
    public static final String KEY_CHILD_IDS = "childIds";

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
}
