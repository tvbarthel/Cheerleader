package fr.tvbarthel.simplesoundcloud.sampleapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Simple LIstView on which top padding could be assigned and enable touch on view under.
 */
public class BottomListView extends ListView {

    public BottomListView(Context context) {
        super(context);
    }

    public BottomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = super.onTouchEvent(ev);
        View child = getChildAt(0);
        if (child == null || ev.getY() < child.getY()) {
            handled = false;
        }
        return handled;
    }
}
