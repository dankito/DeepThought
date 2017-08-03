package net.dankito.deepthought.android.service

import android.content.Context
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager


/**
 * Kudos go to Mirek Rusin (https://stackoverflow.com/a/12938787)
 */
class OnSwipeTouchListener(context: Context, private val swipeDetectedListener: (SwipeDirection) -> Unit) : OnTouchListener {

    companion object {
        private val SWIPE_VELOCITY_THRESHOLD = 10000
    }

    enum class SwipeDirection {
        Top,
        Bottom,
        Right,
        Left
    }


    private var swipeThreshold: Int
    private var swipeVelocityThreshold: Int

    var singleTapListener: (() -> Unit)? = null
    var doubleTapListener: (() -> Unit)? = null

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())

        val displayMetrics = DisplayMetrics()
        val windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager // the results will be higher than using the activity context or the getWindowManager() shortcut
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        swipeThreshold = (displayMetrics.widthPixels * 0.5).toInt()

        swipeVelocityThreshold = displayMetrics.widthPixels * 5
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }


    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            singleTapListener?.invoke()

            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            doubleTapListener?.invoke()

            return super.onDoubleTap(e)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                if(Math.abs(diffX) > Math.abs(diffY)) {
                    if(Math.abs(diffX) > swipeThreshold && Math.abs(velocityX) > swipeVelocityThreshold) {
                        if(diffX > 0) {
                            onSwipeRight()
                        }
                        else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                }
                else if(Math.abs(diffY) > swipeThreshold && Math.abs(velocityY) > swipeVelocityThreshold) {
                    if(diffY > 0) {
                        onSwipeBottom()
                    }
                    else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }
    }


    private fun onSwipeRight() {
        swipeDetectedListener(SwipeDirection.Right)
    }

    private fun onSwipeLeft() {
        swipeDetectedListener(SwipeDirection.Left)
    }

    private fun onSwipeTop() {
        swipeDetectedListener(SwipeDirection.Top)
    }

    private fun onSwipeBottom() {
        swipeDetectedListener(SwipeDirection.Bottom)
    }
}