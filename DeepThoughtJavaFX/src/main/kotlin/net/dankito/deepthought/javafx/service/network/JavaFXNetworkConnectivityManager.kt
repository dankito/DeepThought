package net.dankito.deepthought.javafx.service.network

import net.dankito.utils.services.network.NetworkConnectivityManagerBase
import net.dankito.utils.services.network.NetworkHelper
import java.util.*
import kotlin.concurrent.schedule


class JavaFXNetworkConnectivityManager(networkHelper: NetworkHelper) : NetworkConnectivityManagerBase(networkHelper) {

    companion object {
        private const val CheckForNetworkInterfaceChangesPeriodMillis = 60 * 1000L
    }


    init {
        Timer().schedule(CheckForNetworkInterfaceChangesPeriodMillis, CheckForNetworkInterfaceChangesPeriodMillis) { networkInterfacesChanged() }
    }

}