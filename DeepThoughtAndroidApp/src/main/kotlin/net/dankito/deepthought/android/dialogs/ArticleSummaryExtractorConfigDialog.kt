package net.dankito.deepthought.android.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.widget.EditText
import android.widget.ListView
import kotlinx.android.synthetic.main.dialog_article_summary_extractor_config.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorIconsAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.faviconextractor.Favicon
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.faviconextractor.FaviconType
import javax.inject.Inject


class ArticleSummaryExtractorConfigDialog {

    @Inject
    protected lateinit var faviconExtractor: FaviconExtractor

    @Inject
    protected lateinit var faviconComparator: FaviconComparator


    private lateinit var adapter: ArticleSummaryExtractorIconsAdapter


    init {
        AppComponent.component.inject(this)
    }


    fun askForName(activity: Activity, config: ArticleSummaryExtractorConfig, showCancelButton: Boolean, callback: (didSelectName: Boolean) -> Unit) {
        val builder = AlertDialog.Builder(activity)
        builder.setView(R.layout.dialog_article_summary_extractor_config)

        var input: EditText? = null
        var lstIcons: ListView? = null

        if(showCancelButton) {
            builder.setNegativeButton(android.R.string.cancel, { dialog, _ ->
                dialog.cancel()
                callback(false)
            })
        }

        builder.setPositiveButton(android.R.string.ok, { dialog, _ ->
            val selectedIcon = lstIcons?.checkedItemPosition?.let { if(it >= 0) adapter.getItem(it) else null }
            config.name = input?.text?.toString() ?: config.name
            config.iconUrl = selectedIcon?.url
            dialog.cancel()
            callback(true)
        })

        val dialog = builder.create()
        dialog.show()

        input = dialog.edtxtAskExtractorName
        lstIcons = dialog.lstIcons

        setupEditTextName(input, dialog, config.name)

        setupListIcons(activity, lstIcons, dialog, config)
    }


    private fun setupEditTextName(input: EditText, dialog: AlertDialog, currentName: String) {
        input.setText(currentName)

        input.selectAll()

        input.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }


    private fun setupListIcons(activity: Activity, lstIcons: ListView, dialog: AlertDialog, config: ArticleSummaryExtractorConfig) {
        adapter = ArticleSummaryExtractorIconsAdapter(activity)
        lstIcons.adapter = adapter

        getAvailableIcons(config.siteUrl ?: config.url, config.iconUrl) { listIcons, currentIcon ->
            retrievedIcons(activity, lstIcons, listIcons, currentIcon)
        }
    }

    private fun retrievedIcons(activity: Activity, lstIcons: ListView, listIcons: List<Favicon>, currentIcon: Favicon?) {
        val icons = listIcons.sortedBy { it.size }

        activity.runOnUiThread {
            adapter.setItems(icons)
            currentIcon?.let { lstIcons.setItemChecked(icons.indexOf(it), true) } // select current icon
        }
    }

    private fun getAvailableIcons(siteUrl: String, currentIconUrl: String?, callback: (List<Favicon>, Favicon?) -> Unit) {
        val listIcons = ArrayList<Favicon>()
        var currentIcon: Favicon? = null

        currentIconUrl?.let {
            val temp = Favicon(it, FaviconType.Icon) // cause smart cast is not possible
            currentIcon = temp
            listIcons.add(temp)
        }

        faviconExtractor.extractFaviconsAsync(siteUrl) {
            it.result?.let { favicons ->
                mergeFavicons(listIcons, favicons, currentIcon)
            }

            currentIcon = faviconComparator.getBestIcon(listIcons, maxSize = ArticleSummaryExtractorConfigManager.MAX_SIZE, returnSquarishOneIfPossible = true)

            callback(listIcons, currentIcon)
        }
    }

    private fun mergeFavicons(listIcons: MutableList<Favicon>, retrievedFavicons: List<Favicon>, currentIcon: Favicon?) {
        currentIcon?.let { currentIcon -> // check if url of default icon is also contained in favicons
            retrievedFavicons.forEach {
                if(it.url == currentIcon.url) {
                    listIcons.remove(currentIcon)
                }
            }
        }

        listIcons.addAll(retrievedFavicons)
    }

}