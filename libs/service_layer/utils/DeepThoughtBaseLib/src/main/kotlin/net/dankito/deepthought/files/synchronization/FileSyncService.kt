package net.dankito.deepthought.files.synchronization

import net.dankito.synchronization.device.service.IConnectedDevicesService
import net.dankito.synchronization.device.service.KnownSynchronizedDevicesListener
import net.dankito.synchronization.files.message.PermitSynchronizeFileRequest
import net.dankito.synchronization.files.message.PermitSynchronizeFileResponse
import net.dankito.synchronization.files.message.PermitSynchronizeFileResult
import net.dankito.synchronization.files.model.FileSyncState
import net.dankito.synchronization.files.model.SynchronizeFileResult
import net.dankito.synchronization.files.persistence.ILocalFileInfoRepository
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.model.LocalFileInfo
import net.dankito.synchronization.model.enums.FileSyncStatus
import net.dankito.synchronization.search.ISearchEngine
import net.dankito.synchronization.search.specific.LocalFileInfoSearch
import net.dankito.synchronization.service.permissions.IPermissionsService
import net.dankito.util.AsyncProducerConsumerQueue
import net.dankito.util.hashing.HashService
import net.dankito.util.network.SocketHandler
import net.dankito.util.serialization.ISerializer
import org.slf4j.LoggerFactory
import java.io.*
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.schedule


