package com.yagodar.android.database.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yagodar on 13.08.13.
 */
public class DbHelper extends SQLiteOpenHelper {
    protected DbHelper(AbstractDbManager manager, Context context, String dbName, SQLiteDatabase.CursorFactory csFactory, int dbVersion) {
        super(context, dbName, csFactory, dbVersion);
        mManager = manager;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (DbTableManager dbTableManager : getManager().getAllTableManagers()) {
            db.execSQL(dbTableManager.getSQLExprCreateTable());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (DbTableManager dbTableManager : getManager().getAllTableManagers()) {
            db.execSQL(dbTableManager.getSQLExprDeleteTable());
        }

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    protected AbstractDbManager getManager() {
        return mManager;
    }

    private final AbstractDbManager mManager;

    public static final String SYMB_OP_EQUALITY = "=";

    public static final String SYMB_BRACKET_OPEN = "(";
    public static final String SYMB_BRACKET_CLOSE = ")";

    public static final String SYMB_COMMA = ",";
    public static final String SYMB_DOT_COMMA = ";";
    public static final String SYMB_APOSTROPHE = "'";

    public static final String EXPR_TYPE_TEXT = " TEXT";
    public static final String EXPR_TYPE_INTEGER = " INTEGER";
    public static final String EXPR_TYPE_REAL = " REAL";

    public static final String EXPR_CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
    public static final String EXPR_DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    public static final String EXPR_PRIMARY_KEY = " PRIMARY KEY";
    public static final String EXPR_NOT_NULL = " NOT NULL";
    public static final String EXPR_DEFAULT = " DEFAULT ";
}
