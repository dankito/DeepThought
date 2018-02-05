package net.dankito.deepthought.android.service.permissions

import android.Manifest
import android.content.Context
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.service.permissions.IPermissionsService


class AndroidPermissionsService(private val applicationContext: Context, private val activityTracker: CurrentActivityTracker) : IPermissionsService {

    companion object {
        private const val WriteSynchronizedFilePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }


    override fun hasPermissionToWriteFiles(): Boolean {
        return PermissionsManager.isPermissionGranted(applicationContext, WriteSynchronizedFilePermission)
    }

    override fun requestPermissionToWriteSynchronizedFiles(requestPermissionResult: (Boolean) -> Unit) {
        val currentActivity = activityTracker.currentActivity

        if(currentActivity != null) {
            requestPermissionToWriteSynchronizedFiles(currentActivity, requestPermissionResult)
        }
        else {
            activityTracker.addNextActivitySetListener {
                requestPermissionToWriteSynchronizedFiles(it, requestPermissionResult)
            }
        }
    }

    private fun requestPermissionToWriteSynchronizedFiles(currentActivity: BaseActivity, requestPermissionResult: (Boolean) -> Unit) {
        val permissionsManager = PermissionsManager(currentActivity)
        val rational = currentActivity.getString(R.string.request_write_synchronized_file_permission_rational)

        permissionsManager.requestPermission(WriteSynchronizedFilePermission, rational) { _, isGranted ->
            requestPermissionResult(isGranted)
        }
    }

}