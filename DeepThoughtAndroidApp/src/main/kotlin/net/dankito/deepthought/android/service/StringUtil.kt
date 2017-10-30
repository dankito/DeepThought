package net.dankito.deepthought.android.service

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned


class StringUtil {

    /**
     * context is only needed if html contains images.
     * tintColorResourceId is only needed if you want to colorize the images in html
     */
    fun getSpannedFromHtml(html: String, context: Context? = null, tintColorResourceId: Int? = null): Spanned {
        val imageGetter = if(context != null) HtmlLocalResourceImageGetter(context, tintColorResourceId) else null

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
        }
        else {
            return Html.fromHtml(html, imageGetter, null)
        }
    }

}