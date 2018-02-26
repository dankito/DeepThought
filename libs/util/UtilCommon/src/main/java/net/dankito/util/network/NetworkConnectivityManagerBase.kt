package net.dankito.util.network

import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


abstract class NetworkConnectivityManagerBase(protected val networkHelper: NetworkHelper) : INetworkConnectivityManager {


    val networkInterfaces = ConcurrentHashMap<String, NetworkInterfaceState>()

    val listeners = CopyOnWriteArrayList<(NetworkInterfaceState) -> Unit>()


    init {
        networkHelper.getRealNetworkInterfaces().forEach { nic ->
            networkInterfaces.put(nic.name, getNetworkInterfaceState(nic))
        }
    }


    override fun getBroadcastAddresses(): Collection<Inet4Address> {
        return networkInterfaces.values.filter { it.isUp && it.broadcastAddress != null }.map { it.broadcastAddress!! }
    }

    protected open fun networkInterfacesChanged() {
        val changedInterfaces = networkHelper.getRealNetworkInterfaces()
        val removedInterfaces = ArrayList(networkInterfaces.values)

        changedInterfaces.forEach { changedInterface ->
            val knownInterfaceState = checkChangedNetworkInterface(changedInterface)

            removedInterfaces.remove(knownInterfaceState)
        }

        removedInterfaces.forEach {
            networkInterfaces.remove(it.name)
            networkInterfaceRemoved(NetworkInterfaceState(it.name, false, it.ipV4Addresses, it.broadcastAddress)) // so that UdpDevicesDiscoverer knows which broadcast address to stop
        }
    }

    protected open fun checkChangedNetworkInterface(changedInterface: NetworkInterface): NetworkInterfaceState? {
        val knownInterfaceState = networkInterfaces.get(changedInterface.name)
        val changedInterfaceState = getNetworkInterfaceState(changedInterface)

        if(knownInterfaceState == null) {
            networkInterfaces.put(changedInterface.name, changedInterfaceState)
            newNetworkInterfaceAdded(changedInterfaceState)
        }
        else if(didNetworkInterfaceChange(knownInterfaceState, changedInterfaceState)) {
            networkInterfaces.put(knownInterfaceState.name, changedInterfaceState)
            networkInterfaceStateChanged(knownInterfaceState, changedInterfaceState)
        }

        return knownInterfaceState
    }

    protected open fun didNetworkInterfaceChange(knownInterfaceState: NetworkInterfaceState, changedInterfaceState: NetworkInterfaceState): Boolean {
        // TODO: may also check ip addresses
        return knownInterfaceState.isUp != changedInterfaceState.isUp || knownInterfaceState.broadcastAddress != changedInterfaceState.broadcastAddress
    }

    protected open fun networkInterfaceStateChanged(knownInterfaceState: NetworkInterfaceState, changedInterfaceState: NetworkInterfaceState) {
        knownInterfaceState.isUp = changedInterfaceState.isUp

        if(knownInterfaceState.broadcastAddress == null) {
            knownInterfaceState.broadcastAddress = changedInterfaceState.broadcastAddress

            knownInterfaceState.ipV4Addresses.clear()
            knownInterfaceState.ipV4Addresses.addAll(changedInterfaceState.ipV4Addresses)
        }

        callNetworkInterfaceConnectivityChangedListeners(knownInterfaceState)
    }

    protected open fun newNetworkInterfaceAdded(addedNetworkInterfaceState: NetworkInterfaceState) {
        callNetworkInterfaceConnectivityChangedListeners(addedNetworkInterfaceState)
    }

    protected open fun networkInterfaceRemoved(removedNetworkInterfaceState: NetworkInterfaceState) {
        callNetworkInterfaceConnectivityChangedListeners(removedNetworkInterfaceState)
    }


    protected open fun getNetworkInterfaceState(nic: NetworkInterface): NetworkInterfaceState {
        return NetworkInterfaceState(nic.name, nic.isUp, networkHelper.getIPAddresses(nic, true).toMutableList(), networkHelper.getBroadcastAddress(nic))
    }


    override fun addNetworkInterfaceConnectivityChangedListener(listener: (NetworkInterfaceState) -> Unit) {
        listeners.add(listener)
    }

    override fun removeNetworkInterfaceConnectivityChangedListener(listener: (NetworkInterfaceState) -> Unit) {
        listeners.remove(listener)
    }

    protected open fun callNetworkInterfaceConnectivityChangedListeners(state: NetworkInterfaceState) {
        listeners.forEach { it.invoke(state) }
    }

}