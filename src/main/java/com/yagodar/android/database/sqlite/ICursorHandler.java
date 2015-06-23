package com.yagodar.android.database.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

/**
 * Created by yagodar on 22.06.2015.
 */
public interface ICursorHandler {
    void handle(Cursor cs) throws SQLiteException;
}
