package com.yagodar.android.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Yagodar on 20.08.13.
 */
public abstract class AbstractDbManager {
    protected AbstractDbManager(Context context, String dbName, SQLiteDatabase.CursorFactory csFactory, int dbVersion) {
        this.dbTableManagers = new HashMap<String, DbTableManager>();
        addAllDbTableManager(registerDbTableManagers());
        setDbHelper(registerDbHelper(context, dbName, csFactory, dbVersion));
        loadAllRecords();
    }

    public <V extends AbstractDbTableContract> DbTableManager getDbTableManager(V contract) {
        return dbTableManagers.get(contract.getTableName());
    }

    public <V extends AbstractDbTableContract> DbTableManager getDbTableManager(String tableName) {
        return dbTableManagers.get(tableName);
    }

    public Collection<DbTableManager> getAllDbTableManagers() {
        return dbTableManagers.values();
    }

    protected abstract Collection<AbstractDbTableContract> registerDbTableContracts();

    protected Collection<DbTableManager> registerDbTableManagers() {
        ArrayList<DbTableManager> dbTableManagers = new ArrayList<DbTableManager>();

        for(AbstractDbTableContract dbTableContract : registerDbTableContracts()) {
            dbTableManagers.add(new DbTableManager(dbTableContract));
        }

        return dbTableManagers;
    }


    protected DbHelper registerDbHelper(Context contextString, String dbName, SQLiteDatabase.CursorFactory csFactory, int dbVersion) {
        return new DbHelper(contextString, dbName, csFactory, dbVersion);
    }

    protected void removeDbTableManager(String tableName) {
        dbTableManagers.remove(tableName);
    }

    protected DbHelper getDbHelper() {
        return dbHelper;
    }

    protected boolean isContextEquals(Context context) {
        if(context != null) {
            return dbHelper.getReadableDatabase().getPath().equals(context.getDatabasePath(getDbName()).getPath());
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
        for (DbTableManager tableManager : getAllDbTableManagers()) {
            tableManager.delAllRecords();
        }
    }

    private void addDbTableManager(DbTableManager dbTableManager) {
        if(dbTableManager != null) {
            dbTableManagers.put(dbTableManager.getTableName(), dbTableManager);
            dbTableManager.setDbManager(this);
        }
    }

    private void addAllDbTableManager(Collection<DbTableManager> dbTableManagers) {
        if(dbTableManagers != null) {
            for(DbTableManager dbTableManager : dbTableManagers) {
                addDbTableManager(dbTableManager);
            }
        }
    }

    private void setDbHelper(DbHelper dbHelper) {
        if(dbHelper != null) {
            this.dbHelper = dbHelper;
            this.dbHelper.setDbManager(this);
        }
    }

    private void loadAllRecords() {
        for (DbTableManager dbTableManager : dbTableManagers.values()) {
            dbTableManager.loadRecords();
        }
    }

    private DbHelper dbHelper;
    private final HashMap<String, DbTableManager> dbTableManagers;

    private static final String LOG_TAG = AbstractDbManager.class.getSimpleName();
}
