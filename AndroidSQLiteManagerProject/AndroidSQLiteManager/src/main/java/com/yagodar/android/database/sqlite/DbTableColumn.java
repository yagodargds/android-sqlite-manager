package com.yagodar.android.database.sqlite;

/**
 * Created by Yagodar on 30.08.13.
 */
public class DbTableColumn {
    protected DbTableColumn(String columnName) {
        this(false, TYPE_NULL, columnName, null);
    }

    protected DbTableColumn(int type, String columnName) {
        this(false, type, columnName, null);
    }

    protected DbTableColumn(String columnName, Object defValue) {
        this(false, TYPE_NULL, columnName, defValue);
    }

    protected DbTableColumn(boolean isPrimaryKey, String columnName) {
        this(isPrimaryKey, TYPE_NULL, columnName, null);
    }

    private DbTableColumn(boolean isPrimaryKey, int type, String columnName, Object defValue) {
        this.isPrimaryKey = isPrimaryKey;
        this.columnName = columnName;
        this.defValue = defValue;

        if(this.isPrimaryKey) {
            this.type = TYPE_INTEGER;
            exprDefValue = null;
        }
        else if(defValue != null) {
            if(defValue instanceof Double) {
                this.type = TYPE_DOUBLE;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Float) {
                this.type = TYPE_FLOAT;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Integer || defValue instanceof Byte) {
                this.type = TYPE_INTEGER;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Boolean) {
                this.type = TYPE_BOOLEAN;
                exprDefValue = String.valueOf((Boolean) this.defValue ? 1 : 0);
            }
            else if(defValue instanceof Long) {
                this.type = TYPE_LONG;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof Short) {
                this.type = TYPE_SHORT;
                exprDefValue = String.valueOf(this.defValue);
            }
            else if(defValue instanceof String) {
                this.type = TYPE_STRING;
                exprDefValue = String.valueOf(this.defValue);
            }
            else {
                this.type = TYPE_NULL;
                exprDefValue = null;
            }
        }
        else {
            this.type = type;
            exprDefValue = null;
        }

        exprType = EXPR_TYPE[this.type];
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
        else if(exprDefValue != null) {
            sQLExprOfCreation += DbBaseHelper.EXPR_DEFAULT + DbBaseHelper.SYMB_APOSTROPHE + exprDefValue + DbBaseHelper.SYMB_APOSTROPHE;
        }
        else if(!isNull()) {
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

    private static final String EXPR_TYPE[] = new String[] {
            DbBaseHelper.EXPR_TYPE_TEXT,//TODO
            DbBaseHelper.EXPR_TYPE_TEXT,//TODO
            DbBaseHelper.EXPR_TYPE_REAL,
            DbBaseHelper.EXPR_TYPE_REAL,
            DbBaseHelper.EXPR_TYPE_INTEGER,
            DbBaseHelper.EXPR_TYPE_INTEGER,
            DbBaseHelper.EXPR_TYPE_INTEGER,
            DbBaseHelper.EXPR_TYPE_INTEGER,
            DbBaseHelper.EXPR_TYPE_TEXT
    };
}