open class FileSyncService(protected val connectedDevicesService: IConnectedDevicesService, protected val searchEngine: ISearchEngine<FileLink>,
                           protected val socketHandler: SocketHandler, protected val localFileInfoRepository: ILocalFileInfoRepository, protected val serializer: ISerializer,
                           protected val permissionsService: IPermissionsService, protected val hashService: HashService) {

    companion object {
        protected const val MaxDelayBeforeRetryingToSynchronizeFile = 60 * 60 * 1000L // 1 hour

        private val log = LoggerFactory.getLogger(FileSyncService::class.java)
    }


    protected val currentFileSynchronizations = ConcurrentHashMap<FileLink, FileSyncState>()

    protected val syncStatesOfNotSuccessfullySynchronizedFiles = ConcurrentHashMap<FileLink, FileSyncState>()

    protected val timerForNotSuccessfullySynchronizedFiles = Timer()

    protected val isRequestingPermissionToWriteFiles = AtomicBoolean(false)

    protected val synchronizedFilesWaitingForWriteFilePermissionResult = mutableListOf<FileLink>()


    protected val queue = AsyncProducerConsumerQueue<FileLink>(FileSyncConfig.MaxSimultaneousConnections, autoStart = false) {
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


    open fun addFileToSynchronize(file: FileLink) {
        queue.add(file)
    }

    protected open fun addNotSuccessfullySynchronizedFile(file: FileLink, state: FileSyncState) {
        syncStatesOfNotSuccessfullySynchronizedFiles.put(file, state)

        val delay = calculateDelay(state) // countTries times 1 minute

        timerForNotSuccessfullySynchronizedFiles.schedule(delay) {
            queue.add(file)
        }
    }

    protected open fun calculateDelay(state: FileSyncState): Long {
        if(state.devicesWithGoodChances.isNotEmpty()) { // wait only a short time and re-try again if other file(s) have finished
            return 30 * 1000L
        }
        else if(state.devicesUnlikelyToGetFileFrom.isNotEmpty()) { // don't get other devices on their nerves, wait longer depending on count of retries
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


    protected open fun tryToSynchronizeFile(file: FileLink) {
        if(permissionsService.hasPermissionToWriteFiles()) {
            tryToSynchronizeFileWithPermissionToWriteFile(file)
        }
        else {
            synchronized(isRequestingPermissionToWriteFiles) {
                if(isRequestingPermissionToWriteFiles.get() == false) {
                    isRequestingPermissionToWriteFiles.set(true)

                    permissionsService.requestPermissionToWriteSynchronizedFiles { isGranted ->
                        retrievedRequestWriteFilePermissionResult(isGranted, file)
                    }
                }
                else {
                    synchronizedFilesWaitingForWriteFilePermissionResult.add(file)
                }
            }
        }
    }

    protected open fun retrievedRequestWriteFilePermissionResult(isGranted: Boolean, file: FileLink) {
        if(isGranted) {
            tryToSynchronizeFileWithPermissionToWriteFile(file)

            synchronizedFilesWaitingForWriteFilePermissionResult.forEach {
                tryToSynchronizeFileWithPermissionToWriteFile(it)
            }
        }
        else {
            addNotSuccessfullySynchronizedFile(file, syncStatesOfNotSuccessfullySynchronizedFiles[file] ?: FileSyncState(file))

            synchronizedFilesWaitingForWriteFilePermissionResult.forEach {
                addNotSuccessfullySynchronizedFile(it, syncStatesOfNotSuccessfullySynchronizedFiles[it] ?: FileSyncState(it))
            }
        }

        synchronizedFilesWaitingForWriteFilePermissionResult.clear()
    }

    protected open fun tryToSynchronizeFileWithPermissionToWriteFile(file: FileLink) {
        val localFileInfo = getStoredLocalFileInfo(file)
        log.info("Trying to synchronize file $file ($localFileInfo)")

        val state = syncStatesOfNotSuccessfullySynchronizedFiles[file] ?: FileSyncState(file)
        val connectedDevices = ArrayList(connectedDevicesService.knownSynchronizedDiscoveredDevices)

        synchronized(currentFileSynchronizations) {
            if(currentFileSynchronizations.containsKey(file) || localFileInfo?.syncStatus == FileSyncStatus.UpToDate) { // already synchronized in the meantime
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

    protected open fun tryToSynchronizeFile(file: FileLink, status: FileSyncState, connectedDevices: ArrayList<DiscoveredDevice>): Boolean {
        status.countTries = status.countTries + 1
        status.devicesWithGoodChances.clear()
        status.devicesUnlikelyToGetFileFrom.clear()

        var connectedDevice: DiscoveredDevice? = connectedDevices.removeAt(0)

        while(connectedDevice != null) {
            val result = tryToSynchronizeFileWithDevice(file, connectedDevice)
            log.info("Result of trying to synchronize file $file: $result")

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

    protected open fun setSynchronizationErrorOnStatus(result: SynchronizeFileResult, status: FileSyncState, connectedDevice: DiscoveredDevice) {
        when(result) {
            SynchronizeFileResult.NoSlotsAvailableTryLater,
            SynchronizeFileResult.DidNotReceiveAllBytes
                -> status.devicesWithGoodChances.add(connectedDevice)

            SynchronizeFileResult.RemoteDoesNotHaveFile,
            SynchronizeFileResult.RemoteFileSynchronizationPortNotSet,
            SynchronizeFileResult.LocalFileInfoNotSet,
            SynchronizeFileResult.Prohibited,
            SynchronizeFileResult.ErrorOccurred
                -> status.devicesUnlikelyToGetFileFrom.add(connectedDevice)
        }
    }

    protected open fun tryToSynchronizeFileWithDevice(file: FileLink, connectedDevice: DiscoveredDevice): SynchronizeFileResult {
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

    protected open fun sendRequestAndTryToSynchronizeFileWithDevice(clientSocket: Socket, fileId: String, file: FileLink): SynchronizeFileResult {
        sendMessage(clientSocket, PermitSynchronizeFileRequest(fileId))

        val result = socketHandler.receiveMessage(clientSocket)
        if(result.isSuccessful) {
            result.receivedMessage?.let {
                val response = serializer.deserializeObject(it, PermitSynchronizeFileResponse::class.java)

                if(response.result == PermitSynchronizeFileResult.SynchronizationPermitted) {
                    socketHandler.sendMessage(clientSocket, FileSyncConfig.BeginToStreamFileMessage)

                    return receiveFile(clientSocket, file, response.fileSize ?: file.fileSize)
                }
                else {
                    return mapPermitSynchronizeFileResultToSynchronizeFileResult(response.result)
                }
            }
        }
        else {
            log.error("Could not synchronize file $file", result.error)
        }

        return SynchronizeFileResult.ErrorOccurred
    }

    protected open fun receiveFile(clientSocket: Socket, file: FileLink, fileSize: Long): SynchronizeFileResult {
        getStoredLocalFileInfo(file)?.let { localFileInfo -> // should actually never come to this with localFileInfo == null
            val destinationFile = if(localFileInfo.path != null) File(localFileInfo.path) else getDefaultSavePathForFile(file)
            destinationFile.parentFile.mkdirs()

            val startTime = Date().time

            val countReceivedBytes = saveStreamToFile(destinationFile, clientSocket, fileSize)

            val duration = Date().time - startTime
            log.info("Received in ${duration / 1000}:${String.format("%03d", duration % 1000)} ${countReceivedBytes / fileSize.toFloat() * 100} % " +
                    "($countReceivedBytes / $fileSize) for file ${file.name}")

            if(countReceivedBytes == fileSize) {
                fileSuccessfullySynchronized(file, localFileInfo, destinationFile)
                return SynchronizeFileResult.Success
            }
            else {
                return SynchronizeFileResult.DidNotReceiveAllBytes
            }
        }

        return SynchronizeFileResult.LocalFileInfoNotSet // can this ever happen?
    }

    protected open fun getDefaultSavePathForFile(file: FileLink): File {
        return File("data", file.name) // better overwrite this to find a better save path for file
    }

    protected open fun saveStreamToFile(destinationFile: File, clientSocket: Socket, fileSize: Long): Long {
        val tempFile = File.createTempFile("DeepThought_SynchronizedFile", destinationFile.extension) // don't write directly to destinationFile as when synchronization fails a
        // corrupt file remains, may having overwritten a fully synchronized version of that file
        val outputStream = BufferedOutputStream(FileOutputStream(tempFile))
        val inputStream = DataInputStream(BufferedInputStream(clientSocket.getInputStream()))

        val buffer = ByteArray(FileSyncConfig.BufferSize)

        var receivedChunkSize: Int
        var receivedMessageSize = 0L

        do {
            receivedChunkSize = inputStream.read(buffer, 0, buffer.size)

            if(receivedChunkSize > 0) {
                receivedMessageSize += receivedChunkSize

                outputStream.write(buffer, 0, receivedChunkSize)
            }
        } while(receivedChunkSize >= 0)

        outputStream.flush()
        outputStream.close()

        if(receivedMessageSize == fileSize) {
            tempFile.copyTo(destinationFile, true)
        }
        tempFile.delete()

        return receivedMessageSize
    }

    protected open fun fileSuccessfullySynchronized(file: FileLink, localFileInfo: LocalFileInfo, destinationFile: File) {
        localFileInfo.path = destinationFile.path // use path not absolute path, see comment in getDefaultSavePathForFile()

        localFileInfo.fileSize = File(destinationFile.absolutePath).length() // we have to create a new File object to get file size
        localFileInfo.hashSHA256 = hashService.getFileHash(FileSyncConfig.FileHashAlgorithm, destinationFile)

        file.fileLastModified?.let { lastModified ->
            destinationFile.setLastModified(lastModified.time)
        }
        localFileInfo.fileLastModified = file.fileLastModified

        localFileInfo.syncStatus = if(localFileInfo.fileSize == file.fileSize) FileSyncStatus.UpToDate else FileSyncStatus.NotSynchronizedYet

        localFileInfoRepository.update(localFileInfo)
    }

    protected open fun sendMessage(clientSocket: Socket, message: Any) {
        val serializedResponse = serializer.serializeObject(message)
        val responseBytes = serializedResponse.toByteArray(FileServer.MESSAGE_CHARSET)

        socketHandler.sendMessage(clientSocket, responseBytes)
    }


    protected open fun getStoredLocalFileInfo(file: FileLink): LocalFileInfo? { // duplicate of FileManager.getStoredLocalFileInfo() but cannot inject FileManager here to avoid dependency cycle
        val localFileInfo = AtomicReference<LocalFileInfo?>(null)

        searchEngine.searchLocalFileInfo(LocalFileInfoSearch(file.id) { result ->
            if(result.isNotEmpty()) {
                localFileInfo.set(result[0])
            }
        })

        return localFileInfo.get()
    }


    protected open fun mapPermitSynchronizeFileResultToSynchronizeFileResult(result: PermitSynchronizeFileResult): SynchronizeFileResult {
        when(result) {
            PermitSynchronizeFileResult.DoNotHaveFile -> return SynchronizeFileResult.RemoteDoesNotHaveFile
            PermitSynchronizeFileResult.NoSlotsAvailableTryLater -> return SynchronizeFileResult.NoSlotsAvailableTryLater
            PermitSynchronizeFileResult.Prohibited -> return SynchronizeFileResult.Prohibited
            else -> return SynchronizeFileResult.ErrorOccurred
        }
    }

}