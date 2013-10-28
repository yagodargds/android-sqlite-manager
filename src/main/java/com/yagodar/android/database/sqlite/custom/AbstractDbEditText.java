package com.yagodar.android.database.sqlite.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;

import com.yagodar.android.database.sqlite.DbTableBaseManager;
import com.yagodar.android.database.sqlite.DbTableColumn;
import com.yagodar.android.database.sqlite.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Created by Yagodar on 07.09.13.
 */
public abstract class AbstractDbEditText<T extends Object> extends EditText {
    public AbstractDbEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        nextFocusViews = new SparseArray<View>();

        CharSequence initHintSequence = getHint();
        if(initHintSequence != null) {
            initHint = initHintSequence.toString();
        }

        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AbstractDbEditText,
                0,
                0);

        String tableName;
        String tableColumnName;
        String defValueStr;
        String minValueStr;
        String maxValueStr;
        int minFractionDigits;
        int maxFractionDigits;

        try {
            tableName = styledAttrs.getString(R.styleable.AbstractDbEditText_tableName);
            tableColumnName = styledAttrs.getString(R.styleable.AbstractDbEditText_tableColumnName);
            valueType = styledAttrs.getInteger(R.styleable.AbstractDbEditText_valueType, VALUE_TYPE_TEXT);
            minFractionDigits = styledAttrs.getInteger(R.styleable.AbstractDbEditText_minFractionDigits, DEF_MIN_FRACTION_DIGITS);
            maxFractionDigits = styledAttrs.getInteger(R.styleable.AbstractDbEditText_maxFractionDigits, DEF_MAX_FRACTION_DIGITS);
            defValueStr = styledAttrs.getString(R.styleable.AbstractDbEditText_defValueStr);
            minValueStr = styledAttrs.getString(R.styleable.AbstractDbEditText_minValueStr);
            maxValueStr = styledAttrs.getString(R.styleable.AbstractDbEditText_maxValueStr);
            hintTypeface = styledAttrs.getInteger(R.styleable.AbstractDbEditText_hintTypeface, Typeface.NORMAL);
            hintShowDefValue = styledAttrs.getBoolean(R.styleable.AbstractDbEditText_hintShowDefValue, DEF_HINT_SHOW_DEF_VALUE);
        }
        finally {
            styledAttrs.recycle();
        }

        decimalFormat = new DecimalFormat();
        decimalFormat.setMinimumFractionDigits(minFractionDigits);
        decimalFormat.setMaximumFractionDigits(maxFractionDigits);
        decimalFormat.setGroupingUsed(false);

        DecimalFormatSymbols custom = new DecimalFormatSymbols();
        custom.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(custom);

        registerDatabase(tableName, tableColumnName);

        defValue = parseStringToValue(defValueStr);
        minValue = parseStringToValue(minValueStr);
        maxValue = parseStringToValue(maxValueStr);
    }

    @Override
    public View focusSearch(int direction) {
        View searchedView = nextFocusViews.get(direction);

        if(searchedView == null) {
            searchedView = super.focusSearch(direction);
        }

        return searchedView;
    }

    public void setRecordId(long recordId) {
        setTag(recordId);
        setValue(getValue());
    }

    public long getRecordId() {
        long dbRecordId = -1;

        Object tag = getTag();

        if(tag != null && tag instanceof Long) {
            dbRecordId = (Long) getTag();
        }

        return dbRecordId;
    }

    public void setDefValue(String defValueStr) {
        Object newDefValue = parseStringToValue(defValueStr);
        if(newDefValue != null) {
            updateDbValue(defValue, newDefValue);
            defValue = newDefValue;
        }
    }

    public void pushToDb() {
        setValue(parseStringToValue(getText().toString()));
    }

    public void setColumnValue(Object value) {
        if(tableManager != null && tableColumn != null) {
            long dbRecordId = getRecordId();
            if(dbRecordId != -1) {
                if(value != null && tableColumn.getType() == DbTableColumn.TYPE_STRING) {
                    value = String.valueOf(value);
                }
                tableManager.setColumnValue(dbRecordId, tableColumn.getColumnName(), value);
            }
        }
    }

    public void pullFromDb() {
        T dbValue = getValue();

        if(dbValue == null || ((initHint != null || hintShowDefValue) && dbValue.equals(defValue))) {
            clearText();
        }
        else {
            postSetText(parseValueToString(dbValue));
        }
    }

    public T getValue() {
        Object dbValue = null;

        if(tableManager != null && tableColumn != null) {
            long dbRecordId = getRecordId();
            if(dbRecordId != -1) {
                try {
                    dbValue = tableManager.getColumnValue(dbRecordId, tableColumn.getColumnName());

                    if(tableColumn.getType() == DbTableColumn.TYPE_STRING) {
                        dbValue = parseTypeStringValue((String) dbValue);
                    }
                }
                catch(Exception ignored) {}
            }
        }

        return (T) dbValue;
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

    public void syncValue() {
        pushToDb();
        pullFromDb();
    }

    public void syncValue(Object value) {
        setValue(value);
        pullFromDb();
    }

    protected DbTableBaseManager getTableManager() {
        return tableManager;
    }

    protected DbTableColumn getTableColumn() {
        return tableColumn;
    }

    protected void registerDatabase(String tableName, String tableColumnName) {
        tableManager = registerTableManager(tableName);

        if(tableManager != null) {
            tableColumn = tableManager.getTableContract().getDbTableColumn(tableColumnName);
        }

        postUpdateHint();
    }

    abstract protected DbTableBaseManager registerTableManager(String tableName);

    private void updateDbValue(Object oldDefValue, Object newDefValue) {
        if(newDefValue != null && (oldDefValue == null || compareValues(getValue(), oldDefValue) == 0 && !oldDefValue.equals(newDefValue))) {
            setValue(newDefValue);
        }
    }

    private void setValue(Object value) {
        if(tableColumn != null) {
            Object defDbValue = parseStringToValue(parseValueToString(tableColumn.getDefValue()));
            if(value == null || value.equals(defDbValue)) {
                if(defValue == null) {
                    defValue = defDbValue;
                }

                value = defValue;
            }

            if(compareValues(value, minValue) == -1) {
                value = minValue;
            }

            if(compareValues(value, maxValue) == 1) {
                value = maxValue;
            }

            setColumnValue(value);
        }
    }

    private Object parseStringToValue(String valueStr) {
        Object value = null;

        if(tableColumn != null && valueStr != null && valueStr.length() > 0) {
            try {
                Object bigValue;

                switch(tableColumn.getType()) {
                    case DbTableColumn.TYPE_DOUBLE:
                        bigValue = new BigDecimal(valueStr);
                        value = ((BigDecimal) bigValue).doubleValue();

                        if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0) {
                            value = Double.MAX_VALUE;
                        }
                        else if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(-Double.MAX_VALUE)) < 0) {
                            value = -Double.MAX_VALUE;
                        }

                        break;
                    case DbTableColumn.TYPE_FLOAT:
                        bigValue = new BigDecimal(valueStr);
                        value = ((BigDecimal) bigValue).floatValue();

                        if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) > 0) {
                            value = Float.MAX_VALUE;
                        }
                        else if(((BigDecimal) bigValue).compareTo(BigDecimal.valueOf(-Float.MAX_VALUE)) < 0) {
                            value = -Float.MAX_VALUE;
                        }

                        break;
                    case DbTableColumn.TYPE_INTEGER:
                        bigValue = new BigInteger(valueStr);
                        value = ((BigInteger) bigValue).intValue();

                        if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                            value = Integer.MAX_VALUE;
                        }
                        else if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(-Integer.MAX_VALUE)) < 0) {
                            value = -Integer.MAX_VALUE;
                        }

                        break;
                    case DbTableColumn.TYPE_BOOLEAN:
                        value = Boolean.parseBoolean(valueStr);
                        break;
                    case DbTableColumn.TYPE_LONG:
                        bigValue = new BigInteger(valueStr);
                        value = ((BigInteger) bigValue).longValue();

                        if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                            value = Long.MAX_VALUE;
                        }
                        else if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(-Long.MAX_VALUE)) < 0) {
                            value = -Long.MAX_VALUE;
                        }

                        break;
                    case DbTableColumn.TYPE_SHORT:
                        bigValue = new BigInteger(valueStr);
                        value = ((BigInteger) bigValue).shortValue();

                        if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(Short.MAX_VALUE)) > 0) {
                            value = Short.MAX_VALUE;
                        }
                        else if(((BigInteger) bigValue).compareTo(BigInteger.valueOf(-Short.MAX_VALUE)) < 0) {
                            value = -Short.MAX_VALUE;
                        }

                        break;
                    case DbTableColumn.TYPE_STRING:
                        value = parseTypeStringValue(valueStr);
                        break;
                    default:
                        value = valueStr;
                        break;
                }
            }
            catch(Exception ignored){}
        }

        return value;
    }

    private String parseValueToString(Object value) {
        String parsedValue = String.valueOf(value);

        if(tableColumn != null) {
            try {
                switch(tableColumn.getType()) {
                    case DbTableColumn.TYPE_DOUBLE:
                    case DbTableColumn.TYPE_FLOAT:
                        parsedValue = decimalFormat.format(value);
                        break;
                    case DbTableColumn.TYPE_STRING:
                        switch(valueType) {
                            case VALUE_TYPE_INTEGER:
                                parsedValue = String.valueOf(value);
                                break;
                            case VALUE_TYPE_REAL:
                                parsedValue = decimalFormat.format(value);
                                break;
                            case VALUE_TYPE_TEXT:
                            default:
                                parsedValue = String.valueOf(value);
                                break;
                        }

                        break;
                    default:
                        parsedValue = String.valueOf(value);
                        break;
                }
            }
            catch(Exception ignored) {}
        }

        return parsedValue;
    }

    private Object parseTypeStringValue(String typeStringValueStr) {
        Object value = null;

        if(typeStringValueStr != null && typeStringValueStr.length() > 0) {
            try {
                switch(valueType) {
                    case VALUE_TYPE_INTEGER:
                        value = new BigInteger(typeStringValueStr);
                        break;
                    case VALUE_TYPE_REAL:
                        value = new BigDecimal(typeStringValueStr);
                        break;
                    case VALUE_TYPE_TEXT:
                    default:
                        value = typeStringValueStr;
                        break;
                }
            }
            catch(Exception ignored){}
        }

        return value;
    }

    private int compareValues(Object value, Object compareToValue) {
        int result = -2;

        if(value != null && compareToValue != null) {
            try {
                switch(tableColumn.getType()) {
                    case DbTableColumn.TYPE_DOUBLE:
                        result = ((Double) value).compareTo((Double) compareToValue);
                        break;
                    case DbTableColumn.TYPE_FLOAT:
                        result = ((Float) value).compareTo((Float) compareToValue);
                        break;
                    case DbTableColumn.TYPE_INTEGER:
                        result = ((Integer) value).compareTo((Integer) compareToValue);
                        break;
                    case DbTableColumn.TYPE_BOOLEAN:
                        result = ((Boolean) value).compareTo((Boolean) compareToValue);
                        break;
                    case DbTableColumn.TYPE_LONG:
                        result = ((Long) value).compareTo((Long) compareToValue);
                        break;
                    case DbTableColumn.TYPE_SHORT:
                        result = ((Short) value).compareTo((Short) compareToValue);
                        break;
                    case DbTableColumn.TYPE_STRING:
                        switch(valueType) {
                            case VALUE_TYPE_INTEGER:
                                result = ((BigInteger) value).compareTo((BigInteger) compareToValue);
                                break;
                            case VALUE_TYPE_REAL:
                                result = ((BigDecimal) value).compareTo((BigDecimal) compareToValue);
                                break;
                            case VALUE_TYPE_TEXT:
                            default:
                                result = ((String) value).compareTo((String) compareToValue);
                                break;
                        }

                        break;
                    default:
                        if(value.equals(compareToValue)) {
                            result = 0;
                        }
                        break;
                }
            }
            catch(Exception ignored){}
        }

        return result;
    }

    private void updateHint() {
        String updatedHint = EMPTY_TEXT;

        if(hintShowDefValue) {
            if(defValue == null && tableColumn != null) {
                defValue = tableColumn.getDefValue();
            }

            updatedHint = HINT_DB_VALUE_SEP + parseValueToString(defValue);
        }

        if(initHint != null && initHint.length() > 0) {
            updatedHint = initHint + updatedHint;
        }
        else if(hintShowDefValue) {
            updatedHint = updatedHint.replaceAll(HINT_DB_VALUE_SEP, EMPTY_TEXT);
        }

        setHint(getTypefacedHtmlText(hintTypeface, updatedHint));
    }

    private static CharSequence getTypefacedHtmlText(int typeface, String initText) {
        if(initText == null || typeface < 0 || typeface >= TYPEFACE_TAG.length) {
            return null;
        }

        return Html.fromHtml(TYPEFACE_TAG[typeface] + initText + TYPEFACE_TAG[typeface].replaceAll(TAG_OPENING, TAG_OPENING + TAG_END_OPENING));
    }

    private void postUpdateHint() {
        try {
            post(new Runnable() {
                @Override
                public void run() {
                    updateHint();
                }
            });
        }
        catch(Exception ignored) {}
    }

    private SparseArray<View> nextFocusViews;
    private String initHint;
    private DecimalFormat decimalFormat;

    private DbTableBaseManager tableManager;
    private DbTableColumn tableColumn;
    private int valueType;
    private Object defValue;
    private Object minValue;
    private Object maxValue;
    private int hintTypeface;
    private boolean hintShowDefValue;

    public static final String EMPTY_TEXT = "";

    private static final int VALUE_TYPE_TEXT = 0;
    private static final int VALUE_TYPE_INTEGER = 1;
    private static final int VALUE_TYPE_REAL = 2;

    private static final String HINT_DB_VALUE_SEP = ":";

    private static final String TAG_OPENING = "<";
    private static final String TAG_END_OPENING = "/";
    private static final String TAG_CLOSING = ">";

    private static final String TAG_TYPEFACE_BOLD = "b";
    private static final String TAG_TYPEFACE_ITALIC = "i";
    private static final String TAG_TYPEFACE_BOLD_ITALIC = TAG_TYPEFACE_BOLD + TAG_CLOSING + TAG_OPENING + TAG_TYPEFACE_ITALIC;

    private static final String TYPEFACE_TAG[] = new String[] {
            EMPTY_TEXT,                                             //Typeface.NORMAL
            TAG_OPENING + TAG_TYPEFACE_BOLD + TAG_CLOSING,          //Typeface.BOLD
            TAG_OPENING + TAG_TYPEFACE_ITALIC + TAG_CLOSING,        //Typeface.ITALIC
            TAG_OPENING + TAG_TYPEFACE_BOLD_ITALIC + TAG_CLOSING,   //Typeface.BOLD_ITALIC
    };

    private static boolean DEF_HINT_SHOW_DEF_VALUE = true;
    private static int DEF_MIN_FRACTION_DIGITS = 2;
    private static int DEF_MAX_FRACTION_DIGITS = DEF_MIN_FRACTION_DIGITS;
}
