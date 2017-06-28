package net.dankito.deepthought.android.views

import android.app.Activity
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_main.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter


class FloatingActionMenuButton(private val fab: FloatingActionMenu, private val router: IRouter, private val summaryExtractorManager: ArticleSummaryExtractorConfigManager) {

    init {
        setup()
    }


    private fun setup() {
        fab.fab_add_entry.setOnClickListener { executeAndCloseMenu { router.showCreateEntryView() } }
        fab.fab_add_newspaper_article.setOnClickListener { executeAndCloseMenu { router.showArticleSummaryExtractorsView() } }

        setupFavoriteArticleSummaryExtractors()
    }

    private fun setupFavoriteArticleSummaryExtractors() {
        val activity = fab.context as Activity

        activity.runOnUiThread { setFavoriteArticleSummaryExtractorsOnUIThread(activity, summaryExtractorManager.getFavorites()) }
    }

    private fun setFavoriteArticleSummaryExtractorsOnUIThread(activity: Activity, favoriteArticleSummaryExtractors: List<ArticleSummaryExtractorConfig>) {
        val layoutInflater = activity.layoutInflater

        favoriteArticleSummaryExtractors.forEach { extractorConfig ->
            val menuButton = layoutInflater.inflate(R.layout.view_floating_action_menu_button, null) as FloatingActionButton
            menuButton.labelText = activity.getString(R.string.floating_action_button_add_article_of_newspaper, extractorConfig.name)

            menuButton.setOnClickListener { executeAndCloseMenu { router.showArticleSummaryView(extractorConfig) } }

            fab.addMenuButton(menuButton, 0)
        }
    }

    private fun executeAndCloseMenu(action: () -> Unit) {
        action()
        fab.close(true)
    }

}