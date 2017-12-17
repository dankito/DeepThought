package net.dankito.deepthought.android.views

import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver


fun View.isTouchInsideView(event: MotionEvent): Boolean {
    return isPointInsideView(event.rawX, event.rawY)
}

/**
 * Determines if given points are inside view
 * @param x - x coordinate of point
 * *
 * @param y - y coordinate of point
 * *
 * @param view - view object to compare
 * *
 * @return true if the points are within view bounds, false otherwise
 */
fun View.isPointInsideView(x: Float, y: Float): Boolean {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    val viewX = location[0]
    val viewY = location[1]

    //point is inside view bounds
    if (x > viewX && x < viewX + this.getWidth() &&
            y > viewY && y < viewY + this.getHeight()) {
        return true
    }
    else {
        return false
    }
}


fun View.executeActionAfterMeasuringHeight(action: () -> Unit) {
    if(this.measuredHeight == 0) { // in this case we have to wait till height is determined -> set OnGlobalLayoutListener
        var layoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null // have to do it that complicated otherwise in OnGlobalLayoutListener we cannot access layoutListener variable
        layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            removeOnGlobalLayoutListener(this, layoutListener)

            action()
        }

        this.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }
    else {
        action()
    }
}

private fun removeOnGlobalLayoutListener(lytContextHelp: View, layoutListener: ViewTreeObserver.OnGlobalLayoutListener?) {
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        lytContextHelp.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
    }
    else {
        lytContextHelp.viewTreeObserver.removeGlobalOnLayoutListener(layoutListener)
    }
}