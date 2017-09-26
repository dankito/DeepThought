package net.dankito.deepthought.android.views

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.concurrent.schedule


/**
 * When scrolling enters immersive full screen mode, when scrolling stops leaves full screen mode again.
 */
class FullScreenRecyclerView : RecyclerView {


    companion object {
        private const val DELAY_BEFORE_LEAVING_FULLSCREEN_MILLIS = 500L

        private const val NON_READER_MODE_SYSTEM_UI_FLAGS = 0
        private val READER_MODE_SYSTEM_UI_FLAGS: Int


        init {
            READER_MODE_SYSTEM_UI_FLAGS = createReaderModeSystemUiFlags()
        }

        private fun createReaderModeSystemUiFlags(): Int {
            // see https://developer.android.com/training/system-ui/immersive.html
            var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                // even thought View.SYSTEM_UI_FLAG_FULLSCREEN is also available from SDK 16 and above, to my experience it doesn't work reliable (at least not on Android 4.1)
//                flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags = flags or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
            }

            return flags
        }
    }


    var minimumCountItemsToActivateFullscreenMode = 15

    var enterFullscreenModeListener: (() -> Unit)? = null
    var leaveFullscreenModeListener: (() -> Unit)? = null


    private var isInFullScreenMode = false

    private var shouldLeaveFullScreenMode = false

    private val leaveFullscreenModeTimer = Timer()


    constructor(context: Context) : super(context) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setupView()
    }


    private fun setupView() {
        addOnScrollListener(onScrollListener)
    }


    override fun onWindowSystemUiVisibilityChanged(flags: Int) {
        systemUiVisibilityChanged(flags)

        super.onWindowSystemUiVisibilityChanged(flags)
    }

    private fun systemUiVisibilityChanged(flags: Int) {
        if(flags == NON_READER_MODE_SYSTEM_UI_FLAGS) {
            leaveFullScreenModeOnUiThread()
        }
    }

    private fun setShouldLeaveFullScreenMode() {
        shouldLeaveFullScreenMode = true

        leaveFullscreenModeTimer.schedule(DELAY_BEFORE_LEAVING_FULLSCREEN_MILLIS) {
            if(shouldLeaveFullScreenMode) {
                shouldLeaveFullScreenMode = false

                (context as? Activity)?.runOnUiThread { leaveFullScreenModeOnUiThread() }
            }
        }
    }

    private fun leaveFullScreenModeOnUiThread() {
        val lastVisibleItem = (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: -1
        val scrollToEnd = lastVisibleItem >= 0 && lastVisibleItem >= adapter.itemCount - 1

        this.systemUiVisibility = NON_READER_MODE_SYSTEM_UI_FLAGS

        isInFullScreenMode = false

        leaveFullscreenModeListener?.invoke()

        // currently it's not possible to see the last two items as when we're scrolling down it goes to fullscreen, when it snaps back to non fullscreen,
        // last two items are covered again by other views -> when scrolled to end in fullscreen mode show last item after leaving fullscreen
        // but scrolling immediately has no effect has BottomNavigationView and other views aren't displayed yet -> do it delayed
        if(scrollToEnd) {
            scrollToEndDelayed()
        }

    }

    private fun scrollToEndDelayed() {
        postDelayed({
            val targetPosition = if(adapter.itemCount > 0) adapter.itemCount - 1 else 0 // don't scroll to -1, would throw an exception
            smoothScrollToPosition(targetPosition)
        }, 50)
    }

    private fun enterFullScreenModeIfHasEnoughItemsOnUiThread() {
        if(adapter.itemCount >= minimumCountItemsToActivateFullscreenMode) {
            enterFullScreenModeOnUiThread()
        }
    }

    private fun enterFullScreenModeOnUiThread() {
        this.systemUiVisibility = READER_MODE_SYSTEM_UI_FLAGS

        isInFullScreenMode = true

        enterFullscreenModeListener?.invoke()
    }


    private val onScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if(newState == SCROLL_STATE_IDLE) {
                if(isInFullScreenMode) {
                    setShouldLeaveFullScreenMode() // leaving fullscreen immediately when scrolling stops provides very bad user experience when she/he likes to scroll on as it
                    // then leaves fullscreen and immediately enters it again -> leave with some delay
                }
            }
            else if(newState == SCROLL_STATE_DRAGGING) {
                if(shouldLeaveFullScreenMode) {
                    shouldLeaveFullScreenMode = false
                }

                if(isInFullScreenMode == false) {
                    enterFullScreenModeIfHasEnoughItemsOnUiThread()
                }
            }
        }
    }

}
