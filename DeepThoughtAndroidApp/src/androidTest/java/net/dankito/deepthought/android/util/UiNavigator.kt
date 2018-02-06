package net.dankito.deepthought.android.util

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.DrawerMatchers.isClosed
import android.support.test.espresso.contrib.NavigationViewActions
import android.support.test.espresso.matcher.RootMatchers
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.widget.MenuPopupWindow
import android.view.Gravity
import android.view.View
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import net.dankito.deepthought.android.R
import net.dankito.richtexteditor.command.CommandName
import net.dankito.service.search.SearchEngineBase
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf


open class UiNavigator {


    open fun createItemFromMainActivity(content: String, alsoSaveItem: Boolean = true) {
        navigateFromMainActivityToEditItemActivity()

        enterText(content)
        TestUtil.sleep(500)

        applyContentEditorChanges()

        if(alsoSaveItem) {
            saveItemInEditItemActivity()
        }
    }

    open fun createTagFromMainActivity(tagName: String) {
        navigateFromMainActivityToEditItemActivity()
        enterText(" ")
        TestUtil.sleep(1000)

        applyContentEditorChanges()
        TestUtil.sleep(500)

        createTagsInEditItemActivity(tagName)

        pressBack()
        onView(withText(R.string.action_dismiss)).inRoot(isDialog()).perform(click())
    }

    private fun createTagsInEditItemActivity(vararg tagNames: String) {
        clickOnEditItemActivityFloatingActionButton()
        onView(withId(R.id.fabEditItemTags)).perform(click())
        TestUtil.sleep(500)

        setValueOfEditEntityField(R.id.lytTagsPreview, tagNames.joinToString(SearchEngineBase.TagsSearchTermSeparator))

        TestUtil.sleep(500)
    }

    open fun createSourceFromMainActivity(sourceTitle: String) {
        navigateFromMainActivityToEditItemActivity()
        enterText(" ")
        TestUtil.sleep(1000)

        applyContentEditorChanges()
        TestUtil.sleep(500)

        createSourceInEditItemActivity(sourceTitle)

        pressBack()
        onView(withText(R.string.action_dismiss)).inRoot(isDialog()).perform(click())
    }

    private fun createSourceInEditItemActivity(sourceTitle: String, seriesTitle: String? = null) {
        clickOnEditItemActivityFloatingActionButton()
        onView(withId(R.id.fabEditItemSource)).perform(click())
        TestUtil.sleep(1000)

        clickOnEditEntityReferenceFieldEditDetailsPopupMenu(R.id.lytSourcePreview)

        setValueOfEditEntityField(R.id.lytEditSourceTitle, sourceTitle)

        seriesTitle?.let { createSeriesInEditSourceActivity(it) }

        onView(withId(R.id.mnSaveSource)).perform(click())
        TestUtil.sleep(500)
    }

    private fun createSeriesInEditSourceActivity(seriesTitle: String) {
        onView(withId(R.id.lytEditSourceSeries)).perform(click())

        clickOnEditSourceSeriesFieldEditDetailsPopupMenu(R.id.lytEditSourceSeries)

        setValueOfEditEntityField(R.id.lytEditSeriesTitle, seriesTitle)

        onView(withId(R.id.mnSaveSeries)).perform(click())
        TestUtil.sleep(500)
    }


    open fun createEntities(itemContent: String, itemSummary: String, sourceTitle: String, seriesTitle: String, vararg tagNames: String) {
        createItemFromMainActivity(itemContent, false)

        clickOnEditItemActivityFloatingActionButton()
        onView(withId(R.id.fabEditItemSummary)).perform(click())
        TestUtil.sleep(1000)
        setValueOfEditEntityField(R.id.lytSummaryPreview, itemSummary)

        createSourceInEditItemActivity(sourceTitle, seriesTitle)

        createTagsInEditItemActivity(*tagNames)

        saveItemInEditItemActivity()
    }


    open fun navigateToTabTags() {
        clickOnNavigationDrawerMenuItem(R.id.navTags)
    }

    open fun navigateToTabSources() {
        clickOnNavigationDrawerMenuItem(R.id.navSources)
    }

    open fun navigateToTabReadLaterArticles() {
        clickOnNavigationDrawerMenuItem(R.id.navReadLaterArticles)
    }

    open fun navigateToNewspaperArticlesView() {
        clickOnNavigationDrawerMenuItem(R.id.navArticleSummaryExtractors)
    }

