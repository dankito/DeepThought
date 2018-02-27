package net.dankito.synchronization.device.discovery.udp

import net.dankito.synchronization.device.discovery.DevicesDiscovererListener
import java.net.DatagramPacket


class ReceivedUdpDevicesDiscovererPacket(val receivedData: ByteArray, val packet: DatagramPacket, val senderAddress: String, val localDeviceInfo: String,
                                         val discoveryMessagePrefix: String, val listener: DevicesDiscovererListener)
