package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
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
    }


    var scrollUpDifferenceYThreshold = DefaultScrollUpDifferenceYThreshold
    var scrollDownDifferenceYThreshold = DefaultScrollDownDifferenceYThreshold

    var hasReachedEnd = false

    var changeFullscreenModeListener: ((FullscreenMode) -> Unit)? = null


    private var isInFullscreenMode = false

    private var lastOnScrollFullscreenModeTogglingTimestamp: Date? = null


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onWindowSystemUiVisibilityChanged(visible: Int) {
        if(visible == 0) {
            isInFullscreenMode = false // otherwise isInFullscreenMode stays true and full screen mode isn't entered anymore on resume
        }

        super.onWindowSystemUiVisibilityChanged(visible)
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
        changeFullscreenModeListener?.invoke(FullscreenMode.Enter)
        isInFullscreenMode = true
        lastOnScrollFullscreenModeTogglingTimestamp = Date()
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