    private fun clickOnNavigationDrawerMenuItem(itemId: Int) {
        pressHamburgerIcon()

        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(itemId))
        TestUtil.sleep(1000)
    }

    private fun pressHamburgerIcon() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed.
                .perform(DrawerActions.open()) // Open Drawer

        TestUtil.sleep(1000)
    }


    open fun navigateFromMainActivityToEditItemActivity() {
        TestUtil.sleep(1000)
        clickOnMainActivityFloatingActionButton()
    }

    open fun navigateFromMainActivityToEditItemActivityContentEditor() {
        navigateFromMainActivityToEditItemActivity()

        onView(withId(R.id.wbvwContent)).perform(click())
    }

    open fun clickOnMainActivityFloatingActionButton() {
        // FloatingActionMenu adds a FloatingActionButton without id which
        onView(AllOf.allOf(ViewMatchers.isDescendantOfA(instanceOf(FloatingActionMenu::class.java)), withId(-1),
                instanceOf(FloatingActionButton::class.java), isDisplayed()))
                .perform(ViewActions.click())
    }


    open fun navigateFromEditItemActivityContentEditorToMainActivity() {
        navigateFromEditItemActivityContentEditorToEditItemActivity()

        pressBack()
    }

    open fun navigateFromEditItemActivityContentEditorToEditItemActivity() {
        pressBack()

        onView(withText(R.string.action_dismiss)).inRoot(isDialog()).perform(click())
    }


    open fun createRssFeed(feedUrl: String, feedName: String) {
        navigateToNewspaperArticlesView()

        setEditTextText(R.id.edtxtFeedOrWebsiteUrl, feedUrl)
        onView(withId(R.id.btnCheckFeedOrWebsiteUrl)).perform(click())
        TestUtil.sleep(5000)

        onData(anything())
                .inAdapterView(withId(R.id.lstFeedSearchResults))
                .atPosition(0)
                .perform(click())
        TestUtil.sleep(6000)

        onView(withId(R.id.edtxtAskExtractorName)).inRoot(isDialog()).perform(replaceText(feedName))
        TestUtil.sleep(500)
        onView(withText(android.R.string.ok)).inRoot(isDialog()).perform(click())
        TestUtil.sleep(2000)
    }


    open fun search(query: String) {
        onView(withId(net.dankito.deepthought.android.R.id.search)).perform(click())
        TestUtil.sleep(500)

        setEditTextText(android.support.design.R.id.search_src_text, query)
    }


    open fun enterText(stringResourceId: Int) {
        enterText(InstrumentationRegistry.getInstrumentation().context.resources.getText(stringResourceId).toString())
    }

    open fun enterText(text: String) {
        InstrumentationRegistry.getInstrumentation().sendStringSync(text)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    open fun enterNewLine() {
        enterText("\n")
    }

    open fun setEditTextText(editTextId: Int, text: String) {
        // for Unicode support use replaceText() instead of typeText() (works for EditTexts)
        onView(withId(editTextId)).perform(replaceText(text))

        TestUtil.sleep(500)
    }

    fun setValueOfEditEntityField(entityFieldId: Int, value: String) {
        onView(allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(entityFieldId)))) // find edtxtEntityFieldValue in EditEntityField
                .perform(replaceText(value))

        TestUtil.sleep(500)
    }


    open fun clickOnEditorCommand(command: CommandName) {
        onView(withTagValue(`is`(command))).perform(click())
    }

    private fun saveItemInEditItemActivity() {
        onView(withId(R.id.mnSaveItem)).perform(click())
    }


    open fun clickOnEditItemActivityFloatingActionButton() {
        // FloatingActionMenu adds a FloatingActionButton without id which
        onView(allOf(ViewMatchers.isDescendantOfA(instanceOf(FloatingActionMenu::class.java)), withId(-1),
                instanceOf(FloatingActionButton::class.java), isDisplayed()))
                .perform(ViewActions.click())
    }

    private fun applyContentEditorChanges() {
        onView(withId(R.id.mnApplyHtmlChanges)).perform(click())
    }



    fun clickOnEditEntityReferenceFieldCreateNewEntityPopupMenu(editEntityReferenceFieldId: Int) {
        showEditEntityReferenceFieldPopupMenu(editEntityReferenceFieldId)

        clickOnEditEntityReferenceFieldPopupMenuItem(1)
    }

    fun clickOnEditEntityReferenceFieldEditDetailsPopupMenu(editEntityReferenceFieldId: Int) {
        showEditEntityReferenceFieldPopupMenu(editEntityReferenceFieldId)

        clickOnEditEntityReferenceFieldPopupMenuItem(0)
    }

    fun clickOnEditSourceSeriesFieldRemoveEntityPopupMenu(editSourceSeriesFieldId: Int) {
        showEditEntityReferenceFieldPopupMenu(editSourceSeriesFieldId)

        clickOnEditEntityReferenceFieldPopupMenuItem(2)
    }

    fun clickOnEditSourceSeriesFieldCreateNewEntityPopupMenu(editSourceSeriesFieldId: Int) {
        showEditEntityReferenceFieldPopupMenu(editSourceSeriesFieldId)

        clickOnEditEntityReferenceFieldPopupMenuItem(1)
    }

    fun clickOnEditSourceSeriesFieldEditDetailsPopupMenu(editSourceSeriesFieldId: Int) {
        showEditEntityReferenceFieldPopupMenu(editSourceSeriesFieldId)

        clickOnEditEntityReferenceFieldPopupMenuItem(0)
    }

    fun clickOnEditEntityReferenceFieldRemoveEntityPopupMenu(editEntityReferenceFieldId: Int) {
        showEditEntityReferenceFieldPopupMenu(editEntityReferenceFieldId)

        clickOnEditEntityReferenceFieldPopupMenuItem(2)
    }

    private fun clickOnEditEntityReferenceFieldPopupMenuItem(itemPosition: Int) {
        onData(anything())
                .inRoot(RootMatchers.isPlatformPopup()) // isPlatformPopup() == is in PopupWindow
                .inAdapterView(instanceOf(MenuPopupWindow.MenuDropDownListView::class.java))
                .atPosition(itemPosition)
                .perform(click())
    }

    private fun showEditEntityReferenceFieldPopupMenu(editEntityReferenceFieldId: Int) {
        onView(allOf(withId(R.id.btnEntityFieldAction), isDescendantOfA(withId(editEntityReferenceFieldId)))) // find btnEntityFieldAction in EditEntityReferenceField
                .perform(object : ViewAction {

                    override fun getConstraints(): Matcher<View> {
                        return ViewMatchers.isEnabled() // no constraints, they are checked above
                    }

                    override fun getDescription(): String {
                        return "Click btnEntityFieldAction which is not fully (> 90 %) visible"
                    }

                    override fun perform(uiController: UiController?, view: View?) {
                        view?.performClick()
                    }

                })
    }


    open fun hideKeyboard() {
        Espresso.closeSoftKeyboard()
    }

    open fun pressBack() {
        Espresso.pressBack()
    }

}