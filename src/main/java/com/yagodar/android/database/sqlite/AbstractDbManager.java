package com.yagodar.android.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.yagodar.essential.operation.OperationResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yagodar on 20.08.13.
 */
public abstract class AbstractDbManager {
    protected AbstractDbManager(Context context, String dbName, SQLiteDatabase.CursorFactory csFactory, int dbVersion) {
        mTableManagerByName = new HashMap<>();
        regTableManagers();
        mHelper = new DbHelper(this, context, dbName, csFactory, dbVersion);
    }

    public DbTableManager getTableManager(AbstractDbTableContract contract) {
        return mTableManagerByName.get(contract.getTableName());
    }

    public DbTableManager getTableManager(String tableName) {
        return mTableManagerByName.get(tableName);
    }

    public Collection<DbTableManager> getAllTableManagers() {
        return mTableManagerByName.values();
    }

    public SQLiteDatabase getDatabase() throws SQLiteException {
        return mHelper.getWritableDatabase();
    }

    public void closeCursor(Cursor cs) {
        if(cs != null) {
            cs.close();
        }
    }

    public void closeDatabase(SQLiteDatabase db) {
        if(db != null) {
            db.close();
        }
    }

    protected abstract List<AbstractDbTableContract> regTableContracts();

    protected void regTableManagers() {
        List<AbstractDbTableContract> dbTableContracts = regTableContracts();
        if(dbTableContracts == null) {
            throw new IllegalArgumentException("Db Table Contracts must be properly registered first!");
        }

        for(AbstractDbTableContract dbTableContract : dbTableContracts) {
            mTableManagerByName.put(dbTableContract.getTableName(), new DbTableManager(this, dbTableContract));
        }
    }

    protected DbHelper getHelper() {
        return mHelper;
    }

    protected OperationResult<Long> insert(String tableName, String nullColumnHack, ContentValues values) {
        OperationResult<Long> opResult = new OperationResult<>();

        SQLiteDatabase db = null;
        try {
            db = getDatabase();
            long rowId = db.insertOrThrow(tableName, nullColumnHack, values);
            opResult.setData(rowId);
        } catch(Exception e) {
            opResult.setFailThrowable(e);
        } finally {
            closeDatabase(db);
        }

        return opResult;
    }

    protected OperationResult<Integer> update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        OperationResult<Integer> opResult = new OperationResult<>();

        SQLiteDatabase db = null;
        try {
            db = getDatabase();
            int rowsAffected = db.update(tableName, values, whereClause, whereArgs);
            opResult.setData(rowsAffected);
        } catch(Exception e) {
            opResult.setFailThrowable(e);
        } finally {
            closeDatabase(db);
        }

        return opResult;
    }

    protected OperationResult<Long> replace(String tableName, String nullColumnHack, ContentValues initialValues) {
        OperationResult<Long> opResult = new OperationResult<>();

        SQLiteDatabase db = null;
        try {
            db = getDatabase();
            long rowId = db.replaceOrThrow(tableName, nullColumnHack, initialValues);
            opResult.setData(rowId);
        } catch(Exception e) {
            opResult.setFailThrowable(e);
        } finally {
            closeDatabase(db);
        }

        return opResult;
    }

    protected OperationResult<Void> query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, ICursorHandler handler) {
        OperationResult<Void> opResult = new OperationResult<>();

        SQLiteDatabase db = null;
        Cursor cs = null;
        try {
            db = getDatabase();
            cs = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            handler.handle(cs);
        } catch(Exception e) {
            opResult.setFailThrowable(e);
        } finally {
            closeCursor(cs);
            closeDatabase(db);
        }

        return opResult;
    }

    protected OperationResult<Integer> delete(String tableName, String whereClause, String[] whereArgs) {
        OperationResult<Integer> opResult = new OperationResult<>();

        SQLiteDatabase db = null;
        try {
            db = getDatabase();
            int rowsAffected = db.delete(tableName, whereClause, whereArgs);
            opResult.setData(rowsAffected);
        } catch(Exception e) {
            opResult.setFailThrowable(e);
        } finally {
            closeDatabase(db);
        }

        return opResult;
    }

    private final DbHelper mHelper;
    private final Map<String, DbTableManager> mTableManagerByName;
}
