package net.dankito.deepthought.android.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Ugly, real ugly fix to circumvent a Bug on some devices like Wiko Sunny:
 * There surfaceView.getLeft() returns 0 even though surfaceView has already been swiped to left (-> getLeft() < 0).
 * Therefore {@link #getOpenStatus()} returns {@see com.daimajia.swipe.SwipeLayout.Status.Close} instead of Middle.
 */
public class FixedSwipeLayout extends com.daimajia.swipe.SwipeLayout {

    private Field mIsBeingDraggedField;


    public FixedSwipeLayout(Context context) {
        super(context);

        init();
    }

    public FixedSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FixedSwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }


    private void init() {
        try {
            mIsBeingDraggedField = com.daimajia.swipe.SwipeLayout.class.getDeclaredField("mIsBeingDragged");
            mIsBeingDraggedField.setAccessible(true);
        } catch(Exception ignored) { }
    }

    protected boolean isBeingDragged() {
        if(mIsBeingDraggedField != null) {
            try {
                return (boolean)mIsBeingDraggedField.get(this);
            } catch(Exception ignored) { }
        }

        return false;
    }


    @Override
    public Status getOpenStatus() {
        Status newStatus = Status.Middle;

        View surfaceView = getSurfaceView();
        if(surfaceView == null) {
            newStatus = Status.Close;
        }
        else {
            int surfaceLeft = surfaceView.getLeft();
            int surfaceTop = surfaceView.getTop();

            if (surfaceLeft == getPaddingLeft() && surfaceTop == getPaddingTop() && isBeingDragged() == false) {
                newStatus = Status.Close;
            }
            else if (surfaceLeft == (getPaddingLeft() - getDragDistance()) || surfaceLeft == (getPaddingLeft() + getDragDistance())
                || surfaceTop == (getPaddingTop() - getDragDistance()) || surfaceTop == (getPaddingTop() + getDragDistance())) {
                newStatus = Status.Open;
            }
        }

        return newStatus;
    }

}
