package net.dankito.deepthought.android.dialogs

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_article_summary_extractors.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.ArticleSummaryExtractorsAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class ArticleSummaryExtractorsDialog(private val activity: AppCompatActivity) {

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var summaryExtractorsManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var eventBus: IEventBus


    private val eventBusListener = EventBusListener()

    private var adapter: ArticleSummaryExtractorsAdapter


    init {
        AppComponent.component.inject(this)

        adapter = ArticleSummaryExtractorsAdapter(activity, summaryExtractorsManager)

        eventBus.register(eventBusListener)
    }


    fun showDialog() {
        if(summaryExtractorsManager.getConfigs().isEmpty()) { // no ArticleSummaryExtractorConfig exists -> show AddArticleSummaryExtractorDialog instead
            showAddArticleSummaryExtractorDialog()
        }
        else { // really show ArticleSummaryExtractorsDialog
            showArticleSummaryExtractorsDialog()
        }
    }

    private fun showArticleSummaryExtractorsDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setView(R.layout.dialog_article_summary_extractors)

        builder.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })

        builder.setNeutralButton(R.string.dialog_article_summary_extractors_add_extractor, { dialog, _ ->
            showAddArticleSummaryExtractorView()
            dialog.dismiss()
        })

        builder.setOnDismissListener { eventBus.unregister(eventBusListener)  }

        val dialog = builder.create()
        dialog.show()

        setupAdapter(dialog)
    }

    private fun setupAdapter(dialog: AlertDialog) {
        dialog.rcyArticleSummaryExtractors.adapter = adapter
        adapter.itemClickListener = { item ->
            showSelectedSummaryExtractor(item)
            dialog.dismiss()
        }

        adapter.itemLongClickListener = { item -> ArticleSummaryExtractorConfigDialog().editConfiguration(activity, item) { } }
    }


    private fun showAddArticleSummaryExtractorDialog() {
        router.showAddArticleSummaryExtractorView()
    }


    private fun showSelectedSummaryExtractor(selectedExtractor: ArticleSummaryExtractorConfig) {
        router.showArticleSummaryView(selectedExtractor)
    }

    private fun showAddArticleSummaryExtractorView() {
        router.showAddArticleSummaryExtractorView()
    }


    inner class EventBusListener {

        @Handler
        fun articleSummaryExtractorsChanged(changed: EntitiesOfTypeChanged) {
            if(changed.entityType == ArticleSummaryExtractorConfig::class.java) {
                adapter.updateConfigs()
            }
        }
    }

}