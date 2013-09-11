package com.yagodar.android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Yagodar on 22.08.13.
 */
public class DbTableBaseManager<T extends DbBaseManager> {
    public DbTableBaseManager(DbTableBaseContract tableContract) {
        this.tableContract = tableContract;
    }

    public String getSQLExprCreateDbTable() {
        return DbBaseHelper.EXPR_CREATE_TABLE_IF_NOT_EXISTS
                + getTableName()
                + DbBaseHelper.SYMB_BRACKET_OPEN
                + getSQLExprCreateDbTableColumns()
                + DbBaseHelper.SYMB_BRACKET_CLOSE
                + DbBaseHelper.SYMB_DOT_COMMA;
    }

    public long addRecord() {
        return addRecord(BaseColumns._ID, null);
    }

    public long addRecord(String nullColumnHack, ContentValues values) {
        long id = insert(nullColumnHack, values);

        if(id != -1) {
            recordsMap.put(id, new DbTableRecord(id, null));
        }

        return id;
    }

    public int setValues(long id, ContentValues values) {
        return update(values, BaseColumns._ID + DbBaseHelper.SYMB_OP_EQUALITY + id, null);
    }

    public DbTableRecord getRecord(long id) {
        return recordsMap.get(id);
    }

    public Collection<DbTableRecord> getAllRecords() {
        return recordsMap.values();
    }

    public int delRecord(long id) {
        int rowsAffected = delete(BaseColumns._ID + DbBaseHelper.SYMB_OP_EQUALITY + id, null);

        if(rowsAffected != 0) {
            recordsMap.remove(id);
        }

        return rowsAffected;
    }

    public int delAllRecords() {
        int rowsAffected = delete(null, null);

        if(rowsAffected != 0) {
            recordsMap.clear();
        }

        return rowsAffected;
    }

    public String getSQLExprDeleteDbTable() {
        return DbBaseHelper.EXPR_DROP_TABLE_IF_EXISTS
                + getTableName();
    }

    public DbTableBaseContract getTableContract() {
        return tableContract;
    }

    public Object getColumnValue(long id, String columnName) {
        return recordsMap.get(id).getColumnValue(columnName);
    }

    public int setColumnValue(long id, String columnName, Object value) {
        ContentValues values = new ContentValues();

        if(value == null) {
            values.putNull(columnName);
        }
        else if(value instanceof Boolean) {
            values.put(columnName, (Boolean) value);
        }
        else if(value instanceof Byte) {
            values.put(columnName, (Byte) value);
        }
        else if(value instanceof byte[]) {
            values.put(columnName, (byte[]) value);
        }
        else if(value instanceof Double) {
            values.put(columnName, (Double) value);
        }
        else if(value instanceof Float) {
            values.put(columnName, (Float) value);
        }
        else if(value instanceof Integer) {
            values.put(columnName, (Integer) value);
        }
        else if(value instanceof Long) {
            values.put(columnName, (Long) value);
        }
        else if(value instanceof Short) {
            values.put(columnName, (Short) value);
        }
        else if(value instanceof String) {
            values.put(columnName, (String) value);
        }
        else {
            return -1;
        }

        return setColumnValues(id, values);
    }

    protected void loadRecords() {
        recordsMap = new LinkedHashMap<Long, DbTableRecord>();

        Cursor cs = query(null, null, null, null, null, null, null);
        if(cs != null) {
            long id;
            while(cs.moveToNext()) {
                id = cs.getLong(cs.getColumnIndex(BaseColumns._ID));

                Object columnValues[] = new Object[getTableContract().getDbTableColumnsCount()];
                for (DbTableColumn column : getTableContract().getAllDbTableColumns()) {
                    columnValues[cs.getColumnIndex(column.getColumnName())] = getValue(cs, column);
                }

                recordsMap.put(id, new DbTableRecord(id, columnValues));
            }

            cs.close();
        }
    }

    protected int setColumnValues(long id, ContentValues values) {
        int rowsAffected = setValues(id, values);

        if(rowsAffected > 0) {
            for (Map.Entry<String, Object> valueEntry : values.valueSet()) {
                recordsMap.get(id).setColumnValue(valueEntry.getKey(), valueEntry.getValue());
            }
        }

        return rowsAffected;
    }

    protected String getTableName() {
        return tableContract.getTableName();
    }

    protected void setDbManager(T dbManager) {
        this.dbManager = dbManager;
    }

    protected T getDbManager() {
        return dbManager;
    }

    protected long insert(String nullColumnHack, ContentValues values) {
        return dbManager.insert(getTableName(), nullColumnHack, values);
    }

    protected int update(ContentValues values, String whereClause, String[] whereArgs) {
        return dbManager.update(getTableName(), values, whereClause, whereArgs);
    }

    protected long replace(String nullColumnHack, ContentValues initialValues) {
        return dbManager.replace(getTableName(), nullColumnHack, initialValues);
    }

    protected Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return dbManager.query(getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    protected int delete(String whereClause, String[] whereArgs) {
        return dbManager.delete(getTableName(), whereClause, whereArgs);
    }

    private String getSQLExprCreateDbTableColumns() {
        String sqlExpr = "";

        for (DbTableColumn columnInfo : tableContract.getAllDbTableColumns()) {
            sqlExpr += columnInfo.getSQLExprOfCreation();
            sqlExpr += DbBaseHelper.SYMB_COMMA;
        }

        return sqlExpr.substring(0, sqlExpr.length() - DbBaseHelper.SYMB_COMMA.length());
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
        protected DbTableRecord(long id, Object[] columnValues) {
            if(id != -1) {
                if(columnValues == null) {
                    columnValues = new Object[tableContract.getDbTableColumnsCount()];

                    for (DbTableColumn column : tableContract.getAllDbTableColumns()) {
                        if(!column.isPrimaryKey()) {
                            columnValues[tableContract.getDbTableColumnIndex(column)] = column.getDefValue();
                        }
                    }
                }

                columnValues[tableContract.getDbTableColumnIndex(BaseColumns._ID)] = id;
                this.columnValues = columnValues;
            }
        }

        public long getId() {
            return (Long) getColumnValue(BaseColumns._ID);
        }

        protected Object getColumnValue(String columnName) {
            return columnValues[tableContract.getDbTableColumnIndex(columnName)];
        }

        protected void setColumnValue(String columnName, Object value) {
            columnValues[tableContract.getDbTableColumnIndex(columnName)] = value;
        }

        private Object[] columnValues;
    }

    private T dbManager;
    private LinkedHashMap<Long, DbTableRecord> recordsMap;

    private final DbTableBaseContract tableContract;
}
