package net.dankito.service.synchronization

import com.couchbase.lite.Database
import com.couchbase.lite.listener.Credentials
import com.couchbase.lite.listener.LiteListener
import com.couchbase.lite.replicator.Replication
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.utils.IThreadPool
import org.slf4j.LoggerFactory
import java.net.URL


class CouchbaseLiteSyncManager(private val entityManager: CouchbaseLiteEntityManagerBase, private val networkSettings: INetworkSettings, threadPool: IThreadPool) : ISyncManager {

    companion object {
        private val log = LoggerFactory.getLogger(CouchbaseLiteSyncManager::class.java)
    }


    private val database = entityManager.database
    private val manager = entityManager.manager

    private lateinit var basicDataSyncListener: LiteListener
    private var basicDataSyncPort: Int = 0
    private var basicDataSyncListenerThread: Thread? = null



    override fun startAsync(desiredSynchronizationPort: Int, desiredBasicDataSynchronizationPort: Int, alsoUsePullReplication: Boolean, initializedCallback: (Int) -> Unit) {
        startBasicDataSyncListener(desiredBasicDataSynchronizationPort, null, initializedCallback)
    }

    private fun startBasicDataSyncListener(desiredPort: Int, allowedCredentials: Credentials? = null, initializedCallback: (Int) -> Unit): Boolean {
        log.info("Starting basic data Couchbase Lite sync listener ...")

        basicDataSyncListener = LiteListener(manager, desiredPort, allowedCredentials)
        basicDataSyncPort = basicDataSyncListener.listenPort


        networkSettings.basicDataSynchronizationPort = basicDataSyncPort

        basicDataSyncListenerThread = Thread(basicDataSyncListener)
        basicDataSyncListenerThread?.start()

        log.info("Started basic data sync listener on port $basicDataSyncPort")
        initializedCallback(basicDataSyncPort)

        return basicDataSyncPort > 0 && basicDataSyncPort < 65536
    }

    override fun syncBasicDataWithDevice(deviceId: String, remoteDeviceAddress: String, basicDataSyncPort: Int, syncDone: (Device) -> Unit) {
        var didSynchronizationStop = false
        var receivedRemoteDevice: Device? = null
        var dataBaseChangeListener: Database.ChangeListener? = null

        val push = database.createPushReplication(createSyncUrl(remoteDeviceAddress, basicDataSyncPort))
        push.docIds = listOf(networkSettings.localHostDevice.id) // only push local device to remote

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


    private fun createSyncUrl(address: String, syncPort: Int): URL {
        return URL("http://" + address + ":" + syncPort + "/" + database.getName());
    }

}
