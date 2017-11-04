package net.dankito.deepthought.android.service

import net.dankito.deepthought.android.activities.BaseActivity
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.schedule


class CurrentActivityTracker {

    var currentActivity: BaseActivity? = null
        set(value) {
            field = value // TODO: check field != value

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
            val listenersCopy = ArrayList(nextActivitySetListeners)

            nextActivitySetListeners.clear()

            Timer().schedule(500) { // wait some time till activity is initialized
                listenersCopy.forEach { it(activity) }
            }
        }
    }

}