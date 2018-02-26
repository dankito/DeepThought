package net.dankito.util.network

import org.slf4j.LoggerFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.collections.ArrayList


class NetworkHelper {

    /*
         With Android:

         WifiManager wifi = mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
          quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);

     */

    // in IPv6 there's no such thing as broadcast
    fun getBroadcastAddress(networkInterface: NetworkInterface): Inet4Address? {
        for(address in networkInterface.inetAddresses) {
            if(address is Inet4Address) {
                try {
                    return getBroadcastAddress(address)
                } catch(e: Exception) { log.error("Could not determine Broadcast Address of " + address.hostAddress, e)}
            }
        }

        return null
    }

    fun getBroadcastAddress(address: Inet4Address): Inet4Address? {
        val broadcastAddress = address.address
        broadcastAddress[broadcastAddress.size - 1] = 255.toByte()

        return Inet4Address.getByAddress(broadcastAddress) as? Inet4Address
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * *
     * @return  mac address or empty string
     */
    fun getMACAddress(interfaceName: String?): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (interfaceName != null) {
                    if (!intf.name.equals(interfaceName, ignoreCase = true)) continue
                }
                val mac = intf.hardwareAddress ?: return ""
                val buf = StringBuilder()
                for (idx in mac.indices)
                    buf.append(String.format("%02X:", mac[idx]))
                if (buf.length > 0) buf.deleteCharAt(buf.length - 1)
                return buf.toString()
            }
        } catch (ex: Exception) {
        }
        // for now eat exceptions
        return ""
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4  true=return ipv4, false=return ipv6
     * *
     * @return  address or empty string
     */
    fun getIPAddress(useIPv4: Boolean): InetAddress? {
        val addresses = getIPAddresses(useIPv4)
        if (addresses.size > 0) {
            return addresses[0]
        }

        return null
    }

    fun getIPAddresses(onlyIPv4: Boolean): List<InetAddress> {
        val addresses = ArrayList<InetAddress>()

        try {
            for(networkInterface in getConnectedRealNetworkInterfaces()) {
                addresses.addAll(getIPAddresses(networkInterface, onlyIPv4))
            }
        } catch (ignored: Exception) { } // for now eat exceptions

        return addresses
    }

    fun getIPAddresses(networkInterface: NetworkInterface, onlyIPv4: Boolean): List<InetAddress> {
        val addresses = ArrayList<InetAddress>()
        val interfaceAddresses = Collections.list(networkInterface.inetAddresses)

        for(address in interfaceAddresses) {
            if(address.isLoopbackAddress == false) {
                if(onlyIPv4 == false || address is Inet4Address) {
                    addresses.add(address)
                }
            }
        }

        return addresses
    }

    fun getRealNetworkInterfaces(): Collection<NetworkInterface> {
        val allInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())

        return allInterfaces.filter { shouldInterfaceBeIgnored(it) == false }
    }

    fun getConnectedRealNetworkInterfaces(): Collection<NetworkInterface> {
        return getRealNetworkInterfaces().filter { it.isUp }
    }

    @Throws(SocketException::class)
    private fun shouldInterfaceBeIgnored(networkInterface: NetworkInterface): Boolean {
        return networkInterface.isLoopback || isCellularOrUsbInterface(networkInterface) ||
                isDockerInterface(networkInterface) || isDummyInterface(networkInterface)
    }

    private fun isCellularOrUsbInterface(networkInterface: NetworkInterface): Boolean {
        return networkInterface.name.startsWith("rmnet") // see for example https://stackoverflow.com/a/33748594
    }

    private fun isDockerInterface(networkInterface: NetworkInterface): Boolean {
        return networkInterface.name.startsWith("docker")
    }

    private fun isDummyInterface(networkInterface: NetworkInterface): Boolean {
        return networkInterface.name.startsWith("dummy")
    }


    fun isSocketCloseException(exception: Exception): Boolean {
        return exception is SocketException && "Socket closed" == exception.message
    }

    companion object {

        private val log = LoggerFactory.getLogger(NetworkHelper::class.java)
    }

}
