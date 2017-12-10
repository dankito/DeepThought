package net.dankito.deepthought.android.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityParameters
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.matchers.RecyclerViewInViewMatcher
import net.dankito.deepthought.android.util.matchers.RecyclerViewItemCountAssertion
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class EditItemActivity_EditSourceTest : DeepThoughtAndroidTestBase() {

    companion object {
        private val SourceTitleAtStart = "Unset source title"
        private val SourceTitleAfterEditing = "Now we have changed the source title"
        private val SourceTitleAfterEditingDetails = "Now we have changed the source title in EditSourceActivity"

        private val SeriesTitle = "Series"

        private val PublishingDateString = "27.03.1988"
        private val PublishingDateFormat = SimpleDateFormat("dd.MM.yyyy")
        private val PublishingDate = PublishingDateFormat.parse(PublishingDateString)

        private val SourceToSelectTitle = "Select me"
        private val SourceWithThreeResultsTitleStart = "Multiple sources test result "
        private val CountExistingSources = 6
    }


    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    private val testItem = Item("Test Content")

    private val series = Series(SeriesTitle)

    private val source = Source(SourceTitleAtStart, "", PublishingDate, series = series)


    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()

    @get:Rule
    val testRule = DeepThoughtActivityTestRule<EditEntryActivity>(EditEntryActivity::class.java)


    init {
        TestComponent.component.inject(this)

        testItem.source = source

        testRule.setActivityParameter(parameterHolder, EditEntryActivityParameters(testItem))
    }


    @Test
    fun editSourceTitleInActivity_SaveWithoutLosingFocusBefore_TitleGetsSaved() {
        assertThat(source.title, `is`(SourceTitleAtStart))
        checkDisplayedSourceValue(Source(SourceTitleAtStart, "", PublishingDate).getPreviewWithSeriesAndPublishingDate(series)) // on start Source title with Series and publishing date preview is displayed

        onView(withId(R.id.lytReferencePreview)).perform(click())
        checkDisplayedSourceValue(SourceTitleAtStart) // after a click only Source title is displayed and can be edited

        navigator.setValueOfEditEntityField(R.id.lytReferencePreview, SourceTitleAfterEditing)
        checkDisplayedSourceValue(SourceTitleAfterEditing)

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(source.title, `is`(SourceTitleAfterEditing))
    }

    @Test
    fun editSourceTitleInActivity_SaveAfterLosingFocusBefore_TitleGetsSaved() {
        assertThat(source.title, `is`(SourceTitleAtStart))
        checkDisplayedSourceValue(Source(SourceTitleAtStart, "", PublishingDate).getPreviewWithSeriesAndPublishingDate(series)) // on start Source title with Series and publishing date preview is displayed

        onView(withId(R.id.lytReferencePreview)).perform(click())
        checkDisplayedSourceValue(SourceTitleAtStart) // after a click only Source title is displayed and can be edited

        navigator.setValueOfEditEntityField(R.id.lytReferencePreview, SourceTitleAfterEditing)
        checkDisplayedSourceValue(SourceTitleAfterEditing)

        // now lose focus
        onView(withId(R.id.wbvwContent)).perform(click())
        TestUtil.sleep(1000)
        onView(withId(R.id.mnApplyHtmlChanges)).perform(click())
        checkDisplayedSourceValue(Source(SourceTitleAfterEditing, "", PublishingDate).getPreviewWithSeriesAndPublishingDate(series))

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(source.title, `is`(SourceTitleAfterEditing))
    }


    @Test
    fun setNewSource() {
        assertThat(source.title, `is`(SourceTitleAtStart))
        checkDisplayedSourceValue(Source(SourceTitleAtStart, "", PublishingDate).getPreviewWithSeriesAndPublishingDate(series)) // on start Source title with Series and publishing date preview is displayed

        onView(withId(R.id.lytReferencePreview)).perform(click())
        checkDisplayedSourceValue(SourceTitleAtStart) // after a click only Source title is displayed and can be edited

        navigator.clickOnEditEntityReferenceFieldCreateNewEntityPopupMenu(R.id.lytReferencePreview)
        checkDisplayedSourceValue("")

        navigator.setValueOfEditEntityField(R.id.lytReferencePreview, SourceTitleAfterEditing)
        checkDisplayedSourceValue(SourceTitleAfterEditing)

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.source, `is`(not(source)))
        assertThat(source.title, `is`(SourceTitleAtStart))
        assertThat(testItem.source?.title, `is`(SourceTitleAfterEditing))
    }

    @Test
    fun clearSource() {
        assertThat(source.title, `is`(SourceTitleAtStart))
        checkDisplayedSourceValue(Source(SourceTitleAtStart, "", PublishingDate).getPreviewWithSeriesAndPublishingDate(series)) // on start Source title with Series and publishing date preview is displayed

        onView(withId(R.id.lytReferencePreview)).perform(click())
        checkDisplayedSourceValue(SourceTitleAtStart) // after a click only Source title is displayed and can be edited

        navigator.clickOnEditEntityReferenceFieldRemoveEntityPopupMenu(R.id.lytReferencePreview)
        checkDisplayedSourceValue(testRule.activity.getString(R.string.activity_edit_item_source_onboarding_text))

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.source, `is`(not(source)))
        assertThat(source.title, `is`(SourceTitleAtStart))
        assertThat(testItem.source, nullValue())
    }

    @Test
    fun editDetails() {
        assertThat(source.title, `is`(SourceTitleAtStart))
        checkDisplayedSourceValue(Source(SourceTitleAtStart, "", PublishingDate).getPreviewWithSeriesAndPublishingDate(series)) // on start Source title with Series and publishing date preview is displayed

        onView(withId(R.id.lytReferencePreview)).perform(click())
        checkDisplayedSourceValue(SourceTitleAtStart) // after a click only Source title is displayed and can be edited

        navigator.setValueOfEditEntityField(R.id.lytReferencePreview, SourceTitleAfterEditing)
        checkDisplayedSourceValue(SourceTitleAfterEditing)

        navigator.clickOnEditEntityReferenceFieldEditDetailsPopupMenu(R.id.lytReferencePreview)

        checkDisplayedValueInEditEntityField(SourceTitleAfterEditing, R.id.lytEditReferenceTitle)

        navigator.setValueOfEditEntityField(R.id.lytEditReferenceTitle, SourceTitleAfterEditingDetails)
        checkDisplayedValueInEditEntityField(SourceTitleAfterEditingDetails, R.id.lytEditReferenceTitle)

        onView(withId(R.id.mnSaveReference)).perform(click())
        TestUtil.sleep(1000)

        checkDisplayedSourceValue(SourceTitleAfterEditingDetails)

        assertThat(testItem.source, `is`(source))
        assertThat(source.title, `is`(SourceTitleAfterEditingDetails))
    }


    @Test
    fun selectExistingSource() {
        createTestSources()

        assertThat(source.title, `is`(SourceTitleAtStart))
        checkDisplayedSourceValue(Source(SourceTitleAtStart, "", PublishingDate).getPreviewWithSeriesAndPublishingDate(series)) // on start Source title with Series and publishing date preview is displayed
        checkRecyclerViewSearchResultsIsNotVisible()

        onView(withId(R.id.lytReferencePreview)).perform(click())
        checkDisplayedSourceValue(SourceTitleAtStart) // after a click only Source title is displayed and can be edited

        navigator.setValueOfEditEntityField(R.id.lytReferencePreview, "")
        checkDisplayedSourceValue("")
        checkRecyclerViewSearchResultsIsVisible()
        checkCountItemsInRecyclerViewSearchResults(CountExistingSources)
        TestUtil.sleep(1000) // so that user can see what happened

        navigator.setValueOfEditEntityField(R.id.lytReferencePreview, SourceWithThreeResultsTitleStart)
        checkRecyclerViewSearchResultsIsVisible()
        checkCountItemsInRecyclerViewSearchResults(3)
        TestUtil.sleep(1000) // so that user can see what happened

        navigator.setValueOfEditEntityField(R.id.lytReferencePreview, SourceToSelectTitle)
        checkRecyclerViewSearchResultsIsVisible()
        checkCountItemsInRecyclerViewSearchResults(1)
        onView(RecyclerViewInViewMatcher.withRecyclerView(R.id.lytReferencePreview, R.id.rcySearchResults)
                .atPosition(0))
                .perform(click())

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.source, `is`(not(source)))
        assertThat(source.title, `is`(SourceTitleAtStart))
        assertThat(testItem.source?.title, `is`(SourceToSelectTitle))
    }

    private fun createTestSources() {
        persistSource(Source(SourceWithThreeResultsTitleStart + "1"))
        persistSource(Source(SourceWithThreeResultsTitleStart + "2"))
        persistSource(Source(SourceWithThreeResultsTitleStart + "3"))
        persistSource(Source(SourceToSelectTitle))
        persistSource(Source("Noise 1"))
        persistSource(Source("Noise 2"))
    }


    private fun checkDisplayedSourceValue(valueToMatch: String) {
        checkDisplayedValueInEditEntityField(valueToMatch, R.id.lytReferencePreview)
    }

    private fun checkDisplayedValueInEditEntityField(valueToMatch: String, editEntityFieldId: Int) {
        onView(allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(editEntityFieldId)))) // find edtxtEntityFieldValue in EditEntityField
                .check(matches(withText(`is`(valueToMatch))))
    }

    private fun checkRecyclerViewSearchResultsIsVisible() {
        onView(allOf(instanceOf(RecyclerView::class.java), isDescendantOfA(withId(R.id.lytReferencePreview)))) // find RecyclerView in EditEntityField
                .check(matches(isDisplayed()))
    }

    private fun checkRecyclerViewSearchResultsIsNotVisible() {
        onView(allOf(instanceOf(RecyclerView::class.java), isDescendantOfA(withId(R.id.lytReferencePreview)))) // find RecyclerView in EditEntityField
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private fun checkCountItemsInRecyclerViewSearchResults(expectedCount: Int) {
        onView(allOf(instanceOf(RecyclerView::class.java), isDescendantOfA(withId(R.id.lytReferencePreview)))) // find RecyclerView in EditEntityField
                .check(RecyclerViewItemCountAssertion(expectedCount))
    }

}