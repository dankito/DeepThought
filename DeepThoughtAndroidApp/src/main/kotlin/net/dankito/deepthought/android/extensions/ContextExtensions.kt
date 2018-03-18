package net.dankito.deepthought.android.extensions

import android.content.Context
import android.os.Build


fun Context.getColorFromResourceId(colorResourceId: Int): Int {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return this.getColor(colorResourceId)
    }
    else {
        return this.resources.getColor(colorResourceId)
    }
}


fun Context.getDimension(dimensionResourceId: Int): Int {
    return resources.getDimension(dimensionResourceId).toInt()
}