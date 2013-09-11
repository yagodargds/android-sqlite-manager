package com.yagodar.android.database.sqlite;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by Yagodar on 23.08.13.
 */
public abstract class DbTableBaseContract implements BaseColumns {
    protected DbTableBaseContract(String tableName) {
        this.tableName = tableName;
        dbTableColumns = new LinkedList<DbTableColumn>();
        dbTableColumnNames = new LinkedList<String>();
    }

    public String getTableName() {
        return tableName;
    }

    public DbTableColumn getDbTableColumn(String columnName) {
        return getDbTableColumn(getDbTableColumnIndex(columnName));
    }

    public DbTableColumn getDbTableColumn(int columnIndx) {
        DbTableColumn column = null;

        if(columnIndx >= 0 && columnIndx < dbTableColumns.size()) {
            column = dbTableColumns.get(columnIndx);
        }

        return column;
    }

    public int getDbTableColumnIndex(String columnName) {
        return dbTableColumnNames.indexOf(columnName);
    }

    public int getDbTableColumnIndex(DbTableColumn column) {
        return dbTableColumns.indexOf(column);
    }

    public int getDbTableColumnsCount() {
        return dbTableColumns.size();
    }

    public LinkedList<DbTableColumn> getAllDbTableColumns() {
        return dbTableColumns;
    }

    protected void addDbTableColumn(String columnName) {
        addDbTableColumn(new DbTableColumn(columnName));
    }

    protected void addDbTableColumn(String columnName, Object defValue) {
        addDbTableColumn(new DbTableColumn(columnName, defValue));
    }

    protected void addDbTableColumn(boolean isPrimaryKey, String columnName) {
        addDbTableColumn(new DbTableColumn(isPrimaryKey, columnName));
    }

    private void addDbTableColumn(DbTableColumn dbTableColumn) {
        if(dbTableColumn != null) {
            dbTableColumns.add(dbTableColumn);
            dbTableColumnNames.add(dbTableColumn.getColumnName());
        }
    }

    private final String tableName;
    private final LinkedList<DbTableColumn> dbTableColumns;
    private final LinkedList<String> dbTableColumnNames;
}
