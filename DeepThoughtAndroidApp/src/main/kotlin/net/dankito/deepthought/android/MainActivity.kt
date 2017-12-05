package net.dankito.deepthought.android

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.view_floating_action_button_main.*
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.adapter.MainActivitySectionsPagerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.fragments.MainActivityTabFragment
import net.dankito.deepthought.android.service.ExtractArticleHandler
import net.dankito.deepthought.android.service.IntentHandler
import net.dankito.deepthought.android.views.MainActivityFloatingActionMenuButton
import net.dankito.deepthought.model.*
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.UrlUtil
import net.engio.mbassy.listener.Handler
import org.slf4j.LoggerFactory
import javax.inject.Inject


class MainActivity : BaseActivity() {

    companion object {
        private val log = LoggerFactory.getLogger(MainActivity::class.java)
    }


    private lateinit var sectionsPagerAdapter: MainActivitySectionsPagerAdapter

    private var currentlyVisibleFragment: MainActivityTabFragment<out BaseEntity>? = null

    private lateinit var floatingActionMenuButton: MainActivityFloatingActionMenuButton

    private var eventBusListener: EventBusListener? = null


    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var urlUtil: UrlUtil

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var summaryExtractorManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var extractArticleHandler: ExtractArticleHandler


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        dataManager.addInitializationListener { initializedDataManager() }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }


    private fun setupUI() {
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        showAppVersion(navigationView)
        navigationView.setNavigationItemSelectedListener(navigationListener)

        sectionsPagerAdapter = MainActivitySectionsPagerAdapter(supportFragmentManager)
        viewPager.adapter = sectionsPagerAdapter

        setCurrentlyVisibleFragment(0) // set currentlyVisibleFragment on start otherwise back button won't work on first displayed fragment

        floatingActionMenuButton = MainActivityFloatingActionMenuButton(floatingActionMenu, summaryExtractorManager, router, eventBus)
    }

    private fun showAppVersion(navigationView: NavigationView) {
        try {
            val packageInfo = this.packageManager.getPackageInfo(packageName, 0)
            val version = packageInfo.versionName
            (navigationView.getHeaderView(0).findViewById(R.id.txtAppVersion) as? TextView)?.text = version
        } catch (e: Exception) {
            log.error("Could not read application version")
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if(floatingActionMenuButton.handlesTouch(event)) { // close menu when menu is opened and touch is outside floatingActionMenuButton
            return true
        }

        return super.dispatchTouchEvent(event)
    }

    private fun setCurrentlyVisibleFragment(position: Int) {
        currentlyVisibleFragment?.isCurrentSelectedTab = false

        currentlyVisibleFragment = sectionsPagerAdapter.getItem(position)
        currentlyVisibleFragment?.isCurrentSelectedTab = true
        currentlyVisibleFragment?.viewCameIntoView()
    }

    private fun initializedDataManager() {
        if(dataManager.localSettings.didUserCreateDataEntity == false) {
            val eventBusListener = EventBusListener()
            this.eventBusListener = eventBusListener
            eventBus.register(eventBusListener)
        }
    }

    private fun userCreatedDataEntity() {
        eventBusListener?.let {
            eventBus.unregister(it)
            this.eventBusListener = null
        }

        dataManager.localSettings.didUserCreateDataEntity = true
        dataManager.localSettingsUpdated()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        floatingActionMenuButton.saveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        floatingActionMenuButton.restoreInstanceState(savedInstanceState)
    }


    override fun onResume() {
        super.onResume()

        clearAllActivityResults() // important, so that the results from Activities opened from one of the tabs aren't displayed later in another activity (e.g. opening
        // EditReferenceActivity from ReferenceListView tab first, then going to EditEntryActivity -> Source of first called EditReferenceActivity is then shown in second EditEntryActivity
    }

    override fun onBackPressed() {
        if(floatingActionMenuButton.handlesBackButtonPress()) {

        }
        else if(currentlyVisibleFragment?.onBackPressed() == false) {
            super.onBackPressed() // when not handling by fragment call default back button press handling
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private val navigationListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navArticleSummaryExtractors -> {
                router.showArticleSummaryExtractorsView()
            }
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)

        true
    }


    private fun handleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        IntentHandler(extractArticleHandler, router, urlUtil).handle(intent)
    }



    inner class EventBusListener {

        @Handler
        fun entryChanged(change: EntitiesOfTypeChanged) {
            if(change.changeType == EntityChangeType.Created) {
                when(change.entityType) {
                    Item::class.java,
                    Tag::class.java,
                    Source::class.java,
                    Series::class.java,
                    ReadLaterArticle::class.java ->
                        userCreatedDataEntity()
                }
            }
        }
    }

}