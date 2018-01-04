package net.dankito.deepthought.android.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityParameters
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.util.Assert
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.android.views.FullscreenWebView
import net.dankito.deepthought.model.Item
import net.dankito.richtexteditor.android.toolbar.SearchView
import org.apache.lucene.document.TextField
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class EditItemActivity_SearchInWebViewTest : DeepThoughtAndroidTestBase() {


    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    private val testItem = Item("Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content" +
            " Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content" +
            " Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content" +
            " Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content Test Content")


    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()

    @get:Rule
    val testRule = DeepThoughtActivityTestRule<EditEntryActivity>(EditEntryActivity::class.java)


    init {
        TestComponent.component.inject(this)

        testRule.setActivityParameter(parameterHolder, EditEntryActivityParameters(testItem))

        dataManager.localSettings.didShowItemInformationFullscreenHelp = true
    }


    @Test
    fun showAndHideSearchView() {
        Assert.viewIsNotVisible(R.id.lytFullscreenWebViewOptionsBar)

        onView(withId(R.id.wbvwContent)).perform(doubleClick())
        Assert.viewIsVisible(R.id.lytFullscreenWebViewOptionsBar)
        Assert.viewIsNotVisible(getMatcherForSearchControls())

        onView(getMatcherForButtonToggleSearchControlsVisibility()).perform(click())
        TestUtil.sleep(2000)

        Assert.viewIsVisible(getMatcherForSearchControls())
        onView(getMatcherForSearchField()).check(matches(hasFocus()))

        onView(getMatcherForSearchField()).perform(typeText("Test"))

        onView(getMatcherForCountSearchResultsLabel()).check(matches(ViewMatchers.withText("1 of ")))

        TestUtil.sleep(60 * 1000)
    }


    private fun getMatcherForButtonToggleSearchControlsVisibility(): Matcher<View> {
        return allOf(instanceOf(ImageButton::class.java), isDescendantOfA(instanceOf(FullscreenWebView::class.java)), isDescendantOfA(instanceOf(SearchView::class.java)))
    }

    private fun getMatcherForSearchControls(): Matcher<View> {
        return allOf(instanceOf(LinearLayout::class.java), isDescendantOfA(instanceOf(FullscreenWebView::class.java)), isDescendantOfA(instanceOf(SearchView::class.java)))
    }

    private fun getMatcherForSearchField(): Matcher<View> {
        return allOf(instanceOf(EditText::class.java), isDescendantOfA(instanceOf(SearchView::class.java)), isDescendantOfA(instanceOf(FullscreenWebView::class.java)))
    }

    private fun getMatcherForCountSearchResultsLabel(): Matcher<View> {
        return allOf(instanceOf(TextField::class.java), isDescendantOfA(instanceOf(SearchView::class.java)), isDescendantOfA(instanceOf(FullscreenWebView::class.java)))
    }

}