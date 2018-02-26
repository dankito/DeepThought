package net.dankito.util.network

import java.net.Inet4Address
import java.net.InetAddress


data class NetworkInterfaceState(val name: String, var isUp: Boolean, val ipV4Addresses: MutableList<InetAddress>, var broadcastAddress: Inet4Address?)