package net.dankito.deepthought.android.util

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import net.dankito.deepthought.android.R
import net.dankito.richtexteditor.android.command.Command
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.core.AllOf


open class UiNavigator {


    open fun createItemFromMainActivity(content: String, alsoSaveItem: Boolean = true) {
        navigateFromMainActivityToEditItemActivityContentEditor()

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
        onView(withId(R.id.fabEditEntryTags)).perform(click())
        TestUtil.sleep(500)

        tagNames.forEach { tagName ->
            setEditTextText(R.id.edtxtEditEntrySearchTag, tagName)
            onView(withId(R.id.btnEditEntryCreateOrToggleTags)).perform(click())
            TestUtil.sleep(1000)
        }

        onView(withId(R.id.mnApplyTagsOnEntryChanges)).perform(click())
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
        onView(withId(R.id.fabEditEntryReference)).perform(click())
        TestUtil.sleep(1000)

        setValueOfEditEntityField(R.id.lytEditReferenceTitle, sourceTitle)

        seriesTitle?.let { createSeriesInEditSourceActivity(it) }

        onView(withId(R.id.mnSaveReference)).perform(click())
        TestUtil.sleep(500)
    }

    private fun createSeriesInEditSourceActivity(seriesTitle: String) {
        onView(withId(R.id.lytEditReferenceSeries)).perform(click())

        setValueOfEditEntityField(R.id.lytEditSeriesTitle, seriesTitle)

        onView(withId(R.id.mnSaveSeries)).perform(click())
        TestUtil.sleep(500)
    }


    open fun createEntities(itemContent: String, itemSummary: String, sourceTitle: String, seriesTitle: String, vararg tagNames: String) {
        createItemFromMainActivity(itemContent, false)

        clickOnEditItemActivityFloatingActionButton()
        onView(withId(R.id.fabEditEntryAbstract)).perform(click())
        TestUtil.sleep(1000)
        setValueOfEditEntityField(R.id.lytAbstractPreview, itemSummary)

        createSourceInEditItemActivity(sourceTitle, seriesTitle)

        createTagsInEditItemActivity(*tagNames)

        saveItemInEditItemActivity()
    }


    open fun navigateToTabItems() {
        onView(withId(net.dankito.deepthought.android.R.id.btnvEntries)).perform(click())
        TestUtil.sleep(1000)
    }

    open fun navigateToTabTags() {
        onView(withId(net.dankito.deepthought.android.R.id.btnvTags)).perform(click())
        TestUtil.sleep(1000)
    }

    open fun navigateToTabSources() {
        onView(withId(net.dankito.deepthought.android.R.id.btnvReferences)).perform(click())
        TestUtil.sleep(1000)
    }

    open fun navigateToTabReadLaterArticles() {
        onView(withId(net.dankito.deepthought.android.R.id.btnvReadLaterArticles)).perform(click())
        TestUtil.sleep(1000)
    }


    open fun navigateFromMainActivityToEditItemActivity() {
        TestUtil.sleep(1000)
        clickOnMainActivityFloatingActionButton()
        onView(withId(R.id.fab_add_entry)).perform(click())
    }

    open fun navigateFromMainActivityToEditItemActivityContentEditor() {
        navigateFromMainActivityToEditItemActivity()

        onView(ViewMatchers.withId(R.id.editor)).perform(click())
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
        // for Unicode support use replaceText() instead of typeText() (works for for EditTexts)
        onView(withId(editTextId)).perform(replaceText(text))

        TestUtil.sleep(500)
    }

    private fun setValueOfEditEntityField(entityFieldId: Int, value: String) {
        onView(AllOf.allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(entityFieldId)))) // find edtxtEntityFieldValue in EditEntityField
                .perform(replaceText(value))

        TestUtil.sleep(500)
    }


    open fun clickOnEditorCommand(command: Command) {
        onView(withTagValue(`is`(command))).perform(click())
    }

    private fun saveItemInEditItemActivity() {
        onView(withId(R.id.mnSaveEntry)).perform(click())
    }


    open fun clickOnEditItemActivityFloatingActionButton() {
        // FloatingActionMenu adds a FloatingActionButton without id which
        onView(AllOf.allOf(ViewMatchers.isDescendantOfA(instanceOf(FloatingActionMenu::class.java)), withId(-1),
                instanceOf(FloatingActionButton::class.java), isDisplayed()))
                .perform(ViewActions.click())
    }

    private fun applyContentEditorChanges() {
        onView(withId(R.id.mnApplyHtmlChanges)).perform(click())
    }


    open fun hideKeyboard() {
        Espresso.closeSoftKeyboard()
    }

    open fun pressBack() {
        Espresso.pressBack()
    }

}