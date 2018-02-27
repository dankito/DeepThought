package net.dankito.synchronization.model


data class DiscoveredDevice(var device: Device, var address: String, var messagesPort: Int = 0, var synchronizationPort: Int = 0, var fileSynchronizationPort: Int = 0) {

    override fun toString(): String {
        return "" + device
    }

}
