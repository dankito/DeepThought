package net.dankito.deepthought.android.views

import android.app.Activity
import android.view.LayoutInflater
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
import org.slf4j.LoggerFactory


class MainActivityFloatingActionMenuButton(floatingActionMenu: FloatingActionMenu, private val summaryExtractorsManager: ArticleSummaryExtractorConfigManager, private val router: IRouter,
                               private val eventBus: IEventBus) : FloatingActionMenuButton(floatingActionMenu) {

    companion object {
        private val log = LoggerFactory.getLogger(MainActivityFloatingActionMenuButton::class.java)
    }


    private val favoriteArticleSummaryExtractorsButtons = ArrayList<FloatingActionButton>()

    private var defaultOnMenuButtonClickListener: View.OnClickListener? = null

    private val eventBusListener = EventBusListener()


    init {
        setup()

        summaryExtractorsManager.addInitializationListener { setFavoriteArticleSummaryExtractors() }
    }


    private fun setup() {
        floatingActionMenu.fab_add_entry.setOnClickListener { executeAndCloseMenu { router.showCreateEntryView() } }
        floatingActionMenu.fab_add_newspaper_article.setOnClickListener { executeAndCloseMenu { router.showArticleSummaryExtractorsView() } }

        setFavoriteArticleSummaryExtractors()

        setupEventBusListener()
    }

    /**
     * Disables the floating action menu.
     * Is used when there are no favorite ArticleSummaryExtractors. The a click on floating action directly goes to EditEntryActivity to create an item.
     */
    private fun disableFloatingActionMenu() {
        if(defaultOnMenuButtonClickListener == null) {
            backupDefaultOnMenuButtonClickListener()
        }

        floatingActionMenu.setOnMenuButtonClickListener { router.showCreateEntryView() }
    }

    /**
     * The default OnMenuButtonClick listener - which shows the floating action menu - isn't directly accessible -> get it via reflection
     */
    private fun backupDefaultOnMenuButtonClickListener() {
        try {
            val mMenuButtonField = floatingActionMenu.javaClass.getDeclaredField("mMenuButton")
            mMenuButtonField.isAccessible = true
            val mMenuButton = mMenuButtonField.get(floatingActionMenu)

            val mClickListenerField = mMenuButton.javaClass.getDeclaredField("mClickListener")
            mClickListenerField.isAccessible = true
            defaultOnMenuButtonClickListener = mClickListenerField.get(mMenuButton) as View.OnClickListener
        } catch (e: Exception) {
            log.error("Could not get defaultOnMenuButtonClickListener", e)
        }
    }

    /**
     * Enables the floating action menu.
     * Is used when there is at least one favorite ArticleSummaryExtractors.
     */
    private fun enableFloatingActionMenu() {
        floatingActionMenu.setOnMenuButtonClickListener(defaultOnMenuButtonClickListener)
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

        if(favoriteArticleSummaryExtractors.size == 0) {
            disableFloatingActionMenu()
        }
        else {
            enableFloatingActionMenu()

            val layoutInflater = activity.layoutInflater

            favoriteArticleSummaryExtractors.forEach { extractorConfig ->
                addFavoriteArticleSummaryExtractorsButton(layoutInflater, activity, extractorConfig)
            }
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


    inner class EventBusListener {

        @Handler
        fun articleSummaryExtractorsChanged(changed: ArticleSummaryExtractorConfigChanged) {
            setFavoriteArticleSummaryExtractors()
        }
    }

}