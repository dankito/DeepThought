package net.dankito.utils.services.network

import org.slf4j.LoggerFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


class NetworkHelper {

    // in IPv6 there's no such thing as broadcast
    val broadcastAddresses: List<InetAddress>
        get() {
            val broadcastAddresses = ArrayList<InetAddress>()

            for (address in getIPAddresses(true)) {
                try {
                    val broadcastAddress = address.address
                    broadcastAddress[broadcastAddress.size - 1] = 255.toByte()
                    broadcastAddresses.add(Inet4Address.getByAddress(broadcastAddress))
                } catch (ex: Exception) {
                    log.error("Could not determine Broadcast Address of " + address.hostAddress, ex)
                }

            }

            return broadcastAddresses
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
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                if (shouldInterfaceBeIgnored(networkInterface)) {
                    continue
                }

                val interfaceAddresses = Collections.list(networkInterface.inetAddresses)

                for (address in interfaceAddresses) {
                    if (address.isLoopbackAddress == false) {
                        if (onlyIPv4 == false || address is Inet4Address) {
                            addresses.add(address)
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        // for now eat exceptions

        return addresses
    }

    @Throws(SocketException::class)
    protected fun shouldInterfaceBeIgnored(networkInterface: NetworkInterface): Boolean {
        return networkInterface.isLoopback || networkInterface.isUp == false || isCellularOrUsbInterface(networkInterface) ||
                isDockerInterface(networkInterface) || isDummyInterface(networkInterface)
    }

    protected fun isCellularOrUsbInterface(networkInterface: NetworkInterface): Boolean {
        return networkInterface.name.startsWith("rmnet") // see for example https://stackoverflow.com/a/33748594
    }

    protected fun isDockerInterface(networkInterface: NetworkInterface): Boolean {
        return networkInterface.name.startsWith("docker")
    }

    protected fun isDummyInterface(networkInterface: NetworkInterface): Boolean {
        return networkInterface.name.startsWith("dummy")
    }

    // TODO: try to get rid of this method as it's not reliable (see above)
    fun getIPAddressString(useIPv4: Boolean): String {
        val address = getIPAddress(useIPv4)
        if (address != null) {
            val addressString = address.hostAddress.toUpperCase()

            if (useIPv4)
                return addressString
            else {
                val delim = addressString.indexOf('%') // drop ip6 port suffix
                return if (delim < 0) addressString else addressString.substring(0, delim)
            }
        }

        return ""
    }


    fun isSocketCloseException(exception: Exception): Boolean {
        return exception is SocketException && "Socket closed" == exception.message
    }

    companion object {

        private val log = LoggerFactory.getLogger(NetworkHelper::class.java)
    }

}
