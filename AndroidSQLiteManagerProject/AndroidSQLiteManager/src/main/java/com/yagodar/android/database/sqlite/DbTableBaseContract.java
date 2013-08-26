package com.yagodar.android.database.sqlite;

import android.provider.BaseColumns;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Yagodar on 23.08.13.
 */
public abstract class DbTableBaseContract implements BaseColumns {
    protected DbTableBaseContract(String tableName) {
        this.tableName = tableName;
        this.dbTableColumns = new HashMap<String, DbTableColumn>();
    }

    public String getTableName() {
        return tableName;
    }

    public DbTableColumn getDbTableColumn(String columnName) {
        return dbTableColumns.get(columnName);
    }

    public Collection<DbTableColumn> getAllDbTableColumn() {
        return dbTableColumns.values();
    }

    protected void addDbTableColumn(String columnName) {
        addDbTableColumn(new DbTableColumn(columnName));
    }

    protected void addDbTableColumn(String columnName, Object defValue) {
        addDbTableColumn(new DbTableColumn(columnName, defValue));
    }

    protected void addDbTableColumn(String columnName, boolean isPrimaryKey) {
        addDbTableColumn(new DbTableColumn(columnName, isPrimaryKey));
    }

    private void addDbTableColumn(DbTableColumn dbTableColumnInfo) {
        if(dbTableColumnInfo != null) {
            dbTableColumns.put(dbTableColumnInfo.getColumnName(), dbTableColumnInfo);
        }
    }

    protected void removeDbTableColumnInfo(String columnName) {
        dbTableColumns.remove(columnName);
    }

    public class DbTableColumn {
        private DbTableColumn(String columnName) {
            this(columnName, false, null);
        }

        private DbTableColumn(String columnName, Object defValue) {
            this(columnName, false, defValue);
        }

        private DbTableColumn(String columnName, boolean isPrimaryKey) {
            this(columnName, isPrimaryKey, null);
        }

        private DbTableColumn(String columnName, boolean isPrimaryKey, Object defValue) {
            this.columnName = columnName;
            this.isPrimaryKey = isPrimaryKey;
            this.defValue = defValue;

            if(this.isPrimaryKey) {
                this.type = DbBaseHelper.TYPE_INTEGER;
            }
            else if(defValue != null) {
                if(defValue instanceof String) {
                    this.type = DbBaseHelper.TYPE_TEXT;
                }
                else if(defValue instanceof Integer || defValue instanceof Long || defValue instanceof Byte) {
                    this.type = DbBaseHelper.TYPE_INTEGER;
                }
                else if(defValue instanceof Float || defValue instanceof Double) {
                    this.type = DbBaseHelper.TYPE_REAL;
                }
                else {
                    this.type = null;
                }
            }
            else {
                this.type = null;
            }
        }

        public String getColumnName() {
            return columnName;
        }

        public String getType() {
            return type;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public boolean isNotNull() {
            return defValue == null || this.type == null;
        }

        public Object getDefValue() {
            return defValue;
        }

        protected String getSQLExprOfCreation() {
            String sQLExprOfCreation = columnName + type;

            if(isPrimaryKey) {
                sQLExprOfCreation += DbBaseHelper.EXPR_PRIMARY_KEY;
            }
            else if(!isNotNull()) {
                sQLExprOfCreation += DbBaseHelper.EXPR_DEFAULT + DbBaseHelper.SYMB_APOSTROPHE + defValue + DbBaseHelper.SYMB_APOSTROPHE;
            }
            else {
                sQLExprOfCreation += DbBaseHelper.EXPR_NOT_NULL;
            }

            return sQLExprOfCreation;
        }

        private final String columnName;
        private final String type;
        private final boolean isPrimaryKey;
        private final Object defValue;
    }

    private final String tableName;
    private final HashMap<String, DbTableColumn> dbTableColumns;
}
