package net.dankito.deepthought.android.service

import android.os.Build
import android.text.Html
import android.text.Spanned


class StringUtil {

    fun getSpannedFromHtml(html: String): Spanned {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        }
        else {
            return Html.fromHtml(html)
        }
    }

}