package net.dankito.deepthought.android.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntriesListView
import net.dankito.deepthought.android.fragments.ReferencesListView
import net.dankito.deepthought.android.fragments.TagsListView
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.matchers.RecyclerViewInViewPagerMatcher.Companion.withRecyclerView
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.core.AllOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateEntitiesTest : DeepThoughtAndroidTestBase() {

    companion object {
        const val TestContent = "Test Content"
        const val TestSummary = "Just a summary"

        const val TestSourceTitle = "Source of it all"
        const val TestSeriesTitle = "What a series"

        const val TestTag1Name = "Tag uno"
        const val TestTag2Name = "Tag due"
        const val TestTag3Name = "Tag tre"
    }


    @get:Rule
    val testRule = DeepThoughtActivityTestRule<MainActivity>(MainActivity::class.java)

    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()


    @Test
    fun createItemTagsSourceAndSeries() {
        navigator.createEntities(TestContent, TestSummary, TestSourceTitle, TestSeriesTitle, TestTag1Name, TestTag2Name, TestTag3Name)
        TestUtil.sleep(1000)


        matchRecyclerViewInTabIsDisplayed(EntriesListView::class.java)

        matchReferencePreviewAtPosition(0, TestSourceTitle)

        matchEntryPreviewAtPositionContains(0, TestContent)
        matchEntryPreviewAtPositionContains(0, TestSummary)
        matchEntryPreviewAtPositionContains(0, TestSeriesTitle)

        // TODO: how to match tags?
//        onView(withRecyclerView(R.id.rcyEntities).atPositionOnView(0, R.id.lytEntryTags)).check(matches(withText(containsString(TestTag1Name))))


        navigator.navigateToTabTags()

        matchRecyclerViewInTabIsDisplayed(TagsListView::class.java as Any)

        matchTagNameAtPosition(2, TestTag2Name)
        matchTagNameAtPosition(3, TestTag3Name)
        matchTagNameAtPosition(4, TestTag1Name)


        navigator.navigateToTabSources()

        matchRecyclerViewInTabIsDisplayed(ReferencesListView::class.java as Any)

        matchListItemAtPositionText(ReferencesListView::class.java, 0, R.id.txtvwEntityName, TestSourceTitle)
        matchListItemAtPositionText(ReferencesListView::class.java, 0, R.id.txtvwEntitySecondaryInformation, TestSeriesTitle)
    }

    private fun matchRecyclerViewInTabIsDisplayed(tabTag: Any) {
        onView(AllOf.allOf(withId(R.id.rcyEntities), isDescendantOfA(withTagValue(`is`(tabTag)))))
                .check(matches(isDisplayed()))
    }

    private fun matchEntryPreviewAtPositionContains(position: Int, entryPreview: String) {
        matchListItemAtPositionText(EntriesListView::class.java, position, R.id.txtEntryPreview, entryPreview)
    }

    private fun matchReferencePreviewAtPosition(position: Int, referencePreview: String) {
        matchListItemAtPositionText(EntriesListView::class.java, position, R.id.txtReferencePreview, referencePreview)
    }

    private fun matchTagNameAtPosition(position: Int, tagName: String) {
        matchListItemAtPositionText(TagsListView::class.java, position, R.id.txtTagDisplayText, tagName)
    }

    private fun matchListItemAtPositionText(tabTag: Any, position: Int, textViewResourceId: Int, textViewText: String) {
        onView(withRecyclerView(tabTag, R.id.rcyEntities).atPositionOnView(position, textViewResourceId))
                .check(matches(withText(containsString(textViewText))))
    }

}