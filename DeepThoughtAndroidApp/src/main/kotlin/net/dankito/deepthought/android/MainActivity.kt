package net.dankito.deepthought.android

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.view_floating_action_button_main.*
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.adapter.MainActivitySectionsPagerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.fragments.MainActivityTabFragment
import net.dankito.deepthought.android.service.IntentHandler
import net.dankito.deepthought.android.views.MainActivityFloatingActionMenuButton
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.UrlUtil
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject


class MainActivity : BaseActivity() {


    private lateinit var sectionsPagerAdapter: MainActivitySectionsPagerAdapter

    private var currentlySelectedNavigationItem: MenuItem? = null

    private var currentlyVisibleFragment: MainActivityTabFragment<out BaseEntity>? = null

    private lateinit var floatingActionMenuButton: MainActivityFloatingActionMenuButton


    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var urlUtil: UrlUtil

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var summaryExtractorManager: ArticleSummaryExtractorConfigManager

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var dialogService: IDialogService


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }


    private fun setupUI() {
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        sectionsPagerAdapter = MainActivitySectionsPagerAdapter(supportFragmentManager, bottomViewNavigation)
        viewPager.adapter = sectionsPagerAdapter

        bottomViewNavigation.disableShiftMode()
        bottomViewNavigation.setOnNavigationItemSelectedListener(bottomViewNavigationItemSelectedListener)

        setCurrentlyVisibleFragment(0) // set currentlyVisibleFragment on start otherwise back button won't work on first displayed fragment

        floatingActionMenuButton = MainActivityFloatingActionMenuButton(floatingActionMenu, summaryExtractorManager, router, eventBus)
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


    override fun onResume() {
        super.onResume()

        clearAllActivityResults() // important, so that the results from Activities opened from one of the tabs aren't displayed later in another activity (e.g. opening
        // EditReferenceActivity from ReferenceListView tab first, then going to EditEntryActivity -> Reference of first called EditReferenceActivity is then shown in second EditEntryActivity
    }

    override fun onBackPressed() {
        if(floatingActionMenuButton.handlesBackButtonPress()) {

        }
        else if(currentlyVisibleFragment?.onBackPressed() == false) {
            super.onBackPressed() // when not handling by fragment call default back button press handling
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


    private fun handleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        IntentHandler(articleExtractorManager, router, urlUtil, dialogService).handle(intent)
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

            setCurrentlyVisibleFragment(position)
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

}