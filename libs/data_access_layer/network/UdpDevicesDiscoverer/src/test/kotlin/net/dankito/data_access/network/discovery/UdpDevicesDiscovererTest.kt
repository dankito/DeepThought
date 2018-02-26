package net.dankito.data_access.network.discovery


import net.dankito.util.IThreadPool
import net.dankito.util.ThreadPool
import net.dankito.util.network.NetworkConnectivityManagerBase
import net.dankito.util.network.NetworkHelper
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class UdpDevicesDiscovererTest {

    companion object {

        protected val DISCOVERY_PORT = 32788

        protected val CHECK_FOR_DEVICES_INTERVAL = 100

        protected val DISCOVERY_PREFIX = "UdpDevicesDiscovererTest"

        protected val FIRST_DISCOVERER_ID = "Gandhi"

        protected val SECOND_DISCOVERER_ID = "Mandela"
    }


    protected lateinit var firstDiscoverer: UdpDevicesDiscoverer

    protected lateinit var secondDiscoverer: UdpDevicesDiscoverer

    protected lateinit var threadPool: IThreadPool

    protected var startedDiscoverers: MutableList<IDevicesDiscoverer> = CopyOnWriteArrayList<IDevicesDiscoverer>()


    @Before
    fun setUp() {
        threadPool = ThreadPool()

        firstDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, threadPool)
        secondDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, threadPool)
    }

    @After
    fun tearDown() {
        closeStartedDiscoverers()
    }

    protected fun closeStartedDiscoverers() {
        for (discoverer in startedDiscoverers) {
            discoverer.stop()
        }

        startedDiscoverers.clear()
    }


    @Test
    fun startTwoInstances_BothGetDiscovered() {
        val countDownLatch = CountDownLatch(2)

        val foundDevicesForFirstDevice = CopyOnWriteArrayList<String>()
        startFirstDiscoverer(object : DevicesDiscovererListener {
            override fun deviceFound(deviceInfo: String, address: String) {
                foundDevicesForFirstDevice.add(deviceInfo)
                countDownLatch.countDown()
            }

            override fun deviceDisconnected(deviceInfo: String) {
                foundDevicesForFirstDevice.remove(deviceInfo)
                countDownLatch.countDown()
            }
        })

        val foundDevicesForSecondDevice = CopyOnWriteArrayList<String>()
        startSecondDiscoverer(object : DevicesDiscovererListener {
            override fun deviceFound(deviceInfo: String, address: String) {
                foundDevicesForSecondDevice.add(deviceInfo)
                countDownLatch.countDown()
            }

            override fun deviceDisconnected(deviceInfo: String) {
                foundDevicesForSecondDevice.add(deviceInfo)
                countDownLatch.countDown()
            }
        })

        try {
            countDownLatch.await(3, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }

        Assert.assertEquals(1, foundDevicesForFirstDevice.size)
        Assert.assertEquals(SECOND_DISCOVERER_ID, foundDevicesForFirstDevice[0])

        Assert.assertEquals(1, foundDevicesForSecondDevice.size)
        Assert.assertEquals(FIRST_DISCOVERER_ID, foundDevicesForSecondDevice[0])
    }

    @Test
    fun startElevenInstances_AllGetDiscovered() {
        val discoveredDevices = ConcurrentHashMap<String, MutableList<String>>()
        val createdDiscoverers = CopyOnWriteArrayList<IDevicesDiscoverer>()

        for (i in 0..10) {
            val discoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, threadPool)
            createdDiscoverers.add(discoverer)

            val deviceId = "" + (i + 1)
            discoveredDevices.put(deviceId, CopyOnWriteArrayList<String>())

            startDiscoverer(discoverer, deviceId, object : DevicesDiscovererListener {
                override fun deviceFound(deviceInfo: String, address: String) {
                    val discoveredDevicesForDevice = discoveredDevices[deviceId]
                    discoveredDevicesForDevice?.add(deviceInfo)
                }

                override fun deviceDisconnected(deviceInfo: String) {
                    val discoveredDevicesForDevice = discoveredDevices[deviceId]
                    discoveredDevicesForDevice?.remove(deviceInfo)
                }
            })
        }

        try {
            Thread.sleep(3000)
        } catch (ignored: Exception) {
        }

        for (deviceId in discoveredDevices.keys) {
            val discoveredDevicesForDevice = discoveredDevices[deviceId]
            Assert.assertEquals(10, discoveredDevicesForDevice?.size)
            Assert.assertFalse(discoveredDevicesForDevice?.contains(deviceId) ?: true)
        }
    }


    @Test
    fun startTwoInstances_DisconnectOne() {
        val countDownLatch = CountDownLatch(3)

        val foundDevicesForFirstDevice = CopyOnWriteArrayList<String>()
        startFirstDiscoverer(object : DevicesDiscovererListener {
            override fun deviceFound(deviceInfo: String, address: String) {
                foundDevicesForFirstDevice.add(deviceInfo)
                countDownLatch.countDown()
            }

            override fun deviceDisconnected(deviceInfo: String) {
                foundDevicesForFirstDevice.remove(deviceInfo)
                countDownLatch.countDown()
            }
        })

        val foundDevicesForSecondDevice = CopyOnWriteArrayList<String>()
        startSecondDiscoverer(object : DevicesDiscovererListener {
            override fun deviceFound(deviceInfo: String, address: String) {
                foundDevicesForSecondDevice.add(deviceInfo)
                countDownLatch.countDown()
            }

            override fun deviceDisconnected(deviceInfo: String) {
                foundDevicesForSecondDevice.remove(deviceInfo)
                countDownLatch.countDown()
            }
        })

        try {
            Thread.sleep(300)
        } catch (ignored: Exception) {
        }

        firstDiscoverer.stop()

        try {
            countDownLatch.await(3, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }

        Assert.assertEquals(1, foundDevicesForFirstDevice.size)
        Assert.assertEquals(SECOND_DISCOVERER_ID, foundDevicesForFirstDevice[0])

        Assert.assertEquals(0, foundDevicesForSecondDevice.size)
    }

    @Test
    fun startElevenInstances_FiveGetDisconnected() {
        val discoveredDevices = ConcurrentHashMap<IDevicesDiscoverer, MutableList<String>>()
        val createdDiscoverers = CopyOnWriteArrayList<IDevicesDiscoverer>()

        for (i in 0..10) {
            val discoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, threadPool)
            createdDiscoverers.add(discoverer)
            discoveredDevices.put(discoverer, CopyOnWriteArrayList<String>())

            startDiscoverer(discoverer, "" + (i + 1), object : DevicesDiscovererListener {
                override fun deviceFound(deviceInfo: String, address: String) {
                    val discoveredDevicesForDevice = discoveredDevices[discoverer]
                    discoveredDevicesForDevice?.add(deviceInfo)
                }

                override fun deviceDisconnected(deviceInfo: String) {
                    val discoveredDevicesForDevice = discoveredDevices[discoverer]
                    discoveredDevicesForDevice?.remove(deviceInfo)
                }
            })
        }

        try {
            Thread.sleep(500)
        } catch (ignored: Exception) {
        }

        for (i in 0..4) {
            val discoverer = createdDiscoverers[i]
            discoveredDevices.remove(discoverer)

            discoverer.stop()
        }

        try {
            Thread.sleep(5000)
        } catch (ignored: Exception) {
        }

        for (discoverer in discoveredDevices.keys) {
            val discoveredDevicesForDevice = discoveredDevices[discoverer]
            Assert.assertEquals(5, discoveredDevicesForDevice?.size)
        }
    }


    protected fun startFirstDiscoverer(listener: DevicesDiscovererListener) {
        startDiscoverer(firstDiscoverer, FIRST_DISCOVERER_ID, listener)
    }

    protected fun startSecondDiscoverer(listener: DevicesDiscovererListener) {
        startDiscoverer(secondDiscoverer, SECOND_DISCOVERER_ID, listener)
    }

    protected fun startDiscoverer(discoverer: IDevicesDiscoverer, deviceId: String, listener: DevicesDiscovererListener) {
        startedDiscoverers.add(discoverer)

        val config = DevicesDiscovererConfig(deviceId, DISCOVERY_PORT, CHECK_FOR_DEVICES_INTERVAL, DISCOVERY_PREFIX, listener)

        discoverer.startAsync(config)
    }

}
