package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import java.util.*


/**
 * On scroll down fires a listener to enter full screen mode, on scroll up fires the same listener to leave full screen mode.
 */
class FullScreenWebView : WebView {

    enum class FullScreenMode {
        Enter,
        Leave
    }


    companion object {
        private const val DefaultScrollDownDifferenceYThreshold = 3
        private const val DefaultScrollUpDifferenceYThreshold = -200

        private const val AfterTogglingNotHandleScrollEventsForMillis = 500
    }


    var scrollUpDifferenceYThreshold = DefaultScrollUpDifferenceYThreshold
    var scrollDownDifferenceYThreshold = DefaultScrollDownDifferenceYThreshold

    var changeFullScreenModeListener: ((FullScreenMode) -> Unit)? = null


    private var hasEnteredFullScreenMode = false

    private var lastOnScrollReaderModeTogglingTimestamp: Date? = null


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)

        if(hasFullScreenModeToggledShortlyBefore()) {
            return // when toggling reader mode there's a huge jump in scroll difference due to displaying additional / hiding controls -> filter out these events shortly after  entering/leaving reader mode
        }

        val differenceY = scrollY - oldScrollY

        if(hasEnteredFullScreenMode) {
            checkShouldLeaveFullScreenMode(differenceY)
        }
        else {
            checkShouldEnterFullScreenMode(differenceY)
        }
    }

    private fun checkShouldLeaveFullScreenMode(differenceY: Int) {
        if (differenceY < scrollUpDifferenceYThreshold) {
            changeFullScreenModeListener?.invoke(FullScreenMode.Leave)
            hasEnteredFullScreenMode = false
            lastOnScrollReaderModeTogglingTimestamp = Date()
        }
    }

    private fun checkShouldEnterFullScreenMode(differenceY: Int) {
        if (differenceY > scrollDownDifferenceYThreshold) {
            changeFullScreenModeListener?.invoke(FullScreenMode.Enter)
            hasEnteredFullScreenMode = true
            lastOnScrollReaderModeTogglingTimestamp = Date()
        }
    }

    private fun hasFullScreenModeToggledShortlyBefore(): Boolean {
        return Date().time - (lastOnScrollReaderModeTogglingTimestamp?.time ?: 0) < AfterTogglingNotHandleScrollEventsForMillis
    }

}
