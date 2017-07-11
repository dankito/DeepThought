package net.dankito.deepthought.android

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.view_floating_action_button_main.*
import net.dankito.deepthought.android.adapter.MainActivitySectionsPagerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.IntentHandler
import net.dankito.deepthought.android.service.ui.BaseActivity
import net.dankito.deepthought.android.views.FloatingActionMenuButton
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.service.eventbus.IEventBus
import javax.inject.Inject


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var sectionsPagerAdapter: MainActivitySectionsPagerAdapter

    private var currentlySelectedNavigationItem: MenuItem? = null

    private lateinit var floatingActionMenuButton: FloatingActionMenuButton


    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var summaryExtractorManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var articleExtractors: ArticleExtractors


    init {
        AppComponent.addInitializationListener { appComponentInitialized() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        if(AppComponent.isInitialized) {
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }


    private fun setupUI() {
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

//        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
//        val toggle = ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
//        drawer.addDrawerListener(toggle)
//        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)

        bottomViewNavigation.setOnNavigationItemSelectedListener(bottomViewNavigationItemSelectedListener)
    }


    private fun appComponentInitialized() {
        AppComponent.component.inject(this)

        currentActivityTracker?.currentActivity = this

        runOnUiThread { setupDependentUIParts() }

        handleIntent(intent)
    }

    private fun setupDependentUIParts() {
        floatingActionMenuButton = FloatingActionMenuButton(fab_menu, summaryExtractorManager, router, eventBus)

        sectionsPagerAdapter = MainActivitySectionsPagerAdapter(supportFragmentManager)
        viewPager.adapter = sectionsPagerAdapter
    }


    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
//            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    private fun handleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        IntentHandler(articleExtractors, router).handle(intent)
    }


    private val bottomViewNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        viewPager.setCurrentItem(item.order, true)

        return@OnNavigationItemSelectedListener true
    }

    private val viewPagerPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }

        override fun onPageSelected(position: Int) {
            val previous = currentlySelectedNavigationItem
            if (previous != null) {
                previous.isChecked = false
            }
            else {
                bottomViewNavigation.menu.getItem(0).isChecked = false
            }

            val currentItem = bottomViewNavigation.menu.getItem(position)
            currentItem.isChecked = true
            currentlySelectedNavigationItem = currentItem

            sectionsPagerAdapter.getItem(position).viewCameIntoView()
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

}