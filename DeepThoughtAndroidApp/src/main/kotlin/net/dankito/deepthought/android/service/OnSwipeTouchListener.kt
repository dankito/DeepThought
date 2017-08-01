package net.dankito.deepthought.android.service

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener


/**
 * Kudos go to Mirek Rusin (https://stackoverflow.com/a/12938787)
 */
class OnSwipeTouchListener(context: Context, private val swipeDetectedListener: (SwipeDirection) -> Unit) : OnTouchListener {

    companion object {
        private val SWIPE_THRESHOLD = 100

        private val SWIPE_VELOCITY_THRESHOLD = 100
    }

    enum class SwipeDirection {
        Top,
        Bottom,
        Right,
        Left
    }


    var singleTapListener: (() -> Unit)? = null
    var doubleTapListener: (() -> Unit)? = null

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
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
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
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