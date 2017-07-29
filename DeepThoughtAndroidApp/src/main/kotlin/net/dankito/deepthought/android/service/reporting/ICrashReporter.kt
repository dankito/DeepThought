package net.dankito.deepthought.android.service.reporting

import android.content.Context
import net.dankito.deepthought.service.data.DataManager


interface ICrashReporter {

    fun init(context: Context, dataManager: DataManager)

}