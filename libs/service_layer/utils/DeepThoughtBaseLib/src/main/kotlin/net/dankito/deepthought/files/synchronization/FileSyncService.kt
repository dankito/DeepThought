package net.dankito.deepthought.files.synchronization

import net.dankito.data_access.network.communication.SocketHandler
import net.dankito.deepthought.files.synchronization.model.SynchronizeFileResult
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
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule


class FileSyncService(private val connectedDevicesService: IConnectedDevicesService, fileServer: FileServer, private val socketHandler: SocketHandler,
                      private val localFileInfoService: LocalFileInfoService, private val serializer: ISerializer, private val platformConfiguration: IPlatformConfiguration,
                      private val hashService: HashService) {

    companion object {
        private const val MaxDelayBeforeRetryingToSynchronizeFile = 60 * 60 * 1000L // 1 hour

        private val log = LoggerFactory.getLogger(FileSyncService::class.java)
    }


    private val currentFileSynchronizations = ConcurrentHashMap<FileLink, FileSyncState>()

    private val syncStatesOfNotSuccessfullySynchronizedFiles = ConcurrentHashMap<FileLink, FileSyncState>()

    private val timerForNotSuccessfullySynchronizedFiles = Timer()


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

    private fun addNotSuccessfullySynchronizedFile(file: FileLink, state: FileSyncState) {
        syncStatesOfNotSuccessfullySynchronizedFiles.put(file, state)

        val delay = calculateDelay(state) // countTries times 1 minute

        timerForNotSuccessfullySynchronizedFiles.schedule(delay) {
            queue.add(file)
        }
    }

    private fun calculateDelay(state: FileSyncState): Long {
        if(state.devicesHavingFileButNoFreeSlots.isNotEmpty()) { // wait only a short time and re-try again if other file(s) have finished
            return 30 * 1000L
        }
        else if(state.devicesNotHavingFile.isNotEmpty()) { // don't get other devices on their nerves, wait longer depending on count of retries
            var delay = state.countTries * 60 * 1000L // countTries times 1 minute
            if(delay > MaxDelayBeforeRetryingToSynchronizeFile) {
                delay = MaxDelayBeforeRetryingToSynchronizeFile
            }

            return delay
        }
        else { // no connected devices. Queue will stop soon, re-add with a delay we can be sure queue has stopped
            return 60 * 1000L
        }
    }


    private fun tryToSynchronizeFile(file: FileLink) {
        log.info("Trying to synchronize file $file (${file.localFileInfo})")

        val state = syncStatesOfNotSuccessfullySynchronizedFiles[file] ?: FileSyncState(file)
        val connectedDevices = ArrayList(connectedDevicesService.knownSynchronizedDiscoveredDevices)

        synchronized(currentFileSynchronizations) {
            if(currentFileSynchronizations.containsKey(file) || file.localFileInfo?.syncStatus == FileSyncStatus.UpToDate) { // already synchronized in the meantime
                return // current ongoing synchronization for this file, don't sync twice
            }

            if(connectedDevices.isEmpty()) {
                addNotSuccessfullySynchronizedFile(file, state)
                return
            }

            currentFileSynchronizations.put(file, state)
        }


        if(tryToSynchronizeFile(file, state, connectedDevices)) {
            syncStatesOfNotSuccessfullySynchronizedFiles.remove(file)
        }
        else {
            addNotSuccessfullySynchronizedFile(file, state)
        }

        synchronized(currentFileSynchronizations) {
            currentFileSynchronizations.remove(file)
        }
    }

    private fun tryToSynchronizeFile(file: FileLink, status: FileSyncState, connectedDevices: ArrayList<DiscoveredDevice>): Boolean {
        status.countTries = status.countTries + 1
        status.devicesHavingFileButNoFreeSlots.clear()
        status.devicesNotHavingFile.clear()

        var connectedDevice: DiscoveredDevice? = connectedDevices.removeAt(0)

        while(connectedDevice != null) {
            val result = tryToSynchronizeFileWithDevice(file, connectedDevice)
            log.info("Result of trying to synchronize file from ${connectedDevice.device}: $result")

            if(result == SynchronizeFileResult.Success) {
                return true
            }

            setSynchronizationErrorOnStatus(result, status, connectedDevice)

            if(connectedDevices.size > 0) {
                connectedDevice = connectedDevices.removeAt(0)
            }
            else {
                connectedDevice = null
            }
        }

        return false
    }

    private fun setSynchronizationErrorOnStatus(result: SynchronizeFileResult, status: FileSyncState, connectedDevice: DiscoveredDevice) {
        if(result == SynchronizeFileResult.NoSlotsAvailableTryLater) {
            status.devicesHavingFileButNoFreeSlots.add(connectedDevice)
        }
        else if(result == SynchronizeFileResult.RemoteDoesNotHaveFile) {
            status.devicesNotHavingFile.add(connectedDevice)
        }
        // TODO: what about Prohibited?
    }

    private fun tryToSynchronizeFileWithDevice(file: FileLink, connectedDevice: DiscoveredDevice): SynchronizeFileResult {
        if(connectedDevice.fileSynchronizationPort > 0) {
            try {
                val fileId = file.id

                if(fileId != null) {
                    val clientSocket = Socket(connectedDevice.address, connectedDevice.fileSynchronizationPort)

                    try {
                        return sendRequestAndTryToSynchronizeFileWithDevice(clientSocket, fileId, file)
                    } catch(e: Exception) {
                        log.error("Could not send request and synchronize file with device ${connectedDevice.device} on ${connectedDevice.address}:${connectedDevice.fileSynchronizationPort}", e)
                        return SynchronizeFileResult.ErrorOccurred
                    } finally {
                        socketHandler.closeSocket(clientSocket)
                    }
                }
            } catch(e: Exception) {
                log.error("Could not open connection to device ${connectedDevice.device} on ${connectedDevice.address}:${connectedDevice.fileSynchronizationPort}", e)
                return SynchronizeFileResult.ErrorOccurred
            }
        }

        return SynchronizeFileResult.RemoteFileSynchronizationPortNotSet
    }

    private fun sendRequestAndTryToSynchronizeFileWithDevice(clientSocket: Socket, fileId: String, file: FileLink): SynchronizeFileResult {
        sendMessage(clientSocket, PermitSynchronizeFileRequest(fileId))

        val result = socketHandler.receiveMessage(clientSocket)
        if(result.isSuccessful) {
            result.receivedMessage?.let {
                val response = serializer.deserializeObject(it, PermitSynchronizeFileResponse::class.java)

                if(response.result == PermitSynchronizeFileResult.SynchronizationPermitted) {
                    return receiveFile(clientSocket, file)
                }
                else {
                    return mapPermitSynchronizeFileResultToSynchronizeFileResult(response.result)
                }
            }
        }
        else {
            // TODO:
        }

        return SynchronizeFileResult.ErrorOccurred
    }

    private fun receiveFile(clientSocket: Socket, file: FileLink): SynchronizeFileResult {
        file.localFileInfo?.let { localFileInfo -> // should actually never come to this with localFileInfo == null
            val destinationFile = if(localFileInfo.path != null) File(localFileInfo.path) else File(getDefaultSavePathForFile(file), file.name)
            destinationFile.parentFile.mkdirs()

            val countReceivedBytes = saveStreamToFile(destinationFile, clientSocket)
            log.info("Received $countReceivedBytes and should have received ${file.fileSize} bytes for file ${file.name}")

            if(countReceivedBytes == file.fileSize) {
                fileSuccessfullySynchronized(file, localFileInfo, destinationFile)
                return SynchronizeFileResult.Success
            }
            else {
                return SynchronizeFileResult.DidNotReceiveAllBytes
            }
        }

        return SynchronizeFileResult.LocalFileInfoNotSet // can this ever happen?
    }

    private fun saveStreamToFile(destinationFile: File, clientSocket: Socket): Long {
        val outputStream = BufferedOutputStream(FileOutputStream(destinationFile))
        val inputStream = DataInputStream(clientSocket.getInputStream())

        val buffer = ByteArray(8 * 1024)

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

        localFileInfoService.update(localFileInfo, true)
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

    private fun mapPermitSynchronizeFileResultToSynchronizeFileResult(result: PermitSynchronizeFileResult): SynchronizeFileResult {
        when(result) {
            PermitSynchronizeFileResult.DoNotHaveFile -> return SynchronizeFileResult.RemoteDoesNotHaveFile
            PermitSynchronizeFileResult.NoSlotsAvailableTryLater -> return SynchronizeFileResult.NoSlotsAvailableTryLater
            PermitSynchronizeFileResult.Prohibited -> return SynchronizeFileResult.Prohibited
            else -> return SynchronizeFileResult.ErrorOccurred
        }
    }

}