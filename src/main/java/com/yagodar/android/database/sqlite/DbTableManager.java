package com.yagodar.android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;

import com.yagodar.essential.operation.OperationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yagodar on 22.08.13.
 */
public class DbTableManager {
    public DbTableManager(AbstractDbManager manager, AbstractDbTableContract contract) {
        mManager = manager;
        mContract = contract;
    }

    public OperationResult<Long> insert() {
        return insert(BaseColumns._ID, null);
    }

    public OperationResult<Long> insertToGroup(String columnNameGroupId, long groupId) {
        ContentValues values = new ContentValues();
        values.put(columnNameGroupId, groupId);
        return insert(BaseColumns._ID, values);
    }

    public OperationResult<Long> insert(ContentValues values) {
        return insert(BaseColumns._ID, values);
    }

    public OperationResult<Long> insert(String nullColumnHack, ContentValues values) {
        return mManager.insert(getTableName(), nullColumnHack, values);
    }

    public OperationResult<Integer> update(long id, ContentValues values) {
        return update(values, BaseColumns._ID + DbHelper.SYMB_OP_EQUALITY + id, null);
    }

    public OperationResult<Integer> update(ContentValues values, String whereClause, String[] whereArgs) {
        return mManager.update(getTableName(), values, whereClause, whereArgs);
    }

    public OperationResult<Long> replace(String nullColumnHack, ContentValues initialValues) {
        return mManager.replace(getTableName(), nullColumnHack, initialValues);
    }

    public OperationResult<DbTableRecord> getRecord(long id) {
        final DbTableRecord[] record = new DbTableRecord[1];
        OperationResult<Void> queryResult = query(null, BaseColumns._ID + DbHelper.SYMB_OP_EQUALITY + id, null, null, null, null, null, new ICursorHandler() {
            @Override
            public void handle(Cursor cs) throws SQLiteException {
                if(cs.moveToNext()) {
                    Object columnValues[] = new Object[getContract().getDbTableColumnsCount()];
                    for (DbTableColumn column : getContract().getAllDbTableColumns()) {
                        columnValues[cs.getColumnIndex(column.getColumnName())] = getValue(cs, column);
                    }
                    record[0] = new DbTableRecord(columnValues);
                }
            }
        });

        OperationResult<DbTableRecord> opResult = new OperationResult<>();

        if(queryResult.isSuccessful()) {
            if(record[0] != null) {
                opResult.setData(record[0]);
            } else {
                opResult.setFailMessage("No record in table[" + getTableName() + "] for id[" + id + "]");
            }
        } else {
            opResult.setFailThrowable(queryResult.getFailThrowable());
        }

        return opResult;
    }

    public OperationResult<List<DbTableRecord>> getGroupRecords(String columnNameGroupId, long groupId) {
        final List<DbTableRecord> allRecords = new ArrayList<>();
        OperationResult<Void> queryResult = query(null, columnNameGroupId + DbHelper.SYMB_OP_EQUALITY + groupId, null, null, null, null, null, new ICursorHandler() {
            @Override
            public void handle(Cursor cs) throws SQLiteException {
                while(cs.moveToNext()) {
                    Object columnValues[] = new Object[getContract().getDbTableColumnsCount()];
                    for (DbTableColumn column : getContract().getAllDbTableColumns()) {
                        columnValues[cs.getColumnIndex(column.getColumnName())] = getValue(cs, column);
                    }
                    allRecords.add(new DbTableRecord(columnValues));
                }
            }
        });

        OperationResult<List<DbTableRecord>> opResult = new OperationResult<>();

        if(queryResult.isSuccessful()) {
            opResult.setData(allRecords);
        } else {
            opResult.setFailThrowable(queryResult.getFailThrowable());
        }

        return opResult;
    }

    public OperationResult<Integer> getGroupRecordsCount(String columnNameGroupId, long groupId) {
        final Integer[] count = new Integer[1];
        OperationResult<Void> queryResult = query(new String[] { DbHelper.EXPR_COUNT + DbHelper.SYMB_BRACKET_OPEN + columnNameGroupId + DbHelper.SYMB_BRACKET_CLOSE }, columnNameGroupId + DbHelper.SYMB_OP_EQUALITY + groupId, null, null, null, null, null, new ICursorHandler() {
            @Override
            public void handle(Cursor cs) throws SQLiteException {
                if(cs.moveToNext()) {
                    count[0] = cs.getInt(0);
                }
            }
        });

        OperationResult<Integer> opResult = new OperationResult<>();

        if(queryResult.isSuccessful()) {
            if(count[0] != null) {
                opResult.setData(count[0]);
            } else {
                opResult.setFailMessage("Can`t get group records count in table[" + getTableName() + "] for group id[" + groupId + "]");
            }
        } else {
            opResult.setFailThrowable(queryResult.getFailThrowable());
        }

        return opResult;
    }

