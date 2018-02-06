package net.dankito.deepthought.android.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.widget.ImageButton
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.ItemsListView
import net.dankito.deepthought.android.fragments.ReadLaterArticlesListView
import net.dankito.deepthought.android.fragments.SourcesListView
import net.dankito.deepthought.android.fragments.TagsListView
import net.dankito.deepthought.android.util.Assert
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import org.hamcrest.CoreMatchers
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
        assertOnboardingIsDisplayedInTab(ItemsListView::class.java)

        Assert.viewIsVisible(R.id.arrowToFloatingActionButton)
        assertDrawerTogglesIsNotVisible()
    }

    @Test
    fun addItem_ItemOnboardingThenGetsHidden() {
        assertOnboardingIsDisplayedInTab(ItemsListView::class.java)

        Assert.viewIsVisible(R.id.arrowToFloatingActionButton)
        assertDrawerTogglesIsNotVisible()


        navigator.createItemFromMainActivity("Test info")
        TestUtil.sleep(2000)


        assertDrawerTogglesIsVisible()
        assertOnboardingIsHiddenInTab(ItemsListView::class.java)
    }

    @Test
    fun addItem_AllOtherOnboardingsStillAreDisplayed() {
        assertDrawerTogglesIsNotVisible()

        navigator.createItemFromMainActivity("Test info")
        TestUtil.sleep(2000)


        navigator.navigateToTabTags()
        assertOnboardingIsDisplayedInTab(TagsListView::class.java, true)

        navigator.navigateToTabSources()
        assertOnboardingIsDisplayedInTab(SourcesListView::class.java)

        navigator.navigateToTabReadLaterArticles()
        assertOnboardingIsDisplayedInTab(ReadLaterArticlesListView::class.java)
    }

    @Test
    fun addTag_TagOnboardingThenGetsHidden() {
        assertDrawerTogglesIsNotVisible()


        navigator.createTagFromMainActivity("Test tag")
        TestUtil.sleep(2000)


        assertOnboardingIsDisplayedInTab(ItemsListView::class.java)

        navigator.navigateToTabTags()
        assertOnboardingIsHiddenInTab(TagsListView::class.java)

        navigator.navigateToTabSources()
        assertOnboardingIsDisplayedInTab(SourcesListView::class.java)

        navigator.navigateToTabReadLaterArticles()
        assertOnboardingIsDisplayedInTab(ReadLaterArticlesListView::class.java)
    }

    @Test
    fun addSource_SourceOnboardingThenGetsHidden() {
        assertDrawerTogglesIsNotVisible()


        navigator.createSourceFromMainActivity("Test source")
        TestUtil.sleep(2000)


        assertOnboardingIsDisplayedInTab(ItemsListView::class.java)

        navigator.navigateToTabTags()
        assertOnboardingIsDisplayedInTab(TagsListView::class.java, true)

        navigator.navigateToTabSources()
        assertOnboardingIsHiddenInTab(SourcesListView::class.java)

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

    private fun assertDrawerTogglesIsVisible() {
        Assert.viewIsVisible(CoreMatchers.allOf(CoreMatchers.instanceOf(ImageButton::class.java), isDescendantOfA(withId(R.id.toolbar))))
    }

    private fun assertDrawerTogglesIsNotVisible() {
        Assert.viewIsNotVisible(CoreMatchers.allOf(CoreMatchers.instanceOf(ImageButton::class.java), isDescendantOfA(withId(R.id.toolbar))))
    }

    private fun assertIsVisible(viewId: Int) {
        onView(withId(viewId)).check(matches(isDisplayed()))
    }

    private fun assertIsNotVisible(viewId: Int) {
        onView(withId(viewId)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

}