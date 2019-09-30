package net.dankito.deepthought.android.activities

import android.os.Bundle
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.ui.UiStatePersister
import net.dankito.deepthought.android.ui.theme.AppThemes
import net.dankito.utils.android.permissions.IPermissionsService
import net.dankito.utils.android.permissions.PermissionsService
import net.dankito.utils.android.ui.activities.ActivityParameterHolder
import net.dankito.utils.android.ui.theme.Theme
import net.dankito.utils.windowregistry.android.ui.AndroidWindow
import net.dankito.utils.windowregistry.window.WindowRegistry
import javax.inject.Inject


abstract class BaseActivity : AndroidWindow() {

    companion object {
        const val WaitingForResultForIdBundleExtraName = "WAITING_FOR_RESULT_FOR_ID"
    }


    @Inject
    protected lateinit var parameterHolderField: ActivityParameterHolder

    @Inject
    protected lateinit var uiStatePersister: UiStatePersister

    @Inject
    protected lateinit var windowRegistryField: WindowRegistry


    private var waitingForResultWithId: String? = null

    private val registeredPermissionsServices = mutableListOf<IPermissionsService>()


    override fun getParameterHolder(): ActivityParameterHolder {
        return parameterHolderField
    }

    override fun getWindowRegistryInstance(): WindowRegistry {
        return windowRegistryField
    }


    override fun setupDependencyInjection() {
        super.setupDependencyInjection()

        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            this.waitingForResultWithId = savedInstanceState.getString(WaitingForResultForIdBundleExtraName)
        }
    }

    override fun onDestroy() {
        getParametersId()?.let { parametersId ->
            parameterHolderField.clearParameters(parametersId)
        }

        registeredPermissionsServices.clear()

        super.onDestroy()
    }


    override fun getThemeForName(themeName: String): Theme? {
        if (AppThemes.Dark.name == themeName) {
            return AppThemes.Dark
        }

        return AppThemes.Light
    }

    protected fun changeTheme(useDarkTheme: Boolean) {
        setTheme( if (useDarkTheme) AppThemes.Dark else AppThemes.Light )
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(WaitingForResultForIdBundleExtraName, null)
            waitingForResultWithId?.let { outState.putString(WaitingForResultForIdBundleExtraName, it) }
        }
    }


    protected fun getParameters(): Any? {
        getParametersId()?.let { parametersId ->
            return parameterHolderField.getParameters(parametersId) // we're done with activity. remove parameters from cache to not waste any memory
        }

        return null
    }


    internal fun setWaitingForResult(targetResultId: String) {
        waitingForResultWithId = targetResultId

        parameterHolderField.clearActivityResults(targetResultId)
    }

    protected fun getAndClearResult(targetResultId: String): Any? {
        val result = parameterHolderField.getActivityResult(targetResultId)

        parameterHolderField.clearActivityResults(targetResultId)

        return result
    }

    protected fun clearAllActivityResults() {
        parameterHolderField.clearActivityResults()
    }


    /**
     * Sometimes objects are too large for bundle in onSaveInstance() which would crash application, so save them in-memory as almost always activity gets destroyed but not Application
     */

    protected fun serializeStateToDiskIfNotNull(outState: Bundle, bundleKey: String, state: Any?) {
        uiStatePersister.serializeStateToDiskIfNotNull(outState, bundleKey, state)
    }

    protected fun<T> restoreStateFromDisk(savedInstanceState: Bundle, bundleKey: String, stateClass: Class<T>): T? {
        return uiStatePersister.restoreStateFromDisk(savedInstanceState, bundleKey, stateClass)
    }


    /*          Permissions handling            */

    fun registerPermissionsService(): IPermissionsService {
        val permissionsService = PermissionsService(this)

        registeredPermissionsServices.add(permissionsService)

        return permissionsService
    }

    fun unregisterPermissionsService(permissionsService: IPermissionsService) {
        registeredPermissionsServices.remove(permissionsService)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        registeredPermissionsServices.forEach { permissionsService ->
            permissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}