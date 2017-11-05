package net.dankito.deepthought.android.reporting

import android.content.Context
import com.appsee.Appsee
import com.crashlytics.android.BuildConfig
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import net.dankito.deepthought.service.data.DataManager


class FabricCrashReporter : ICrashReporter {

    override fun init(context: Context, dataManager: DataManager) {
        // Set up Crashlytics, disabled for debug builds (see https://docs.fabric.io/android/crashlytics/build-tools.html)
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(context, crashlyticsKit)

        Appsee.start("9e1f961ecef546e98e20eb1a8d498636")

        dataManager.addInitializationListener {
            Crashlytics.setUserIdentifier(dataManager.localDevice.id)
            Crashlytics.setUserName(dataManager.localUser.id)

            Appsee.setUserId(dataManager.localUser.id)
        }
    }

}