    public OperationResult<List<DbTableRecord>> getAllRecords() {
        final List<DbTableRecord> allRecords = new ArrayList<>();
        OperationResult<Void> queryResult = query(null, null, null, null, null, null, null, new ICursorHandler() {
            @Override
            public void handle(Cursor cs) throws SQLiteException {
                while(cs.moveToNext()) {
                    Object columnValues[] = new Object[getContract().getDbTableColumnsCount()];
                    for (DbTableColumn column : getContract().getAllDbTableColumns()) {
                        columnValues[cs.getColumnIndex(column.getColumnName())] = getValue(cs, column);
                    }
                    allRecords.add(new DbTableRecord(columnValues));
                }
            }
        });

        OperationResult<List<DbTableRecord>> opResult = new OperationResult<>();

        if(queryResult.isSuccessful()) {
            opResult.setData(allRecords);
        } else {
            opResult.setFailThrowable(queryResult.getFailThrowable());
        }

        return opResult;
    }

    public OperationResult<Void> query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, ICursorHandler handler) {
        return mManager.query(getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit, handler);
    }

    public OperationResult<Integer> delete(long id) {
        return delete(BaseColumns._ID + DbHelper.SYMB_OP_EQUALITY + id, null);
    }

    public OperationResult<Integer> deleteGroup(String columnNameGroupId, long groupId) {
        return delete(columnNameGroupId + DbHelper.SYMB_OP_EQUALITY + groupId, null);
    }

    public OperationResult<Integer> deleteAll() {
        return delete(null, null);
    }

    public OperationResult<Integer> delete(String whereClause, String[] whereArgs) {
        return mManager.delete(getTableName(), whereClause, whereArgs);
    }

    public AbstractDbTableContract getContract() {
        return mContract;
    }

    protected AbstractDbManager getManager() {
        return mManager;
    }

    protected String getTableName() {
        return mContract.getTableName();
    }

    protected String getSQLExprCreateTable() {
        return DbHelper.EXPR_CREATE_TABLE_IF_NOT_EXISTS
                + getTableName()
                + DbHelper.SYMB_BRACKET_OPEN
                + getSQLExprCreateDbTableColumns()
                + DbHelper.SYMB_BRACKET_CLOSE
                + DbHelper.SYMB_DOT_COMMA;
    }

    protected String getSQLExprDeleteTable() {
        return DbHelper.EXPR_DROP_TABLE_IF_EXISTS
                + getTableName();
    }

    private String getSQLExprCreateDbTableColumns() {
        String sqlExpr = "";

        for (DbTableColumn columnInfo : mContract.getAllDbTableColumns()) {
            sqlExpr += columnInfo.getSQLExprOfCreation();
            sqlExpr += DbHelper.SYMB_COMMA;
        }

        return sqlExpr.substring(0, sqlExpr.length() - DbHelper.SYMB_COMMA.length());
    }

    private Object getValue(Cursor cs, DbTableColumn column) {
        Object value = null;

        if(cs != null) {
            int columnIndex = cs.getColumnIndex(column.getColumnName());

            if(columnIndex != -1) {
                switch(column.getType()) {
                    case DbTableColumn.TYPE_BLOB:
                        value = cs.getBlob(columnIndex);
                        break;
                    case DbTableColumn.TYPE_DOUBLE:
                        value = cs.getDouble(columnIndex);
                        break;
                    case DbTableColumn.TYPE_FLOAT:
                        value = cs.getFloat(columnIndex);
                        break;
                    case DbTableColumn.TYPE_INTEGER:
                        value = cs.getInt(columnIndex);
                        break;
                    case DbTableColumn.TYPE_BOOLEAN:
                        value = cs.getInt(columnIndex) == 1;
                        break;
                    case DbTableColumn.TYPE_LONG:
                        value = cs.getLong(columnIndex);
                        break;
                    case DbTableColumn.TYPE_SHORT:
                        value = cs.getShort(columnIndex);
                        break;
                    case DbTableColumn.TYPE_STRING:
                        value = cs.getString(columnIndex);
                        break;
                    default:
                        break;
                }
            }
        }

        return value;
    }

    public class DbTableRecord {
        protected DbTableRecord(Object[] values) {
            if(values == null) {
                throw new IllegalArgumentException("Values must not be null!");
            }

            mValues = values;
        }

        public long getId() {
            return (Long) getValue(BaseColumns._ID);
        }

        public Object getValue(String columnName) {
            return mValues[mContract.getDbTableColumnIndex(columnName)];
        }

        private Object[] mValues;
    }

    private final AbstractDbManager mManager;
    private final AbstractDbTableContract mContract;
}
