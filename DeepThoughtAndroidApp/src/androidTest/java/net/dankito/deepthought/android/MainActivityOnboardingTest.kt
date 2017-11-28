package net.dankito.deepthought.android

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.fragments.ReadLaterArticlesListView
import net.dankito.deepthought.android.fragments.ReferencesListView
import net.dankito.deepthought.android.fragments.TagsListView
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.AllOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityOnboardingTest: DeepThoughtAndroidTestBase() {

    @get:Rule
    val testRule = DeepThoughtActivityTestRule<MainActivity>(MainActivity::class.java)

    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()


    @Test
    fun noItems_OnboardingShouldBeDisplayed() {
        assertOnboardingIsDisplayedInTab(EntriesListView::class.java)

        assertIsVisibleInTab(R.id.arrowToFloatingActionButton, EntriesListView::class.java)
        assertIsNotVisible(R.id.bottomViewNavigation)
    }

    @Test
    fun addItem_ItemOnboardingThenGetsHidden() {
        assertOnboardingIsDisplayedInTab(EntriesListView::class.java)

        assertIsVisibleInTab(R.id.arrowToFloatingActionButton, EntriesListView::class.java)
        assertIsNotVisible(R.id.bottomViewNavigation)


        navigator.createEntry("Test info")
        TestUtil.sleep(2000)


        assertIsVisible(R.id.bottomViewNavigation)
        assertOnboardingIsHiddenInTab(EntriesListView::class.java)
    }

    @Test
    fun addItem_AllOtherOnboardingsStillAreDisplayed() {
        assertIsNotVisible(R.id.bottomViewNavigation)

        navigator.createEntry("Test info")
        TestUtil.sleep(2000)


        navigator.navigateToTabTags()
        assertOnboardingIsDisplayedInTab(TagsListView::class.java, true)

        navigator.navigateToTabSources()
        assertOnboardingIsDisplayedInTab(ReferencesListView::class.java)

        navigator.navigateToTabReadLaterArticles()
        assertOnboardingIsDisplayedInTab(ReadLaterArticlesListView::class.java)
    }

    @Test
    fun addTag_TagOnboardingThenGetsHidden() {
        assertIsNotVisible(R.id.bottomViewNavigation)


        navigator.createTag("Test tag")
        TestUtil.sleep(2000)


        assertOnboardingIsDisplayedInTab(EntriesListView::class.java)

        navigator.navigateToTabTags()
        assertOnboardingIsHiddenInTab(TagsListView::class.java)

        navigator.navigateToTabSources()
        assertOnboardingIsDisplayedInTab(ReferencesListView::class.java)

        navigator.navigateToTabReadLaterArticles()
        assertOnboardingIsDisplayedInTab(ReadLaterArticlesListView::class.java)
    }


    private fun assertOnboardingIsDisplayedInTab(tabClass: Any, isRecyclerViewVisible: Boolean = false) {
        assertIsVisibleInTab(R.id.lytOnboardingText, tabClass)

        onView(withId(R.id.search)).check(ViewAssertions.doesNotExist())

        if(isRecyclerViewVisible) {
            assertIsVisibleInTab(R.id.rcyEntities, tabClass)
        }
        else {
            assertIsNotVisibleInTab(R.id.rcyEntities, tabClass)
        }

        assertIsNotVisibleInTab(R.id.lytFilteredEntities, tabClass)
        assertIsNotVisibleInTab(R.id.lytContextHelp, tabClass)
    }

    private fun assertOnboardingIsHiddenInTab(tabClass: Any) {
        assertIsNotVisibleInTab(R.id.lytOnboardingText, tabClass)

        assertIsVisible(R.id.search)

        assertIsVisibleInTab(R.id.rcyEntities, tabClass)
        assertIsNotVisibleInTab(R.id.lytFilteredEntities, tabClass)
        assertIsNotVisibleInTab(R.id.lytContextHelp, tabClass)
    }

    private fun assertIsVisibleInTab(viewId: Int, tabClass: Any) {
        onView(AllOf.allOf(withId(viewId), isDescendantOfA(ViewMatchers.withTagValue(`is`(tabClass)))))
                .check(matches(ViewMatchers.isDisplayed()))
    }

    private fun assertIsNotVisibleInTab(viewId: Int, tabClass: Any) {
        onView(AllOf.allOf(withId(viewId), isDescendantOfA(ViewMatchers.withTagValue(`is`(tabClass)))))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private fun assertIsVisible(viewId: Int) {
        onView(withId(viewId)).check(matches(isDisplayed()))
    }

    private fun assertIsNotVisible(viewId: Int) {
        onView(withId(viewId)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

}