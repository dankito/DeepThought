package net.dankito.deepthought.android.extensions

import android.os.Build
import android.widget.TextView


fun TextView.setTextColorToColorResource(colorResourceId: Int) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.setTextColor(this.context.resources.getColor(colorResourceId, this.context.theme))
    }
    else {
        this.setTextColor(this.context.resources.getColor(colorResourceId))
    }
}