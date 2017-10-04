package net.dankito.deepthought.android

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import net.dankito.deepthought.android.fragments.EntriesListView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.AllOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityOnboardingTest {

    @get:Rule
    val testRule = DeepThoughtActivityTestRule<MainActivity>(MainActivity::class.java)


    @Test
    fun noEntries_OboardingShouldBeShown() {
        assertIsVisibleInTab(R.id.lytOnboardingText, EntriesListView::class.java)
        assertIsVisibleInTab(R.id.arrowToFloatingActionButton, EntriesListView::class.java)

        assertIsNotVisible(R.id.bottomViewNavigation)
        onView(withId(R.id.search)).check(ViewAssertions.doesNotExist())

        assertIsNotVisibleInTab(R.id.rcyEntities, EntriesListView::class.java)
        assertIsNotVisibleInTab(R.id.lytFilteredEntities, EntriesListView::class.java)
        assertIsNotVisibleInTab(R.id.lytContextHelp, EntriesListView::class.java)
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