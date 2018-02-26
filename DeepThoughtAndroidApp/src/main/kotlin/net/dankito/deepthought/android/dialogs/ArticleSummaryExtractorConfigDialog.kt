package net.dankito.deepthought.android.dialogs

import android.app.Activity
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.widget.EditText
import android.widget.ListView
import kotlinx.android.synthetic.main.dialog_article_summary_extractor_config.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorIconsAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.extensions.extractor
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.presenter.ArticleSummaryExtractorConfigPresenter
import net.dankito.faviconextractor.Favicon
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.faviconextractor.FaviconType
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractor
import net.dankito.util.ui.dialog.IDialogService
import net.dankito.util.ui.dialog.ConfirmationDialogButton
import javax.inject.Inject


class ArticleSummaryExtractorConfigDialog {

    @Inject
    protected lateinit var extractorsConfigManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var faviconExtractor: FaviconExtractor

    @Inject
    protected lateinit var faviconComparator: FaviconComparator

    @Inject
    protected lateinit var dialogService: IDialogService


    private lateinit var adapter: ArticleSummaryExtractorIconsAdapter

    private val presenter: ArticleSummaryExtractorConfigPresenter


    init {
        AppComponent.component.inject(this)

        presenter = ArticleSummaryExtractorConfigPresenter(extractorsConfigManager)
    }


    fun editConfiguration(activity: Activity, config: ArticleSummaryExtractorConfig, callback: (didEditConfiguration: Boolean) -> Unit) {
        val builder = AlertDialog.Builder(activity)
        builder.setView(R.layout.dialog_article_summary_extractor_config)

        var input: EditText? = null
        var lstIcons: ListView? = null

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
            callback(false)
        }

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            val selectedIcon = lstIcons?.checkedItemPosition?.let { if(it >= 0) adapter.getItem(it) else null }
            config.name = input?.text?.toString() ?: config.name
            config.iconUrl = selectedIcon?.url
            saveConfig(config, dialog, activity, callback)
        }

        if(config.extractor is IImplementedArticleSummaryExtractor == false && config.isPersisted() == true) {
            builder.setNeutralButton(R.string.action_delete) { dialog, _ ->
                deleteConfig(config, dialog, activity)
            }
        }

        val dialog = builder.create()
        dialog.show()

        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) // so that keyboard doesn't cover OK and Cancel buttons

        input = dialog.edtxtAskExtractorName
        lstIcons = dialog.lstIcons

        setupEditTextName(input, dialog, config.name)

        setupListIcons(activity, lstIcons, config)

        setupButtons(activity, dialog)
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

    private fun setupButtons(activity: Activity, dialog: AlertDialog) {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.let { negativeButton ->
            negativeButton.setTextColor(activity.resources.getColor(R.color.alert_cancel_button_text_color))
        }

        dialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.let { neutralButton ->
            neutralButton.setTextColor(activity.resources.getColor(R.color.alert_delete_button_text_color))
        }
    }


    private fun setupListIcons(activity: Activity, lstIcons: ListView, config: ArticleSummaryExtractorConfig) {
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
            currentIcon?.let {
                lstIcons.setItemChecked(icons.indexOf(it), true)
                lstIcons.smoothScrollToPosition(icons.indexOf(currentIcon))
            }
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

            // to retrieve all icon sizes
            val bestIcon = faviconComparator.getBestIcon(listIcons, maxSize = ArticleSummaryExtractorConfigManager.MAX_SIZE, returnSquarishOneIfPossible = true)
            if(preferOverCurrentIcon(bestIcon, currentIcon, ArticleSummaryExtractorConfigManager.MAX_SIZE)) {
                currentIcon = bestIcon
            }

            callback(listIcons, currentIcon)
        }
    }

    private fun preferOverCurrentIcon(otherIcon: Favicon?, currentIcon: Favicon?, maxSize: Int): Boolean {
        otherIcon?.let {
            if(currentIcon == null) {
                return true
            }

            if(otherIcon.size?.isSquare() == true && currentIcon.size?.isSquare() == false) {
                return true
            }
            else if(otherIcon.size?.isSquare() == false && currentIcon.size?.isSquare() == true) {
                return false
            }

            return Math.abs((otherIcon.size?.width ?: 0) - maxSize) < Math.abs((currentIcon.size?.width ?: 0) - maxSize) // get that one that is closest to maxSize
        }

        return false
    }

    private fun mergeFavicons(listIcons: MutableList<Favicon>, retrievedFavicons: List<Favicon>, currentIcon: Favicon?) {
        listIcons.addAll(retrievedFavicons)

        currentIcon?.let { currentIcon -> // check if url of default icon is also contained in favicons
            retrievedFavicons.forEach {
                if(it.url == currentIcon.url) {
                    listIcons.remove(it)
                }
            }
        }
    }


    private fun saveConfig(config: ArticleSummaryExtractorConfig, dialog: DialogInterface, activity: Activity, callback: (didEditConfiguration: Boolean) -> Unit) {
        presenter.saveAsync(config) {
            activity.runOnUiThread { dialog.cancel() }

            callback(it)
        }
    }

    private fun deleteConfig(config: ArticleSummaryExtractorConfig, dialog: DialogInterface, activity: Activity) {
        dialogService.showConfirmationDialog(activity.getString(R.string.dialog_article_summary_extractor_alert_message_delete_config, config.name)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                presenter.deleteConfigAsync(config) {
                    activity.runOnUiThread { dialog.cancel() }
                }
            }
        }
    }

}