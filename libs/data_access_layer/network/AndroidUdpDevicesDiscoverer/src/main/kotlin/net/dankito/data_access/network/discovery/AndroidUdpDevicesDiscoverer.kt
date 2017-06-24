package net.dankito.data_access.network.discovery

import android.content.Context
import android.net.wifi.WifiManager

import net.dankito.utils.IThreadPool


class AndroidUdpDevicesDiscoverer(private var context: Context, threadPool: IThreadPool) : UdpDevicesDiscoverer(threadPool) {

    companion object {
        private const val MULTICAST_LOCK_NAME = "AndroidUdpDevicesDiscoverer"
    }


    private var multicastLock: WifiManager.MulticastLock? = null


    override fun startAsync(config: DevicesDiscovererConfig) {
        acquireWifiLock()

        super.startAsync(config)
    }

    override fun stop() {
        releaseWifiLock()

        super.stop()
    }


    /**
     * To improve battery life, processing of multicast packets is disabled by default on Android.
     * We can and must reenable this for the service discovery to work.
     * This is done programmatically by acquiring a lock in our activity.
     * (Explanation copied from http://home.heeere.com/tech-androidjmdns.html)
     */
    private fun acquireWifiLock() {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_NAME)

        multicastLock?.let { multicastLock ->
            multicastLock.setReferenceCounted(true)
            multicastLock.acquire()
        }
    }

    private fun releaseWifiLock() {
        multicastLock?.let { multicastLock ->
            if(multicastLock.isHeld) {
                multicastLock.release()
            }
        }

        multicastLock = null
    }

}
