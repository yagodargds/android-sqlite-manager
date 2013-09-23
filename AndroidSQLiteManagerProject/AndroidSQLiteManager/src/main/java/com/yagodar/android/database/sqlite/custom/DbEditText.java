package com.yagodar.android.database.sqlite.custom;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;

import com.yagodar.android.database.sqlite.DbTableBaseManager;
import com.yagodar.android.database.sqlite.DbTableColumn;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by Yagodar on 07.09.13.
 */
public class DbEditText<T extends Object> extends EditText {
    public DbEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        nextFocusViews = new SparseArray<View>();

        addTextChangedListener(new DbEtTextWatcher());
    }

    @Override
    public View focusSearch(int direction) {
        View searchedView = nextFocusViews.get(direction);

        if(searchedView == null) {
            searchedView = super.focusSearch(direction);
        }

        return searchedView;
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
                    Object bigValue;

                    switch(dbTableColumnBase.getType()) {
                        case DbTableColumn.TYPE_DOUBLE:
                            bigValue = new BigDecimal(text);
                            value = ((BigDecimal) bigValue).doubleValue();

                            if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0) {
                                value = Double.MAX_VALUE;
                            }
                            else if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(-Double.MAX_VALUE)) < 0) {
                                value = -Double.MAX_VALUE;
                            }

                            break;
                        case DbTableColumn.TYPE_FLOAT:
                            bigValue = new BigDecimal(text);
                            value = ((BigDecimal) bigValue).floatValue();

                            if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) > 0) {
                                value = Float.MAX_VALUE;
                            }
                            else if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(-Float.MAX_VALUE)) < 0) {
                                value = -Float.MAX_VALUE;
                            }

                            break;
                        case DbTableColumn.TYPE_INTEGER:
                            bigValue = new BigInteger(text);
                            value = ((BigInteger) bigValue).intValue();

                            if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                                value = Integer.MAX_VALUE;
                            }
                            else if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(-Integer.MAX_VALUE)) < 0) {
                                value = -Integer.MAX_VALUE;
                            }

                            break;
                        case DbTableColumn.TYPE_BOOLEAN:
                            value = Boolean.parseBoolean(text);
                            break;
                        case DbTableColumn.TYPE_LONG:
                            bigValue = new BigInteger(text);
                            value = ((BigInteger) bigValue).longValue();

                            if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                                value = Long.MAX_VALUE;
                            }
                            else if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(-Long.MAX_VALUE)) < 0) {
                                value = -Long.MAX_VALUE;
                            }

                            break;
                        case DbTableColumn.TYPE_SHORT:
                            bigValue = new BigInteger(text);
                            value = ((BigInteger) bigValue).shortValue();

                            if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(Short.MAX_VALUE)) > 0) {
                                value = Short.MAX_VALUE;
                            }
                            else if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(-Short.MAX_VALUE)) < 0) {
                                value = -Short.MAX_VALUE;
                            }

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

    public void setNextFocusView(int direction, View view) {
        nextFocusViews.put(direction, view);
    }

    public View getNextFocusView(int direction) {
        return nextFocusViews.get(direction);
    }

    public void clearText() {
        postSetText(EMPTY_TEXT);
    }

    public void postSetText(final String text) {
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

    protected boolean isInputRegistered() {
        return isInputRegistered;
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
    private SparseArray<View> nextFocusViews;

    public static final String EMPTY_TEXT = "";
}
