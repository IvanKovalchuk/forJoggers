package com.kivsw.forjoggers.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by ivan on 01.12.15.
 */
public class CustomPagerView extends android.support.v4.view.ViewPager {

    private boolean enabled= true;

    public interface IonPageAppear
    {
        void onPageAppear();
        void onPageDisappear();
    }


    public CustomPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled)
        {
            return super.onTouchEvent(event);
        }
        return false;
    }

    /**
     * Allows to swipe page only when the user touches near the screen edge
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(event.getPointerCount()==1)
                {
                    int w=getWidth();
                    int min=w*7/100,max=w*93/100; // select 15% from the screen width
                    int x=(int)event.getX();
                    enabled= x<min || x>max;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(event.getPointerCount()==1)
                    enabled=false;
                break;
            case MotionEvent.ACTION_CANCEL:
                enabled=false;
                break;
        }

        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    /*public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }*/
}
