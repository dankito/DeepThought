package net.dankito.deepthought.android.extensions

import android.os.Build
import android.view.ViewGroup


fun ViewGroup.MarginLayoutParams?.setLeftMargin(leftMargin: Int) {
    if(this == null) {
        return
    }

    this.leftMargin = leftMargin

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        this.marginStart= leftMargin
    }
}

fun ViewGroup.MarginLayoutParams?.setRightMargin(rightMargin: Int) {
    if(this == null) {
        return
    }

    this.rightMargin = rightMargin

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        this.marginEnd = rightMargin
    }
}