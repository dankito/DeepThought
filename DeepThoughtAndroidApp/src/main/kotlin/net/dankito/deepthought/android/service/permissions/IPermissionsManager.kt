package net.dankito.deepthought.android.service.permissions

import android.Manifest


interface IPermissionsManager {

    /**
     * To be called from the Activity passed as parameter to PermissionManager's constructor.
     * Simple pass all parameters passed to Activity's onRequestPermissionsResult() to this method.
     *
     * Responses to permission requests are sent to an Activity, so there's no other way then doing it that cumbersome.
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)

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
    fun checkPermission(permission: String, rationaleToShowToUserResourceId: Int, callback: (String, Boolean) -> Unit)

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
    fun checkPermission(permission: String, rationaleToShowToUser: String, callback: (String, Boolean) -> Unit)

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
    fun checkPermissions(permissions: Array<String>, rationalesToShowToUser: Array<String>, callback: (Map<String, Boolean>) -> Unit)

    /**
     * Checks if a permission is already granted.
     *
     * @param permission A value from [Manifest.permission]
     * @return
     */
    fun isPermissionGranted(permission: String): Boolean

    fun requestPermission(permission: String, rationaleToShowToUser: String, callback: (String, Boolean) -> Unit)

}