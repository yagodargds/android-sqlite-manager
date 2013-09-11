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
    protected DbBaseManager(Context context) {
        this.dbTableManagers = new HashMap<String, DbTableBaseManager>();
        addAllDbTableManager(registerDbTableManagers());
        setDbHelper(registerDbHelper(context));
        loadAllRecords();
    }

    public <V extends DbTableBaseContract> DbTableBaseManager getDbTableManager(V contract) {
        return dbTableManagers.get(contract.getTableName());
    }

    public Collection<DbTableBaseManager> getAllDbTableManagers() {
        return dbTableManagers.values();
    }

    protected abstract Collection<DbTableBaseManager> registerDbTableManagers();

    protected abstract T registerDbHelper(Context context);

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
            rowId = dbHelper.getWritableDatabase().insertOrThrow(tableName, nullColumnHack, values);
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
            rowId = dbHelper.getWritableDatabase().replaceOrThrow(tableName, nullColumnHack, initialValues);
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

    protected void delAllRecords() {
        for (DbTableBaseManager tableManager : getAllDbTableManagers()) {
            tableManager.delAllRecords();
        }
    }

    private void addDbTableManager(DbTableBaseManager dbTableManager) {
        if(dbTableManager != null) {
            dbTableManagers.put(dbTableManager.getTableName(), dbTableManager);
            dbTableManager.setDbManager(this);
        }
    }

    private void addAllDbTableManager(Collection<DbTableBaseManager> dbTableManagers) {
        if(dbTableManagers != null) {
            for(DbTableBaseManager dbTableManager : dbTableManagers) {
                addDbTableManager(dbTableManager);
            }
        }
    }

    private void setDbHelper(T dbHelper) {
        if(dbHelper != null) {
            this.dbHelper = dbHelper;
            this.dbHelper.setDbManager(this);
        }
    }

    private void loadAllRecords() {
        for (DbTableBaseManager dbTableManager : dbTableManagers.values()) {
            dbTableManager.loadRecords();
        }
    }

    private T dbHelper;
    private final HashMap<String, DbTableBaseManager> dbTableManagers;

    private static final String LOG_TAG = DbBaseManager.class.getSimpleName();
}
