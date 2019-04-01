package com.kivsw.forjoggers.ui.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ivan on 12/5/16.
 */

public class EditText extends AppCompatEditText {

    public EditText(Context context) {
        super(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*@TargetApi(21)
    public EditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    /**
     *
     */
    public interface OnFocusChangedListener
    {
        void onFocusChanged(EditText view, boolean gainFocus);
    };

    /**
     *
     */
    Set<OnFocusChangedListener> onFocusChangedListeners=new HashSet<>();
    public void addOnFocusChangedListener(OnFocusChangedListener listener)
    {
        onFocusChangedListeners.add(listener);
    }
    public boolean removeOnFocusListener(OnFocusChangedListener listener)
    {
        return onFocusChangedListeners.remove(listener);
    }

    @Override
    protected void onFocusChanged (boolean gainFocus, int direction, Rect previouslyFocusedRect)
    {
        super.onFocusChanged(gainFocus,direction,previouslyFocusedRect);
        for(OnFocusChangedListener l:onFocusChangedListeners)
            if(l!=null) l.onFocusChanged(this, gainFocus);
    }






}
