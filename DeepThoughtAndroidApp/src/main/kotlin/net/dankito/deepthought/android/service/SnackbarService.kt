package net.dankito.deepthought.android.service

import android.app.Activity
import android.net.Uri
import android.support.design.internal.SnackbarContentLayout
import android.support.design.widget.Snackbar
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.extensions.getColorFromResourceId
import net.dankito.deepthought.android.extensions.setTintColor
import net.dankito.deepthought.ui.IRouter
import org.slf4j.LoggerFactory
import javax.inject.Inject


class SnackbarService {

    companion object {
        private val log = LoggerFactory.getLogger(SnackbarService::class.java)
    }


    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker

    @Inject
    protected lateinit var router: IRouter


    private var currentSnackbar: Snackbar? = null


    init {
        AppComponent.component.inject(this)
    }


    fun showUrlInClipboardDetectedSnackbar(activity: Activity, url: String, actionInvokedListener: () -> Unit) {
        try {
            val uri = Uri.parse(url)
            var host = uri.host
            if(host.startsWith("www.")) {
                host = host.substring(4)
            }

            val text = activity.getString(R.string.snackbar_extract_item_from_url, host)
            showSnackbar(text, activity, { customizeUrlInClipboardDetectedSnackbar(activity, it) }, actionInvokedListener)
        } catch(e: Exception) { log.error("Could not show snackbar for Clipboard url $url", e) }
    }

    private fun customizeUrlInClipboardDetectedSnackbar(activity: Activity, snackbar: Snackbar) {
        val textColor = activity.getColorFromResourceId(R.color.colorPrimary)
        snackbar.setActionTextColor(textColor)

        (snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as? TextView)?.let { txtvwSnackbarStandardText ->
            txtvwSnackbarStandardText.setTextColor(textColor)
            txtvwSnackbarStandardText.maxLines = 4
        }

        val icon = ImageView(activity)
        icon.setImageResource(R.drawable.ic_help_outline_white_48dp)
        icon.setTintColor(R.color.colorPrimary)

        addIconToSnackbar(snackbar, icon)
    }


    private fun showSnackbar(text: String, activity: Activity, customizeSnackbarListener: ((Snackbar) -> Unit)? = null, actionInvokedListener: () -> Unit) {
        var rootView = activity.findViewById(R.id.content_layout_root) // content_layout_root only works for MainActivity -> find a generic solution
        if(rootView == null) {
            rootView = activity.findViewById(android.R.id.content)
        }

        synchronized(this) {
            currentSnackbar = Snackbar.make(rootView, text, Snackbar.LENGTH_INDEFINITE)
        }

        currentSnackbar?.let { snackbar ->
            snackbar.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar, event: Int) {
                    snackbarDismmissed()
                }
            })

            snackbar.setAction(android.R.string.ok) { actionInvokedListener() }

            customizeSnackbarListener?.invoke(snackbar)

            snackbar.show()
        }
    }

    private fun snackbarDismmissed() {
        synchronized(this) {
            currentSnackbar = null
        }
    }

    private fun addIconToSnackbar(snackbar: Snackbar, icon: ImageView) {
        (snackbar.view as? Snackbar.SnackbarLayout)?.let { snackbarLayout ->
            for (i in 0..snackbarLayout.childCount - 1) {
                val child = snackbarLayout.getChildAt(i)
                if (child is SnackbarContentLayout) {
                    child.addView(icon, 0)

                    (icon.layoutParams as? LinearLayout.LayoutParams)?.let { layoutParams ->
                        layoutParams.gravity = Gravity.CENTER_VERTICAL
                    }
                }
            }
        }
    }

}