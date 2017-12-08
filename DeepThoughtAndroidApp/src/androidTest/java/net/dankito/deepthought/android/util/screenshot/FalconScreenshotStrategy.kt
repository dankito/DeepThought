package net.dankito.deepthought.android.util.screenshot

import com.jraska.falcon.Falcon

import net.dankito.deepthought.android.DeepThoughtActivityTestRule

import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.ScreenshotStrategy


class FalconScreenshotStrategy(private val activityTestRule: DeepThoughtActivityTestRule<*>) : ScreenshotStrategy {

    override fun takeScreenshot(screenshotName: String, screenshotCallback: ScreenshotCallback) {
        screenshotCallback.screenshotCaptured(screenshotName, Falcon.takeScreenshotBitmap(activityTestRule.activity))
    }

}
