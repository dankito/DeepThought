package net.dankito.deepthought.android.util

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import net.dankito.deepthought.android.R
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf


open class UiNavigator {


    open fun createEntry(content: String) {
        navigateFromMainActivityToEditItemActivityContentEditor()

        enterText(content)

        Espresso.onView(ViewMatchers.withId(R.id.mnApplyHtmlChanges)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.mnSaveEntry)).perform(ViewActions.click())
    }

    open fun navigateFromMainActivityToEditItemActivity() {
        TestUtil.sleep(1000)
        clickOnMainActivityFloatingActionButton()
        Espresso.onView(ViewMatchers.withId(R.id.fab_add_entry)).perform(ViewActions.click())
    }

    open fun navigateFromMainActivityToEditItemActivityContentEditor() {
        navigateFromMainActivityToEditItemActivity()

        TestUtil.sleep(2000)
        Espresso.onView(ViewMatchers.withId(R.id.editor)).perform(ViewActions.click())
    }

    open fun clickOnMainActivityFloatingActionButton() {
        // FloatingActionMenu adds a FloatingActionButton without id which
        Espresso.onView(AllOf.allOf(ViewMatchers.isDescendantOfA(CoreMatchers.instanceOf(FloatingActionMenu::class.java)), ViewMatchers.withId(-1),
                CoreMatchers.instanceOf(FloatingActionButton::class.java), isDisplayed()))
                .perform(ViewActions.click())
    }

    open fun enterText(text: String) {
        InstrumentationRegistry.getInstrumentation().sendStringSync(text)
    }
}