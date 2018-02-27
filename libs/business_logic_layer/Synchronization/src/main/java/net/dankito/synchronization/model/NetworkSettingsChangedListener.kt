package net.dankito.synchronization.model


interface NetworkSettingsChangedListener {

    fun settingsChanged(networkSettings: NetworkSettings, setting: NetworkSetting, newValue: Any, oldValue: Any?)

}
