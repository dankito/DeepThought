package net.dankito.util.network

import java.util.*
import kotlin.concurrent.schedule


/**
 * A really stupid implementation for checking if a network interface connectivity changed.
 * As Java doesn't provide a network connectivity changed listener as Android does,
 * simply periodically checks network interface states.
 */
class JavaNetworkConnectivityManager(networkHelper: NetworkHelper) : NetworkConnectivityManagerBase(networkHelper) {

    companion object {
        private const val CheckForNetworkInterfaceChangesPeriodMillis = 60 * 1000L
    }


    init {
        Timer().schedule(CheckForNetworkInterfaceChangesPeriodMillis, CheckForNetworkInterfaceChangesPeriodMillis) { networkInterfacesChanged() }
    }

}