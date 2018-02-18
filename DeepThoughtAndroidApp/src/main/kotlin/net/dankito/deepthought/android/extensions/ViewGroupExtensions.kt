package net.dankito.deepthought.android.extensions

import android.view.ViewGroup


fun ViewGroup.setViewsEnabledState(isEnabled: Boolean) {
    for(i in 0 until childCount) {
        val child = getChildAt(i)
        child.isEnabled = isEnabled

        if(child is ViewGroup) {
            child.setViewsEnabledState(isEnabled)
        }
    }
}