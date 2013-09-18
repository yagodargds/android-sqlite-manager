package com.yagodar.android.database.sqlite.custom;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.yagodar.android.database.sqlite.DbTableBaseManager;
import com.yagodar.android.database.sqlite.DbTableColumn;

/**
 * Created by Yagodar on 07.09.13.
 */
public class DbEditText<T extends Object> extends EditText {
    public DbEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        addTextChangedListener(new DbEtTextWatcher());
    }

    public void initDbManagerBase(DbTableBaseManager dbTableManagerBase, String dbTableColumnNameBase) {
        if(dbTableManagerBase != null) {
            this.dbTableManagerBase = dbTableManagerBase;
            this.dbTableColumnBase = dbTableManagerBase.getTableContract().getDbTableColumn(dbTableColumnNameBase);
        }
    }

    public void setDbRecordId(long recordId) {
        setTag(recordId);
    }

    public long getDbRecordId() {
        long dbRecordId = -1;

        Object tag = getTag();

        if(tag != null && tag instanceof Long) {
            dbRecordId = (Long) getTag();
        }

        return dbRecordId;
    }

    public void pushToDb() {
        if(dbTableColumnBase != null && isInputRegistered()) {
            String text = getText().toString();

            if(text.length() > 0) {
                try {
                    Object value;

                    switch(dbTableColumnBase.getType()) {
                        case DbTableColumn.TYPE_DOUBLE:
                            value = Double.parseDouble(text);
                            break;
                        case DbTableColumn.TYPE_FLOAT:
                            value = Float.parseFloat(text);
                            break;
                        case DbTableColumn.TYPE_INTEGER:
                            value = Integer.parseInt(text);
                            break;
                        case DbTableColumn.TYPE_BOOLEAN:
                            value = Boolean.parseBoolean(text);
                            break;
                        case DbTableColumn.TYPE_LONG:
                            value = Long.parseLong(text);
                            break;
                        case DbTableColumn.TYPE_SHORT:
                            value = Short.parseShort(text);
                            break;
                        default:
                            value = text;
                            break;
                    }

                    setDbValue(value);
                }
                catch(Exception ignored){}
            }
            else {
                setDbValue(dbTableColumnBase.getDefValue());
            }
        }
    }

    public void setDbValue(Object value) {
        if(dbTableManagerBase != null && dbTableColumnBase != null) {
            long dbRecordId = getDbRecordId();
            if(dbRecordId != -1) {
                dbTableManagerBase.setColumnValue(dbRecordId, dbTableColumnBase.getColumnName(), value);
            }
        }
    }

    public void pullFromDb() {
        postSetText(String.valueOf(getDbValue()));
    }

    public T getDbValue() {
        T value = null;

        if(dbTableManagerBase != null && dbTableColumnBase != null) {
            long dbRecordId = getDbRecordId();
            if(dbRecordId != -1) {
                try {
                    value = (T) dbTableManagerBase.getColumnValue(dbRecordId, dbTableColumnBase.getColumnName());
                }
                catch(Exception ignored) {}
            }
        }

        return value;
    }

    public void resetInputRegistered() {
        isInputRegistered = false;
    }

    protected boolean isInputRegistered() {
        return isInputRegistered;
    }

    protected void clearText() {
        postSetText(EMPTY_TEXT);
    }

    protected void postSetText(final String text) {
        try {
            post(new Runnable() {
                @Override
                public void run() {
                    setText(text);
                }
            });
        }
        catch(Exception ignored) {}
    }

    private class DbEtTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isInputRegistered = true;
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private DbTableBaseManager dbTableManagerBase;
    private DbTableColumn dbTableColumnBase;
    private boolean isInputRegistered;

    private static final String EMPTY_TEXT = "";
}
