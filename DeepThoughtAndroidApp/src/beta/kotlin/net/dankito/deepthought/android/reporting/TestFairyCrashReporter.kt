package net.dankito.deepthought.android.reporting

import android.content.Context
import com.testfairy.TestFairy
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import net.dankito.deepthought.service.data.DataManager
import java.util.*


class TestFairyCrashReporter : ICrashReporter {

    override fun init(context: Context, dataManager: DataManager) {
        // set your TestFairy app token in  or simply uncomment this lines
        getAppToken(context)?.let { appToken ->
            TestFairy.begin(context, appToken)

            dataManager.addInitializationListener {
                TestFairy.identify(dataManager.localDevice.id)
                TestFairy.setUserId(dataManager.localUser.id)
            }
        }
    }

    private fun getAppToken(context: Context): String? {
        try {
            val resources = context.resources

            val testFairyProperties = Properties()
            testFairyProperties.load(resources.openRawResource(R.raw.testfairy))

            return testFairyProperties.getProperty("app.token")
        } catch(e: Exception) {

        }

        return null
    }

}