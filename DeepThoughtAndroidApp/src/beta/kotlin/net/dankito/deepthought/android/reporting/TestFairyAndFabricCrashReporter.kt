package net.dankito.deepthought.android.reporting

import android.content.Context
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import net.dankito.deepthought.service.data.DataManager


class TestFairyAndFabricCrashReporter : ICrashReporter {

    private val testFairyCrashReporter = TestFairyCrashReporter()

    private val fabricCrashReporter = FabricCrashReporter()


    override fun init(context: Context, dataManager: DataManager) {
        testFairyCrashReporter.init(context, dataManager)

        fabricCrashReporter.init(context, dataManager)
    }

}