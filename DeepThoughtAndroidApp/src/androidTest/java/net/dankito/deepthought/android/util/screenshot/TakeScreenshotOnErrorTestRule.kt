package net.dankito.deepthought.android.util.screenshot

import android.graphics.Bitmap
import android.os.Environment
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getTargetContext
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class TakeScreenshotOnErrorTestRule : TestWatcher() {

    companion object {

        private val TIME_ERROR_OCCURRED_DATE_FORMAT = SimpleDateFormat("yyyy.MM.dd_HH:mm:ss.SSS")

        private val log = LoggerFactory.getLogger(TakeScreenshotOnErrorTestRule::class.java)
    }


    protected val pathForScreenshots: File
        get() {
            var path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ui_test_screenshots")
            path = File(path, getTargetContext().packageName)

            if (!path.exists()) {
                path.mkdirs()
            }

            return path
        }


    override fun failed(e: Throwable, description: Description) {
        super.failed(e, description)

        takeScreenshot(e, description)
    }

    protected fun takeScreenshot(e: Throwable, description: Description) {
        takeScreenshotWithUiAutomation(description)

        takeScreenshotWithSpoon(e, description)
    }

    protected fun takeScreenshotWithSpoon(e: Throwable, description: Description) {
        try {
            SpoonScreenshotAction.perform("TestFailed", description.className, description.methodName)
        } catch (ex: Exception) {
            log.error("Could not take screenshot with Spoon for Throwable " + e, ex)
        }

    }


    protected fun takeScreenshotWithUiAutomation(description: Description) {
        // Take advantage of UiAutomator screenshot method
        try {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            val screenshot = uiAutomation.takeScreenshot()

            val path = pathForScreenshots
            val filename = getFilenameForScreenshot(description)
            val fileOutputStream = FileOutputStream(File(path, filename))

            screenshot.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        } catch (ex: Exception) {
            log.error("Could not take screenshot with UiAutomation", ex)
        }

    }

    private fun getFilenameForScreenshot(description: Description): String {
        //            String filename = description.getClassName() + "-" + description.getMethodName() + ".png";
        var filename = description.className + "-"

        if (description.methodName.length > 20) {
            filename += description.methodName.substring(0, 20)
        } else {
            filename += description.methodName
        }

        return TIME_ERROR_OCCURRED_DATE_FORMAT.format(Date()) + "_" + filename + ".png"
    }

}
