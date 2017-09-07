package net.dankito.deepthought.android.views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import org.slf4j.LoggerFactory


/**
 * So that ViewPager's pages cannot be switched by swiping.
 * (We have swiping enabled on RecyclerView's items, so it would be bad UX if you could do both with swiping, showing an item's details action and switching pages).
 * Thanks to louielouie (https://stackoverflow.com/questions/9650265/how-do-disable-paging-by-swiping-with-finger-in-viewpager-but-still-be-able-to-s)
 */
class NonSwipeableViewPager : ViewPager {

    companion object {
        private val log = LoggerFactory.getLogger(NonSwipeableViewPager::class.java)
    }


    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }


    //down one is added for smooth scrolling

    private fun setMyScroller() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, SmoothScroller(context))
        } catch (e: Exception) {
            log.error("Could not set mScroller field to SmoothScroller", e)
        }

    }

    inner class SmoothScroller(context: Context) : Scroller(context, DecelerateInterpolator()) {

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, 350 /*1 secs*/)
        }

    }

}