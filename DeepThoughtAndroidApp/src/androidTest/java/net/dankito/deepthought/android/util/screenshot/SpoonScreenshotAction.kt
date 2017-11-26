package net.dankito.deepthought.android.util.screenshot

import android.app.Activity
import android.content.ContextWrapper
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.ViewMatchers.isRoot
import android.view.View
import com.jraska.falcon.FalconSpoon
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.io.File

/**
 * Class to take screenshots using Spoon library from an Espresso test
 * Original code from Gist: https://gist.github.com/edenman/7fdd32a4d59ccc01185b
 */
/**
 * Initialize with information required to take a screenshot
 * @param tag Name of the screenshot to include in the file name
 * @param testClass Name of the class taking the screenshot (required by Spoon library)
 * @param testMethod Name of the method taking the screenshot
 */
class SpoonScreenshotAction(private val mTag: String, private val mTestClass: String, private val mTestMethod: String) : ViewAction {

    companion object {
        /**
         * Get the last captured screenshot file
         * @return Last screenshot file handler or null if there was no screenshot taken
         */
        var lastScreenshot: File? = null
            private set

        /**
         * Get the activity from the context of the view
         * @param view View from which the activity will be inferred
         * @return Activity that contains the given view
         */
        private fun getActivity(view: View): Activity {
            var context = view.context
            while (context !is Activity) {
                if (context is ContextWrapper) {
                    context = context.baseContext
                } else {
                    throw IllegalStateException("Got a context of class " + context.javaClass + " and I don't know how to get the Activity from it")
                }
            }
            return context
        }

        /**
         * Espresso action to be take a screenshot of the current activity
         * This must be called directly from the test method
         * @param tag Name of the screenshot to include in the file name
         */
        fun perform(tag: String) {
            val trace = Thread.currentThread().stackTrace
            val testClass = trace[3].className
            val testMethod = trace[3].methodName

            perform(tag, testClass, testMethod)
        }

        /**
         * Espresso action to be take a screenshot of the current activity
         * This must be called directly from the test method
         * @param tag Name of the screenshot to include in the file name
         */
        fun perform(tag: String, testClass: String, method: String) {
            onView(isRoot()).perform(SpoonScreenshotAction(tag, testClass, method))
        }
    }


    override fun getConstraints(): Matcher<View> {
        return Matchers.any(View::class.java)
    }

    override fun getDescription(): String {
        return "Taking a screenshot using spoon."
    }

    override fun perform(uiController: UiController, view: View) {
        lastScreenshot = FalconSpoon.screenshot(getActivity(view), mTag, mTestClass, mTestMethod)
    }

}
