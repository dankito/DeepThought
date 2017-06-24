package net.dankito.data_access.network.discovery

import java.net.DatagramPacket


class ReceivedUdpDevicesDiscovererPacket(val receivedData: ByteArray, val packet: DatagramPacket, val senderAddress: String, val localDeviceInfo: String,
                                         val discoveryMessagePrefix: String, val listener: DevicesDiscovererListener)
