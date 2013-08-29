package com.yagodar.android.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Yagodar on 20.08.13.
 */
public abstract class DbBaseManager<T extends DbBaseHelper> {
    protected DbBaseManager(T dbHelper) {
        this.dbHelper = dbHelper;
        this.dbHelper.setDbManager(this);
        this.dbTableManagers = new HashMap<String, DbTableBaseManager>();
    }

    public DbTableBaseManager getDbTableManager(String tableName) {
        return dbTableManagers.get(tableName);
    }

    public Collection<DbTableBaseManager> getAllDbTableManagers() {
        return dbTableManagers.values();
    }

    protected void addDbTableManager(DbTableBaseManager dbTableManager) {
        if(dbTableManager != null) {
            dbTableManagers.put(dbTableManager.getTableName(), dbTableManager);
            dbTableManager.setDbManager(this);
        }
    }

    protected void removeDbTableManager(String tableName) {
        dbTableManagers.remove(tableName);
    }

    protected T getDbHelper() {
        return dbHelper;
    }

    protected boolean isContextEquals(Context context) {
        if(context != null) {
            return dbHelper.getReadableDatabase().getPath().equals(context.getDatabasePath(getDbName()));
        }

        return false;
    }

    protected String getDbName() {
        String dbPath = dbHelper.getReadableDatabase().getPath();
        return dbPath.substring(dbPath.lastIndexOf("/") + 1);
    }

    protected long insert(String tableName, String nullColumnHack, ContentValues values) {
        long rowId = -1;

        try {
            rowId = dbHelper.getWritableDatabase().insert(tableName, nullColumnHack, values);
        }
        catch(Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }

        return rowId;
    }

    protected int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        int rowsAffected = 0;

        try {
            rowsAffected = dbHelper.getWritableDatabase().update(tableName, values, whereClause, whereArgs);
        }
        catch(Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }

        return rowsAffected;
    }

    protected long replace(String tableName, String nullColumnHack, ContentValues initialValues) {
        long rowId = 0;

        try {
            rowId = dbHelper.getWritableDatabase().replace(tableName, nullColumnHack, initialValues);
        }
        catch(Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }

        return rowId;
    }

    protected Cursor query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        Cursor cs = null;

        try {
            cs = dbHelper.getReadableDatabase().query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        }
        catch(Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }

        return cs;
    }

    protected int delete(String tableName, String whereClause, String[] whereArgs) {
        int rowsAffected = 0;

        try {
            rowsAffected = dbHelper.getWritableDatabase().delete(tableName, whereClause, whereArgs);
        }
        catch(Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }

        return rowsAffected;
    }

    private final T dbHelper;
    private final HashMap<String, DbTableBaseManager> dbTableManagers;

    private static final String LOG_TAG = DbBaseManager.class.getSimpleName();
}
