package com.yagodar.android.database.sqlite;

/**
 * Created by Yagodar on 30.08.13.
 */
public class DbTableColumn {
    protected DbTableColumn(String columnName) {
        this(false, columnName, null);
    }

    protected DbTableColumn(String columnName, Object defValue) {
        this(false, columnName, defValue);
    }

    protected DbTableColumn(boolean isPrimaryKey, String columnName) {
        this(isPrimaryKey, columnName, null);
    }

    private DbTableColumn(boolean isPrimaryKey, String columnName, Object defValue) {
        this.isPrimaryKey = isPrimaryKey;
        this.columnName = columnName;
        this.defValue = defValue;

        if(this.isPrimaryKey) {
            type = TYPE_INTEGER;
            exprType = DbBaseHelper.EXPR_TYPE_INTEGER;
            exprDefValue = null;
        }
        else if(defValue != null) {
            if(defValue instanceof Double) {
                type = TYPE_DOUBLE;
                exprType = DbBaseHelper.EXPR_TYPE_REAL;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Float) {
                type = TYPE_FLOAT;
                exprType = DbBaseHelper.EXPR_TYPE_REAL;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Integer || defValue instanceof Byte) {
                type = TYPE_INTEGER;
                exprType = DbBaseHelper.EXPR_TYPE_INTEGER;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Boolean) {
                type = TYPE_BOOLEAN;
                exprType = DbBaseHelper.EXPR_TYPE_INTEGER;
                exprDefValue = String.valueOf((Boolean) this.defValue ? 1 : 0);
            }
            else if(defValue instanceof Long) {
                type = TYPE_LONG;
                exprType = DbBaseHelper.EXPR_TYPE_INTEGER;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Short) {
                type = TYPE_SHORT;
                exprType = DbBaseHelper.EXPR_TYPE_INTEGER;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof String) {
                type = TYPE_STRING;
                exprType = DbBaseHelper.EXPR_TYPE_TEXT;
                exprDefValue = String.valueOf(this.defValue);
            }
            else {
                type = TYPE_NULL;
                exprType = null;
                exprDefValue = null;
            }
        }
        else {
            type = TYPE_NULL;
            exprType = null;
            exprDefValue = null;
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public int getType() {
        return type;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isNull() {
        return type == TYPE_NULL || defValue == null;
    }

    public Object getDefValue() {
        return defValue;
    }

    protected String getSQLExprOfCreation() {
        String sQLExprOfCreation = columnName + exprType;

        if(isPrimaryKey) {
            sQLExprOfCreation += DbBaseHelper.EXPR_PRIMARY_KEY;
        }
        else if(!isNull()) {
            sQLExprOfCreation += DbBaseHelper.EXPR_DEFAULT + DbBaseHelper.SYMB_APOSTROPHE + exprDefValue + DbBaseHelper.SYMB_APOSTROPHE;
        }
        else {
            sQLExprOfCreation += DbBaseHelper.EXPR_NOT_NULL;
        }

        return sQLExprOfCreation;
    }

    private final String columnName;
    private final int type;
    private final String exprType;
    private final boolean isPrimaryKey;
    private final Object defValue;
    private final String exprDefValue;

    public static final int TYPE_NULL = 0;
    public static final int TYPE_BLOB = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_FLOAT = 3;
    public static final int TYPE_INTEGER = 4;
    public static final int TYPE_BOOLEAN = 5;
    public static final int TYPE_LONG = 6;
    public static final int TYPE_SHORT = 7;
    public static final int TYPE_STRING = 8;
}
