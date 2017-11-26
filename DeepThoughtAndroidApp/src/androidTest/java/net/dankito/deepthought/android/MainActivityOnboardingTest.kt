package net.dankito.deepthought.android

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.util.TestUtil
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


        createEntry("Test info")
        TestUtil.sleep(2000)


        assertOnboardingIsHiddenInTab(EntriesListView::class.java)
    }


    private fun createEntry(information: String) {
        TestUtil.sleep(1000)
        clickOnFloatingActionButton()
        onView(withId(R.id.fab_add_entry)).perform(click())

        TestUtil.sleep(2000)
        onView(withId(R.id.editor)).perform(click())
        InstrumentationRegistry.getInstrumentation().sendStringSync(information)
        onView(withId(R.id.mnApplyHtmlChanges)).perform(click())
        onView(withId(R.id.mnSaveEntry)).perform(click())
    }

    private fun clickOnFloatingActionButton() {
        // FloatingActionMenu adds a FloatingActionButton without id which
        onView(AllOf.allOf(isDescendantOfA(CoreMatchers.instanceOf(FloatingActionMenu::class.java)), withId(-1), CoreMatchers.instanceOf(FloatingActionButton::class.java), isDisplayed()))
                .perform(click())
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