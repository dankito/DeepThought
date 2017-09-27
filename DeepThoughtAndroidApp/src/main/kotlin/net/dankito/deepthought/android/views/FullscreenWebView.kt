package net.dankito.deepthought.android.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import java.util.*


/**
 * On scroll down fires a listener to enter full screen mode, on scroll up fires the same listener to leave full screen mode.
 */
class FullscreenWebView : WebView {

    enum class FullscreenMode {
        Enter,
        Leave
    }


    companion object {
        private const val DefaultScrollDownDifferenceYThreshold = 3
        private const val DefaultScrollUpDifferenceYThreshold = -10

        private const val HasReachedEndTolerance = 5

        private const val AfterTogglingNotHandleScrollEventsForMillis = 500


        private const val NON_FULLSCREEN_MODE_SYSTEM_UI_FLAGS = 0
        private val FULLSCREEN_MODE_SYSTEM_UI_FLAGS: Int


        init {
            FULLSCREEN_MODE_SYSTEM_UI_FLAGS = createFullscreenModeSystemUiFlags()
        }

        private fun createFullscreenModeSystemUiFlags(): Int {
            // see https://developer.android.com/training/system-ui/immersive.html
            var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                // even thought View.SYSTEM_UI_FLAG_FULLSCREEN is also available from SDK 16 and above, to my experience it doesn't work reliable (at least not on Android 4.1)
                flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags = flags or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
            }

            return flags
        }
    }


    var isInFullscreenMode = false

    var scrollUpDifferenceYThreshold = DefaultScrollUpDifferenceYThreshold
    var scrollDownDifferenceYThreshold = DefaultScrollDownDifferenceYThreshold

    var changeFullscreenModeListener: ((FullscreenMode) -> Unit)? = null


    private var hasReachedEnd = false

    private var lastOnScrollFullscreenModeTogglingTimestamp: Date? = null


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onWindowSystemUiVisibilityChanged(flags: Int) {
        if(flags == 0) {
            isInFullscreenMode = false // otherwise isInFullscreenMode stays true and full screen mode isn't entered anymore on resume
        }

        // as immersive fullscreen is only available for KitKat and above leave immersive fullscreen mode by swiping from screen top or bottom is also only available on these  devices
        if(flags == NON_FULLSCREEN_MODE_SYSTEM_UI_FLAGS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            leaveFullscreenMode()
        }

        super.onWindowSystemUiVisibilityChanged(flags)
    }

    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)

        if(hasFullscreenModeToggledShortlyBefore()) {
            return // when toggling reader mode there's a huge jump in scroll difference due to displaying additional / hiding controls -> filter out these events shortly after  entering/leaving reader mode
        }

        val differenceY = scrollY - oldScrollY

        if(isInFullscreenMode == false) {
            checkShouldEnterFullscreenMode(differenceY)
        }

        this.hasReachedEnd = scrollY >= computeVerticalScrollRange() - computeVerticalScrollExtent() - HasReachedEndTolerance
    }

    private fun checkShouldEnterFullscreenMode(differenceY: Int) {
        if(differenceY > scrollDownDifferenceYThreshold || differenceY < scrollUpDifferenceYThreshold) {
            enterFullscreenMode()
        }
    }


    private fun enterFullscreenMode() {
        isInFullscreenMode = true
        lastOnScrollFullscreenModeTogglingTimestamp = Date()

        changeFullscreenModeListener?.invoke(FullscreenMode.Enter)

        this.systemUiVisibility = FULLSCREEN_MODE_SYSTEM_UI_FLAGS
    }


    private fun leaveFullscreenMode() {
        isInFullscreenMode = false

        changeFullscreenModeListener?.invoke(FullscreenMode.Leave)

        this.systemUiVisibility = NON_FULLSCREEN_MODE_SYSTEM_UI_FLAGS

        if(hasReachedEnd) {
            scrollToEndDelayed()
        }
    }


    private fun hasFullscreenModeToggledShortlyBefore(): Boolean {
        return Date().time - (lastOnScrollFullscreenModeTogglingTimestamp?.time ?: 0) < AfterTogglingNotHandleScrollEventsForMillis
    }


    fun scrollToEndDelayed() {
        postDelayed({
            scrollToEnd()
        }, 50)
    }

    fun scrollToEnd() {
        lastOnScrollFullscreenModeTogglingTimestamp = Date() // we also have to set lastOnScrollFullscreenModeTogglingTimestamp as otherwise scrolling may is large enough to re-enter fullscreen mode
        scrollY = computeVerticalScrollRange() - computeVerticalScrollExtent()
    }

}
