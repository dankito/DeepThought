package net.dankito.deepthought.model


interface NetworkSettingsChangedListener {

    fun settingsChanged(networkSettings: INetworkSettings, setting: NetworkSetting, newValue: Any, oldValue: Any?)

}
