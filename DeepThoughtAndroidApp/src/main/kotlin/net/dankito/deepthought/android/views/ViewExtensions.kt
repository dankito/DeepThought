package net.dankito.deepthought.android.views

import android.view.MotionEvent
import android.view.View


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