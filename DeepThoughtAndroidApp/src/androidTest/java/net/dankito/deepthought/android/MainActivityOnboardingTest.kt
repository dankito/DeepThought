package net.dankito.deepthought.android

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import net.dankito.deepthought.android.fragments.EntriesListView
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
    fun noEntries_OboardingShouldBeShown() {
        assertOnboardingIsShownInTab(EntriesListView::class.java)

        assertIsVisibleInTab(R.id.arrowToFloatingActionButton, EntriesListView::class.java)
        assertIsNotVisible(R.id.bottomViewNavigation)
    }

    @Test
    fun addEntities_OnboardingThenGetsHidden() {
        assertOnboardingIsShownInTab(EntriesListView::class.java)

        assertIsVisibleInTab(R.id.arrowToFloatingActionButton, EntriesListView::class.java)
        assertIsNotVisible(R.id.bottomViewNavigation)


        navigator.createEntry("Test info")
        TestUtil.sleep(2000)


        assertOnboardingIsHiddenInTab(EntriesListView::class.java)
    }


    private fun assertOnboardingIsShownInTab(tabClass: Any) {
        assertIsVisibleInTab(R.id.lytOnboardingText, tabClass)

        onView(withId(R.id.search)).check(ViewAssertions.doesNotExist())

        assertIsNotVisibleInTab(R.id.rcyEntities, tabClass)
        assertIsNotVisibleInTab(R.id.lytFilteredEntities, tabClass)
        assertIsNotVisibleInTab(R.id.lytContextHelp, tabClass)
    }

    private fun assertOnboardingIsHiddenInTab(tabClass: Any) {
        assertIsNotVisibleInTab(R.id.lytOnboardingText, tabClass)

        onView(withId(R.id.search)).check(matches(isDisplayed()))

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

    private fun assertIsNotVisible(viewId: Int) {
        onView(withId(viewId)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

}