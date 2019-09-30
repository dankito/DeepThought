package net.dankito.deepthought.android.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.windowdata.EditItemWindowData
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class EditItemActivity_EditItemWithNoSourceTest : DeepThoughtAndroidTestBase() {

    companion object {
        private val SourceTitleAfterEditing = "Now we have changed the source title"
    }


    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    private val testItem = Item("Test Content")


    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()

    @get:Rule
    val testRule = DeepThoughtActivityTestRule<EditItemActivity>(EditItemActivity::class.java)


    init {
        TestComponent.component.inject(this)

        testRule.setActivityParameter(parameterHolder, EditItemWindowData(testItem))
    }


    @Test
    fun editSourceTitleInActivity_SaveWithoutLosingFocusBefore_TitleGetsSaved() {
        assertIsNotVisible(R.id.lytSourcePreview)

        navigator.clickOnEditItemActivityFloatingActionButton()
        onView(withId(R.id.fabEditItemSource)).perform(click())

        assertIsVisible(R.id.lytSourcePreview) // after a click only Source title is displayed and can be edited
        checkDisplayedSourceValue("")

        navigator.setValueOfEditEntityField(R.id.lytSourcePreview, SourceTitleAfterEditing)
        checkDisplayedSourceValue(SourceTitleAfterEditing)

        onView(withId(R.id.mnSaveItem)).perform(click())
        Assert.assertThat(testItem.source?.title, `is`(SourceTitleAfterEditing))
    }

    @Test
    fun editSourceTitleInActivity_SaveAfterLosingFocusBefore_TitleGetsSaved() {
        assertIsNotVisible(R.id.lytSourcePreview)

        navigator.clickOnEditItemActivityFloatingActionButton()
        onView(withId(R.id.fabEditItemSource)).perform(click())

        assertIsVisible(R.id.lytSourcePreview) // after a click only Source title is displayed and can be edited
        checkDisplayedSourceValue("")

        navigator.setValueOfEditEntityField(R.id.lytSourcePreview, SourceTitleAfterEditing)
        checkDisplayedSourceValue(SourceTitleAfterEditing)

        // now lose focus
        onView(withId(R.id.wbvwContent)).perform(click())
        TestUtil.sleep(1000)
        onView(withId(R.id.mnApplyHtmlChanges)).perform(click())
        checkDisplayedSourceValue(SourceTitleAfterEditing)

        onView(withId(R.id.mnSaveItem)).perform(click())
        Assert.assertThat(testItem.source?.title, `is`(SourceTitleAfterEditing))
    }


    private fun assertIsNotVisible(viewId: Int) {
        onView(withId(viewId)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private fun assertIsVisible(viewId: Int) {
        onView(withId(viewId)).check(matches(isDisplayed()))
    }

    private fun checkDisplayedSourceValue(valueToMatch: String) {
        onView(allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(R.id.lytSourcePreview)))) // find edtxtEntityFieldValue in EditEntityField
                .check(matches(withText(`is`(valueToMatch))))
    }

}