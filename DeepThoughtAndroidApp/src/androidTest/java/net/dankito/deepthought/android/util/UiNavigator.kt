package net.dankito.deepthought.android.util

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
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


    open fun createEntry(content: String) {
        navigateFromMainActivityToEditItemActivityContentEditor()

        enterText(content)

        onView(withId(R.id.mnApplyHtmlChanges)).perform(click())
        onView(withId(R.id.mnSaveEntry)).perform(click())
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
        TestUtil.sleep(500L)

        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText(query))
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


    open fun clickOnEditorCommand(command: Command) {
        onView(withTagValue(`is`(command))).perform(click())
    }


    open fun hideKeyboard() {
        Espresso.closeSoftKeyboard()
    }

    open fun pressBack() {
        Espresso.pressBack()
    }

}