package net.dankito.deepthought.android.service

import net.dankito.deepthought.android.activities.BaseActivity
import java.util.concurrent.CopyOnWriteArrayList


class CurrentActivityTracker {

    var currentActivity: BaseActivity? = null
        set(value) {
            field = value

            if(value != null) {
                callAndClearNextActivitySetListeners(value)
            }
        }


    private val nextActivitySetListeners = CopyOnWriteArrayList<(BaseActivity) -> Unit>()

    fun addNextActivitySetListener(listener: (BaseActivity) -> Unit) {
        synchronized(nextActivitySetListeners) {
            nextActivitySetListeners.add(listener)
        }
    }

    private fun callAndClearNextActivitySetListeners(activity: BaseActivity) {
        synchronized(nextActivitySetListeners) {
            nextActivitySetListeners.forEach { it(activity) }

            nextActivitySetListeners.clear()
        }
    }

}