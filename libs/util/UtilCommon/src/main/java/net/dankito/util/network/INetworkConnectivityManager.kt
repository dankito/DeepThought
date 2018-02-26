package net.dankito.util.network

import java.net.Inet4Address


interface INetworkConnectivityManager {

    fun getBroadcastAddresses(): Collection<Inet4Address>


    fun addNetworkInterfaceConnectivityChangedListener(listener: (NetworkInterfaceState) -> Unit)

    fun removeNetworkInterfaceConnectivityChangedListener(listener: (NetworkInterfaceState) -> Unit)

}