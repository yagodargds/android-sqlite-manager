package com.yagodar.android.database.sqlite.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.yagodar.android.database.sqlite.R;

/**
 * Created by Yagodar on 30.09.13.
 */
public class CustomLib extends EditText {
    public CustomLib(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomLib,
                0,
                0);

        try {
            Object bla = styledAttrs.getString(R.styleable.CustomLib_customAttr);
            System.out.println(String.valueOf(bla));
        }
        finally {
            styledAttrs.recycle();
        }
    }
}
