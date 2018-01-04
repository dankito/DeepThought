package net.dankito.deepthought.android.util

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.view.View
import org.hamcrest.Matcher


class Assert {

    companion object {

        fun viewIsVisible(viewId: Int) {
            viewIsVisible(withId(viewId))
        }

        fun viewIsVisible(viewMatcher: Matcher<View>) {
            onView(viewMatcher).check(matches(isDisplayed()))
        }


        fun viewIsNotVisible(viewId: Int) {
            viewIsNotVisible(withId(viewId))
        }

        fun viewIsNotVisible(viewMatcher: Matcher<View>) {
            onView(viewMatcher).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        }

    }

}