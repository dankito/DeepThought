package net.dankito.deepthought.android.service.reporting

import android.content.Context
import net.dankito.deepthought.service.data.DataManager


class NoOpCrashReporter : ICrashReporter {

    override fun init(context: Context, dataManager: DataManager) { }

}