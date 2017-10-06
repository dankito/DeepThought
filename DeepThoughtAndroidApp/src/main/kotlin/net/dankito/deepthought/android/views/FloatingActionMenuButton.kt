package net.dankito.deepthought.android.views

import android.app.Activity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_main.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.messages.ArticleSummaryExtractorConfigChanged
import net.dankito.service.eventbus.IEventBus
import net.engio.mbassy.listener.Handler


class FloatingActionMenuButton(private val floatingActionMenu: FloatingActionMenu, private val summaryExtractorsManager: ArticleSummaryExtractorConfigManager, private val router: IRouter,
                               private val eventBus: IEventBus) {

    private val favoriteArticleSummaryExtractorsButtons = ArrayList<FloatingActionButton>()

    private val eventBusListener = EventBusListener()


    init {
        setup()

        summaryExtractorsManager.addInitializationListener { setFavoriteArticleSummaryExtractors() }
    }


    private fun setup() {
        floatingActionMenu.setClosedOnTouchOutside(true)

        floatingActionMenu.fab_add_entry.setOnClickListener { executeAndCloseMenu { router.showCreateEntryView() } }
        floatingActionMenu.fab_add_newspaper_article.setOnClickListener { executeAndCloseMenu { router.showArticleSummaryExtractorsView() } }

        setFavoriteArticleSummaryExtractors()

        setupEventBusListener()
    }

    private fun setupEventBusListener() {
        eventBus.register(eventBusListener)

        floatingActionMenu.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                eventBus.unregister(eventBusListener)

                floatingActionMenu.removeOnAttachStateChangeListener(this)
            }

            override fun onViewAttachedToWindow(v: View?) {
            }

        })
    }


    private fun setFavoriteArticleSummaryExtractors() {
        val activity = floatingActionMenu.context as Activity

        activity.runOnUiThread { setFavoriteArticleSummaryExtractorsOnUIThread(activity, summaryExtractorsManager.getFavorites()) }
    }

    private fun setFavoriteArticleSummaryExtractorsOnUIThread(activity: Activity, favoriteArticleSummaryExtractors: List<ArticleSummaryExtractorConfig>) {
        clearFavoriteArticleSummaryExtractorsButtons()

        val layoutInflater = activity.layoutInflater

        favoriteArticleSummaryExtractors.forEach { extractorConfig ->
            addFavoriteArticleSummaryExtractorsButton(layoutInflater, activity, extractorConfig)
        }
    }

    private fun addFavoriteArticleSummaryExtractorsButton(layoutInflater: LayoutInflater, activity: Activity, extractorConfig: ArticleSummaryExtractorConfig) {
        val menuButton = layoutInflater.inflate(R.layout.view_floating_action_menu_button, null) as FloatingActionButton
        menuButton.labelText = activity.getString(R.string.floating_action_button_add_article_of_newspaper, extractorConfig.name)

        menuButton.setOnClickListener { executeAndCloseMenu { router.showArticleSummaryView(extractorConfig) } }

        floatingActionMenu.addMenuButton(menuButton, 0)

        favoriteArticleSummaryExtractorsButtons.add(menuButton)
    }

    private fun clearFavoriteArticleSummaryExtractorsButtons() {
        favoriteArticleSummaryExtractorsButtons.forEach { floatingActionMenu.removeMenuButton(it) }
        favoriteArticleSummaryExtractorsButtons.clear()
    }

    private fun executeAndCloseMenu(action: () -> Unit) {
        action()
        closeMenu()
    }

    private fun closeMenu() {
        floatingActionMenu.close(true)
    }


    fun handlesBackButtonPress(): Boolean {
        if(floatingActionMenu.isOpened) {
            closeMenu()
            return true
        }

        return false
    }


    fun handlesTouch(event: MotionEvent): Boolean {
        if(floatingActionMenu.isOpened) { // if menu is opened and user clicked somewhere else in the view, close menu
            if(floatingActionMenu.isTouchInsideView(event) == false) {
                closeMenu()

                return true
            }
        }

        return false
    }


    inner class EventBusListener {

        @Handler
        fun articleSummaryExtractorsChanged(changed: ArticleSummaryExtractorConfigChanged) {
            setFavoriteArticleSummaryExtractors()
        }
    }

}