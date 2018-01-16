package net.dankito.deepthought.android.service.permissions


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class PermissionsManager(private val activity: Activity) : IPermissionsManager {

    companion object {
        /**
         * Static version in case no Activity instance is available.
         *
         * @param context
         * @param permission
         * @return
         */
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return PackageManager.PERMISSION_GRANTED == context.checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid())
        }
    }


    private var nextRequestCode = 27388

    private val pendingPermissionRequests = ConcurrentHashMap<String, MutableList<(String, Boolean) -> Unit>>()


    /**
     * To be called from [.activity].
     * Simple pass all parameters passed to Activity's onRequestPermissionsResult() to this method.
     *
     * Responses to permission requests are sent to an Activity, so there's no other way then doing it that cumbersome.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if(permissions.size == 0) { // unbelievable, on Cyanogen it can happen, that permissions is an empty array
            return
        }

        val permission = permissions[0]
        var callbacks: List<(String, Boolean) -> Unit>? = null

        synchronized(pendingPermissionRequests) {
            callbacks = pendingPermissionRequests.remove(permission)
        }

        callbacks?.let {
            if(permissions.size > 0 && grantResults != null && grantResults.size > 0) {
                val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                callbacks?.forEach { callback ->
                    callback(permission, permissionGranted)
                }
            }
        }
    }


    /**
     * Checks first if the `permission` is granted. If so, returns immediately.
     *
     * If not, checks if permission has been requested before. If so, User has to be shown a rationale why she/he's being re-asked.<br></br>
     * If permission has never been requested before or User allows re-requesting it, a permission request will be passed on to User.
     *
     * @param permission A value from [Manifest.permission]
     * @param rationaleToShowToUserResourceId The string resource id of rationale shown to User before re-requesting permission.
     * @param callback The callback being called when determined if permission is granted or not.
     */
    override fun checkPermission(permission: String, rationaleToShowToUserResourceId: Int, callback: (String, Boolean) -> Unit) {
        checkPermission(permission, activity.resources.getString(rationaleToShowToUserResourceId), callback)
    }

    /**
     * Checks first if the `permission` is granted. If so, returns immediately.
     *
     * If not, checks if permission has been requested before. If so, User has to be shown a rationale why she/he's being re-asked.<br></br>
     * If permission has never been requested before or User allows re-requesting it, a permission request will be passed on to User.
     *
     * @param permission A value from [Manifest.permission]
     * @param rationaleToShowToUser The rationale shown to User before re-requesting permission.
     * @param callback The callback being called when determined if permission is granted or not.
     */
    override fun checkPermission(permission: String, rationaleToShowToUser: String, callback: (String, Boolean) -> Unit) {
        if(isRunningOnUiThread == false) {
            checkPermissionOnNonUiThread(permission, rationaleToShowToUser, callback)
        }
        else {
            Thread(Runnable { checkPermissionOnNonUiThread(permission, rationaleToShowToUser, callback) }).start()
        }
    }

    private fun checkPermissionOnNonUiThread(permission: String, rationaleToShowToUser: String, callback: (String, Boolean) -> Unit) {
        if(isPermissionGranted(permission)) {
            callback(permission, true)
        }
        else {
            requestPermission(permission, rationaleToShowToUser, callback)
        }
    }

    /**
     * Checks for each permission first if the `permission` is granted. If so, returns immediately.
     *
     * If not, checks if permission has been requested before. If so, User has to be shown a rationale why she/he's being re-asked.<br></br>
     * If permission has never been requested before or User allows re-requesting it, a permission request will be passed on to User.
     *
     * @param permissions A value from [Manifest.permission]
     * @param rationalesToShowToUser The rationales shown to User before re-requesting permission.
     * @param callback The callback being called when determined if permission is granted or not.
     */
    override fun checkPermissions(permissions: Array<String>, rationalesToShowToUser: Array<String>, callback: (Map<String, Boolean>) -> Unit) {
        if(isRunningOnUiThread == false) {
            checkPermissionsOnNonUiThread(permissions, rationalesToShowToUser, callback)
        }
        else {
            Thread(Runnable { checkPermissionsOnNonUiThread(permissions, rationalesToShowToUser, callback) }).start()
        }
    }

    /**
     * countDownLatch.await() blocks current thread -> do not block calling thread
     * @param permissions
     * @param rationalesToShowToUser
     * @param callback
     */
    private fun checkPermissionsOnNonUiThread(permissions: Array<String>, rationalesToShowToUser: Array<String>, callback: (Map<String, Boolean>) -> Unit) {
        val permissionResults = ConcurrentHashMap<String, Boolean>()
        val countDownLatch = CountDownLatch(permissions.size)

        for(i in permissions.indices) {
            val permission = permissions[i]

            if(isPermissionGranted(permission)) {
                permissionResults.put(permission, true)
                countDownLatch.countDown()
            }
            else {
                requestPermission(permission, rationalesToShowToUser[i]) { _, isGranted ->
                    permissionResults.put(permission, isGranted)
                    countDownLatch.countDown()
                }
            }
        }

        try {
            countDownLatch.await()
        } catch (ignored: Exception) { }

        callback(permissionResults)
    }

    /**
     * Checks if a permission is already granted.
     *
     * @param permission A value from [Manifest.permission]
     * @return
     */
    override fun isPermissionGranted(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity, permission)
    }


    /**
     * Checks if permission has been requested before.<br></br>
     * If so, shows a rationale to User why permission is re-request.<br></br>
     * If not, requests the permission directly from User.
     * @param permission A value from [Manifest.permission]
     * @param rationaleToShowToUser The rationale shown to User before re-requesting permission.
     * @param callback The callback being called when determined if permission is granted or not.
     */
    override fun requestPermission(permission: String, rationaleToShowToUser: String, callback: (String, Boolean) -> Unit) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            showRationale(permission, rationaleToShowToUser, callback)

        }
        else {
            // permission has not been granted yet. Request it directly.
            requestPermissionFromUser(permission, callback)
        }
    }

    /**
     * Calls [.showRationaleThreadSafe] on UI thread.
     * @param permission A value from [Manifest.permission]
     * @param rationaleToShowToUser The rationale shown to User before re-requesting permission.
     * @param callback The callback being called when determined if permission is granted or not.
     */
    private fun showRationale(permission: String, rationaleToShowToUser: String, callback: (String, Boolean) -> Unit) {
        activity.runOnUiThread { showRationaleThreadSafe(permission, rationaleToShowToUser, callback) }
    }

    /**
     * Shows rationale to User why permission is re-requested.<br></br>
     * If User allows re-requesting, [.requestPermissionFromUser] is called.
     * @param permission A value from [Manifest.permission]
     * @param rationaleToShowToUser The rationale shown to User before re-requesting permission.
     * @param callback The callback being called when determined if permission is granted or not.
     */
    private fun showRationaleThreadSafe(permission: String, rationaleToShowToUser: String, callback: (String, Boolean) -> Unit) {
        AlertDialog.Builder(activity)
                .setMessage(rationaleToShowToUser)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes) { dialog, which -> requestPermissionFromUser(permission, callback) }.show()
    }

    /**
     * Shows request for permission to User.
     * @param permission A value from [Manifest.permission]
     * @param callback The callback being called when determined if permission is granted or not.
     */
    private fun requestPermissionFromUser(permission: String, callback: (String, Boolean) -> Unit) {
        synchronized(pendingPermissionRequests) {
            if(pendingPermissionRequests.containsKey(permission)) { // there's already a pending requestPermissions() call for this permission -> don't ask again, add to pending permissions
                pendingPermissionRequests[permission]?.add(callback)
            }
            else {
                val requestCode = nextRequestCode++

                val callbacksForPermission = ArrayList<(String, Boolean) -> Unit>()
                callbacksForPermission.add(callback)
                pendingPermissionRequests.put(permission, callbacksForPermission)

                thread {
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(permission),
                            requestCode)
                }
            }
        }
    }


    private val isRunningOnUiThread: Boolean
        get() = Looper.getMainLooper().thread === Thread.currentThread()

}