package net.dankito.deepthought.model


interface NetworkSettingsChangedListener {

    fun settingsChanged(networkSettings: NetworkSettings, setting: NetworkSetting, newValue: Any, oldValue: Any?)

}
