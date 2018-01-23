package net.dankito.deepthought.files.synchronization

import net.dankito.data_access.network.communication.SocketHandler
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.service.data.LocalFileInfoService
import net.dankito.service.synchronization.IConnectedDevicesService
import net.dankito.service.synchronization.KnownSynchronizedDevicesListener
import net.dankito.utils.AsyncProducerConsumerQueue
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.services.hashing.HashAlgorithm
import net.dankito.utils.services.hashing.HashService
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap


class FileSyncService(private val connectedDevicesService: IConnectedDevicesService, fileServer: FileServer, private val socketHandler: SocketHandler,
                      private val localFileInfoService: LocalFileInfoService, private val serializer: ISerializer, private val platformConfiguration: IPlatformConfiguration,
                      private val hashService: HashService) {

    companion object {
        private val log = LoggerFactory.getLogger(FileSyncService::class.java)
    }


    private val currentFileSynchronizations = ConcurrentHashMap<FileLink, FileSyncState>()


    private val queue = AsyncProducerConsumerQueue<FileLink>(getCountConnectionsToUse(), autoStart = false) {
        tryToSynchronizeFile(it)
    }


    init {
        connectedDevicesService.addKnownSynchronizedDevicesListener(object : KnownSynchronizedDevicesListener {
            override fun knownSynchronizedDeviceConnected(connectedDevice: DiscoveredDevice) {
                if(queue.isRunning == false) {
                    queue.start()
                }
            }

            override fun knownSynchronizedDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
                if(connectedDevicesService.knownSynchronizedDiscoveredDevices.size == 0) {
                    queue.stop()
                }
            }

        })
    }


    fun addFileToSynchronize(file: FileLink) {
        queue.add(file)
    }

    private fun tryToSynchronizeFile(file: FileLink) {
        log.info("Trying to synchronize file $file (${file.localFileInfo})")

        if(currentFileSynchronizations.containsKey(file) || file.localFileInfo?.syncStatus == FileSyncStatus.UpToDate) { // already synchronized in the meantime
            return // current ongoing synchronization for this file, don't sync twice
        }

        val connectedDevices = ArrayList(connectedDevicesService.knownSynchronizedDiscoveredDevices)

        if(connectedDevices.size > 0) {
            val state = FileSyncState(file)
            currentFileSynchronizations.put(file, state)

            if(tryToSynchronizeFile(file, state, connectedDevices) == false) {
                addFileToSynchronize(file) // TODO: what to do with state, e.g. the information of devices not having this file?
            }

            currentFileSynchronizations.remove(file)
        }
        else {
            // TODO: sleep a while before re-adding file
            addFileToSynchronize(file)
        }
    }

    private fun tryToSynchronizeFile(file: FileLink, status: FileSyncState, connectedDevices: ArrayList<DiscoveredDevice>): Boolean {
        var connectedDevice: DiscoveredDevice? = connectedDevices.removeAt(0)

        while(connectedDevice != null) {
            val result = tryToSynchronizeFileWithDevice(file, connectedDevice)
            log.info("Result of trying to synchronize file from ${connectedDevice.device}: $result")

            if(result == PermitSynchronizeFileResult.SynchronizationPermitted) {
                return true
            }
            else if(result == PermitSynchronizeFileResult.NoSlotsAvailableTryLater) { // TODO: do we really need this information?
                status.devicesHavingFileButNoFreeSlots.add(connectedDevice)
            }
            else if(result == PermitSynchronizeFileResult.DoNotHaveFile) {
                status.devicesNotHavingFile.add(connectedDevice) // TODO: what about Prohibited?
            }

            if(connectedDevices.size > 0) {
                connectedDevice = connectedDevices.removeAt(0)
            }
            else {
                connectedDevice = null
            }
        }

        return false
    }

    private fun tryToSynchronizeFileWithDevice(file: FileLink, connectedDevice: DiscoveredDevice): PermitSynchronizeFileResult {
        if(connectedDevice.fileSynchronizationPort > 0) {
            try {
                val fileId = file.id

                if(fileId != null) {
                    val clientSocket = Socket(connectedDevice.address, connectedDevice.fileSynchronizationPort)

                    try {
                        return sendRequestAndTryToSynchronizeFileWithDevice(clientSocket, fileId, file)
                    } catch(e: Exception) {
                        log.error("Could not send request and synchronize file with device ${connectedDevice.device} on ${connectedDevice.address}:${connectedDevice.fileSynchronizationPort}", e)
                    } finally {
                        socketHandler.closeSocket(clientSocket)
                    }
                }
            } catch(e: Exception) {
                log.error("Could not open connection to device ${connectedDevice.device} on ${connectedDevice.address}:${connectedDevice.fileSynchronizationPort}", e)
            }
        }

        return PermitSynchronizeFileResult.ErrorOccurred
    }

    private fun sendRequestAndTryToSynchronizeFileWithDevice(clientSocket: Socket, fileId: String, file: FileLink): PermitSynchronizeFileResult {
        sendMessage(clientSocket, PermitSynchronizeFileRequest(fileId))

        val result = socketHandler.receiveMessage(clientSocket)
        if(result.isSuccessful) {
            result.receivedMessage?.let {
                val response = serializer.deserializeObject(it, PermitSynchronizeFileResponse::class.java)

                if(response.result == PermitSynchronizeFileResult.SynchronizationPermitted) {
                    if(receiveFile(clientSocket, file)) {
                        return PermitSynchronizeFileResult.SynchronizationPermitted
                    }
                }
            }
        }
        else {
            // TODO:
        }

        return PermitSynchronizeFileResult.ErrorOccurred
    }

    private fun receiveFile(clientSocket: Socket, file: FileLink): Boolean {
        file.localFileInfo?.let { localFileInfo -> // should actually never come to this with localFileInfo == null
            val destinationFile = if(localFileInfo.path != null) File(localFileInfo.path) else File(getDefaultSavePathForFile(file), file.name)
            destinationFile.parentFile.mkdirs()

            val countReceivedBytes = saveStreamToFile(destinationFile, clientSocket)
            log.info("Received $countReceivedBytes and should have received ${file.fileSize} bytes for file ${file.name}")

            if(countReceivedBytes == file.fileSize) {
                fileSuccessfullySynchronized(file, localFileInfo, destinationFile)
                return true
            }
        }

        return false
    }

    private fun saveStreamToFile(destinationFile: File, clientSocket: Socket): Long {
        val outputStream = BufferedOutputStream(FileOutputStream(destinationFile))
        val inputStream = DataInputStream(clientSocket.getInputStream())

        val buffer = ByteArray(1 * 1024)

        var receivedChunkSize: Int
        var receivedMessageSize = 0L

        do {
            receivedChunkSize = inputStream.read(buffer)

            if(receivedChunkSize > 0) {
                receivedMessageSize += receivedChunkSize

                outputStream.write(buffer, 0, receivedChunkSize)
            }
        } while(receivedChunkSize >= 0)

        outputStream.flush()
        inputStream.close() // inputStream will be closed by socketHandler
        outputStream.close()

        return receivedMessageSize
    }

    private fun fileSuccessfullySynchronized(file: FileLink, localFileInfo: LocalFileInfo, destinationFile: File) {
        localFileInfo.path = destinationFile.absolutePath

        localFileInfo.fileSize = File(destinationFile.absolutePath).length() // we have to create a new File object to get file size
        localFileInfo.hashSHA512 = hashService.getFileHash(HashAlgorithm.SHA512, destinationFile)

        file.fileLastModified?.let { lastModified ->
            destinationFile.setLastModified(lastModified.time)
        }
        localFileInfo.fileLastModified = file.fileLastModified

        localFileInfo.syncStatus = if(localFileInfo.fileSize == file.fileSize) FileSyncStatus.UpToDate else FileSyncStatus.NotSynchronizedYet

        localFileInfoService.update(localFileInfo)
    }

    private fun getDefaultSavePathForFile(file: FileLink): String {
        return platformConfiguration.getDefaultDataFolder().absolutePath // TODO
    }

    private fun sendMessage(clientSocket: Socket, message: Any) {
        val serializedResponse = serializer.serializeObject(message)
        val responseBytes = serializedResponse.toByteArray(FileServer.MESSAGE_CHARSET)

        socketHandler.sendMessage(clientSocket, responseBytes)
    }


    private fun getCountConnectionsToUse(): Int {
        val countProcessors = Runtime.getRuntime().availableProcessors()

        return Math.min(4, 2 * countProcessors) // on older Androids don't use too many simultaneous connections, and a max of 4 connections should be really sufficient
    }

}