package net.dankito.service.synchronization

import com.couchbase.lite.Database
import com.couchbase.lite.listener.Credentials
import com.couchbase.lite.listener.LiteListener
import com.couchbase.lite.replicator.Replication
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.service.synchronization.changeshandler.SynchronizedChangesHandler
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap


class CouchbaseLiteSyncManager(private val entityManager: CouchbaseLiteEntityManagerBase, private val synchronizedChangesHandler: SynchronizedChangesHandler,
                               private val networkSettings: INetworkSettings, private val alsoUsePullReplication: Boolean = true) : ISyncManager {

    companion object {
        val PortNotSet = -1

        private val SynchronizationDefaultPort = 27387

        private val EntitiesFilterName = "Entities_Filter"

        private val log = LoggerFactory.getLogger(CouchbaseLiteSyncManager::class.java)
    }


    private val database = entityManager.database
    private val manager = entityManager.manager

    private var basicDataSyncListener: LiteListener? = null
    private var basicDataSyncPort: Int = PortNotSet
    private var basicDataSyncListenerThread: Thread? = null

    private var couchbaseLiteListener: LiteListener? = null
    private var synchronizationPort: Int = PortNotSet
    private var listenerThread: Thread? = null

    private var pushReplications: MutableMap<DiscoveredDevice, Replication> = ConcurrentHashMap()
    private var pullReplications: MutableMap<DiscoveredDevice, Replication> = ConcurrentHashMap()


    init {
        setReplicationFilter(database)
    }

    private fun setReplicationFilter(database: Database) {
        val entitiesToFilter = ArrayList<String>()
        entitiesToFilter.add(DeepThought::class.java.name)

        database.setFilter(EntitiesFilterName) { revision, params ->
            revision.getProperty(Dao.TYPE_COLUMN_NAME)?.let { entityType ->
                return@setFilter entitiesToFilter.contains(entityType) == false
            }
            true
        }
    }


    override fun stop() {
        stopBasicDataSyncListener()

        closeSynchronizationPort()

        stopSynchronizingWithAllDevices()
    }

    override fun startAsync(desiredSynchronizationPort: Int, desiredBasicDataSynchronizationPort: Int, alsoUsePullReplication: Boolean, initializedCallback: (Int) -> Unit) {
        startBasicDataSyncListener(desiredBasicDataSynchronizationPort, null, initializedCallback)
    }

    private fun startBasicDataSyncListener(desiredPort: Int, allowedCredentials: Credentials? = null, initializedCallback: (Int) -> Unit): Boolean {
        log.info("Starting basic data Couchbase Lite sync listener ...")

        basicDataSyncListener = LiteListener(manager, desiredPort, allowedCredentials)
        basicDataSyncPort = basicDataSyncListener?.listenPort ?: PortNotSet


        networkSettings.basicDataSynchronizationPort = basicDataSyncPort

        basicDataSyncListenerThread = Thread(basicDataSyncListener)
        basicDataSyncListenerThread?.start()

        log.info("Started basic data sync listener on port $basicDataSyncPort")
        initializedCallback(basicDataSyncPort)

        return basicDataSyncPort > 0 && basicDataSyncPort < 65536
    }

    private fun stopBasicDataSyncListener() {
        basicDataSyncListener?.stop()

        basicDataSyncListenerThread?.let { basicDataSyncListenerThread ->
            try { basicDataSyncListenerThread.interrupt() } catch(ignored: Exception) { }
            this.basicDataSyncListenerThread = null
        }

        basicDataSyncPort = PortNotSet
        networkSettings.basicDataSynchronizationPort = PortNotSet
    }

    override fun syncBasicDataWithDevice(deviceId: String, remoteDeviceAddress: String, basicDataSyncPort: Int, syncDone: (Device) -> Unit) {
        log.info("Starting basic data synchronization with device $deviceId")
        var didSynchronizationStop = false
        var receivedRemoteDevice: Device? = entityManager.getEntityById(Device::class.java, deviceId) // as remote may already push his data to our side but we just haven't pushed our data to remote
        var dataBaseChangeListener: Database.ChangeListener? = null

        val push = createPushForBasicDataSync(remoteDeviceAddress, basicDataSyncPort)

        val pushChangeListener = object : Replication.ChangeListener {
            override fun changed(event: Replication.ChangeEvent) {
                didSynchronizationStop = push.status == Replication.ReplicationStatus.REPLICATION_STOPPED
                checkIfBasicDataSynchronizationIsDone(didSynchronizationStop, receivedRemoteDevice, push, this, dataBaseChangeListener!!, syncDone)
            }

        }

        dataBaseChangeListener = object : Database.ChangeListener {
            override fun changed(event: Database.ChangeEvent) {
                if(event.isExternal) {
                    event.changes.filter { deviceId == it.documentId }.forEach {
                        receivedRemoteDevice = entityManager.getEntityById(Device::class.java, it.documentId)
                        checkIfBasicDataSynchronizationIsDone(didSynchronizationStop, receivedRemoteDevice, push, pushChangeListener, this, syncDone)
                    }
                }
            }
        }

        database.addChangeListener(dataBaseChangeListener)
        push.addChangeListener(pushChangeListener)

        push.start()
    }

    private fun createPushForBasicDataSync(remoteDeviceAddress: String, basicDataSyncPort: Int): Replication {
        val push = database.createPushReplication(createSyncUrl(remoteDeviceAddress, basicDataSyncPort))

        // sync all known devices so that remote also knows our synchronized and ignored devices
        val deviceIdsToSync = ArrayList<String>(networkSettings.localUser.synchronizedDevices.map { it.id })
        deviceIdsToSync.addAll(networkSettings.localUser.ignoredDevices.map { it.id!! })
        deviceIdsToSync.add(networkSettings.localHostDevice.id!!) // push localDevice.id as last as this is the signal that basic data synchronization is done
        push.docIds = deviceIdsToSync

        return push
    }

    private fun checkIfBasicDataSynchronizationIsDone(didSynchronizationStop: Boolean, receivedRemoteDevice: Device?, push: Replication,
                               pushChangeListener: Replication.ChangeListener, databaseChangeListener: Database.ChangeListener, syncDone: (Device) -> Unit) {
        if (didSynchronizationStop && receivedRemoteDevice != null) {
            push.stop()
            push.removeChangeListener(pushChangeListener)

            database.removeChangeListener(databaseChangeListener)

            log.info("Basic synchronization with device $receivedRemoteDevice done")
            syncDone(receivedRemoteDevice)
        }
    }

    @Throws(Exception::class)
    override fun openSynchronizationPort(): Int? {
        if(couchbaseLiteListener != null) { // listener already started
            return synchronizationPort
        }

        log.info("Starting Couchbase Lite Listener")

        couchbaseLiteListener = LiteListener(manager, SynchronizationDefaultPort, null) // TODO: set allowedCredentials
        synchronizationPort = couchbaseLiteListener?.listenPort ?: PortNotSet

        if(synchronizationPort > 0 && synchronizationPort < 65536) {
            networkSettings.synchronizationPort = synchronizationPort

            listenerThread = Thread(couchbaseLiteListener)
            listenerThread?.start()

            database.addChangeListener(databaseChangeListener)

            log.info("Listening now on synchronization port $synchronizationPort")
            return synchronizationPort
        }

        return null
    }

    override fun closeSynchronizationPort() {
        log.info("Stopping Couchbase Lite Listener")

        couchbaseLiteListener?.stop()
        couchbaseLiteListener = null

        listenerThread?.let { listenerThread ->
            try { listenerThread.interrupt() } catch (ignored: Exception) { }
            this.listenerThread = null
        }

        synchronizationPort = PortNotSet
        networkSettings.synchronizationPort = PortNotSet
    }


    @Throws(Exception::class)
    override fun startSynchronizationWithDevice(device: DiscoveredDevice) {
        if(pullReplications.containsKey(device)) { // synchronization already started with this device
            return
        }

        openSynchronizationPort() // as at this stage it may not be opened yet but is needed for synchronization

        log.info("Starting Replication with Device " + device)

        val syncUrl: URL
        try {
            syncUrl = createSyncUrl(device.address, device.synchronizationPort)
        } catch (e: MalformedURLException) {
            throw Exception(e)
        }

        val pushReplication = database.createPushReplication(syncUrl)
        pushReplication.filter = EntitiesFilterName
        pushReplication.isContinuous = true

        pushReplications.put(device, pushReplication)

        pushReplication.start()

        if (alsoUsePullReplication) {
            val pullReplication = database.createPullReplication(syncUrl)
            pullReplication.filter = EntitiesFilterName
            pullReplication.isContinuous = true

            pullReplications.put(device, pullReplication)

            pullReplication.start()
        }
    }

    private fun stopSynchronizingWithAllDevices() {
        for(device in pushReplications.keys) {
            stopSynchronizationWithDevice(device)
        }
    }

    override fun stopSynchronizationWithDevice(device: DiscoveredDevice) {
        synchronized(this) {
            log.info("Stopping Replication with Device " + device)

            pullReplications.remove(device)?.stop()

            pushReplications.remove(device)?.stop()

            if(pushReplications.isEmpty()) { // no devices connected anymore
                closeSynchronizationPort()
            }
        }
    }


    private fun createSyncUrl(address: String, syncPort: Int): URL {
        return URL("http://" + address + ":" + syncPort + "/" + database.name)
    }


    private val databaseChangeListener = Database.ChangeListener { event ->
        synchronizedChangesHandler.handleChange(event)
    }

}
