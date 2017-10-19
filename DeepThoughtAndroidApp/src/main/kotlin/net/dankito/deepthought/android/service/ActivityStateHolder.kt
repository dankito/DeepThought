package net.dankito.deepthought.android.service

import android.app.Activity


class ActivityStateHolder {

    private val states = HashMap<String, Any?>()


    fun storeState(activity: Class<out Activity>, name: String, state: Any?) {
        states.put(getKey(activity, name), state)
    }

    fun getAndClearState(activity: Class<out Activity>, name: String): Any? {
        return states.remove(getKey(activity, name))
    }


    private fun getKey(activity: Class<out Activity>, name: String): String {
        return activity.name + "_" + name
    }

}