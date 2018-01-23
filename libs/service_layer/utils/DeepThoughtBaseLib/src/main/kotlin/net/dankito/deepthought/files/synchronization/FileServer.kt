package net.dankito.deepthought.files.synchronization

import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.network.communication.SocketHandler
import net.dankito.deepthought.files.synchronization.model.PermitSynchronizeFileRequest
import net.dankito.deepthought.files.synchronization.model.PermitSynchronizeFileResponse
import net.dankito.deepthought.files.synchronization.model.PermitSynchronizeFileResult
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IThreadPool
import net.dankito.utils.serialization.ISerializer
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.BindException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.nio.charset.Charset
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread


class FileServer(private val searchEngine: ISearchEngine, private val entityManager: IEntityManager, private val networkSettings: INetworkSettings,
                 private val socketHandler: SocketHandler, private val serializer: ISerializer, private val threadPool: IThreadPool) {


    companion object {
        const val MESSAGE_CHARSET_NAME = "UTF-8"

        val MESSAGE_CHARSET = Charset.forName(MESSAGE_CHARSET_NAME)

        private val log = LoggerFactory.getLogger(FileServer::class.java)
    }


    private var serverSocket: ServerSocket? = null

    private var maxSimultaneousConnections = 1

    private val currentConnections = CopyOnWriteArrayList<Socket>()


    fun startServerAsync(desiredPort: Int = 60705, maxSimultaneousConnections: Int = 1, startingServerDone: ((startedSuccessfully: Boolean) -> Unit)? = null) {
        this.maxSimultaneousConnections = maxSimultaneousConnections

        thread {
            createServerSocket(desiredPort, startingServerDone)
        }
    }


    private fun createServerSocket(desiredPort: Int, startingServerDone: ((startedSuccessfully: Boolean) -> Unit)? = null) {
        try {
            serverSocket = ServerSocket(desiredPort)

            networkSettings.fileSynchronizationPort = desiredPort
            startingServerDone?.invoke(true)

            waitForArrivingRequests()
        } catch (e: Exception) {
            log.warn("Could not bind serverSocket to port " + desiredPort, e)

            if((e is BindException || e is IOException) && desiredPort < 65535) {
                createServerSocket(desiredPort + 1, startingServerDone)
            }
            else {
                log.error("Could not start FileServer")
                startingServerDone?.invoke(false)
            }
        }
    }


    private fun waitForArrivingRequests() {
        while(Thread.currentThread().isInterrupted == false && serverSocket != null) {
            try {
                serverSocket?.accept()?.let { clientSocket ->
                    receivedRequestAsync(clientSocket)
                }
            } catch (e: Exception) {
                if(e is InterruptedException || e is SocketException) {
                    break
                }
                else {
                    log.error("An error occurred accepting client request", e)
                }
            }

        }
    }

    internal fun receivedRequestAsync(clientSocket: Socket) {
        threadPool.runAsync { receivedRequest(clientSocket) }
    }

    private fun receivedRequest(clientSocket: Socket) {
        val socketResult = socketHandler.receiveMessage(clientSocket)

        if (socketResult.isSuccessful) {
            socketResult.receivedMessage?.let { receivedMessage ->
                receivedRequest(clientSocket, receivedMessage)
            }
        } else {
            // TODO: what to do in error case?

            closeClientSocket(clientSocket)
        }
    }

    private fun receivedRequest(clientSocket: Socket, requestString: String) {
        try {
            log.info("Received file request: $requestString")

            val request = serializer.deserializeObject(requestString, PermitSynchronizeFileRequest::class.java)
            receivedRequest(clientSocket, request)
        } catch (e: Exception) {
            log.error("Could not deserialize request string " + requestString, e)
            sendResponseAndCloseSocket(clientSocket, PermitSynchronizeFileResult.ErrorOccurred, null, e)
        }

    }

    private fun receivedRequest(clientSocket: Socket, request: PermitSynchronizeFileRequest) {
        val fileId = request.fileId

        if(currentConnections.size >= maxSimultaneousConnections) {
            sendResponseAndCloseSocket(clientSocket, PermitSynchronizeFileResult.NoSlotsAvailableTryLater, fileId)
            return
        }

        val fileIdContainer = FileLink("/")
        fileIdContainer.id = fileId

        val localFileInfo = searchEngine.getLocalFileInfo(fileIdContainer)

        if(localFileInfo == null || localFileInfo.path == null || localFileInfo.syncStatus != FileSyncStatus.UpToDate || File(localFileInfo.path).exists() == false) {
            sendResponseAndCloseSocket(clientSocket, PermitSynchronizeFileResult.DoNotHaveFile, fileId)
        }
        else {
            sendResponse(clientSocket, PermitSynchronizeFileResult.SynchronizationPermitted, fileId)

            sendFileToClient(clientSocket, localFileInfo)
        }
    }

    private fun sendFileToClient(clientSocket: Socket, localFileInfo: LocalFileInfo) {
        localFileInfo.path?.let { filePath ->
            log.info("Sending file $filePath to client ${clientSocket.inetAddress}")

            socketHandler.sendMessage(clientSocket, FileInputStream(filePath))
        }

        closeClientSocket(clientSocket)
    }


    private fun sendResponse(clientSocket: Socket, result: PermitSynchronizeFileResult, fileId: String?,
                             error: Exception? = null) {
        val serializedResponse = serializer.serializeObject(PermitSynchronizeFileResponse(result, fileId, error))
        val responseBytes = serializedResponse.toByteArray(MESSAGE_CHARSET)

        socketHandler.sendMessage(clientSocket, responseBytes)
    }

    private fun sendResponseAndCloseSocket(clientSocket: Socket, result: PermitSynchronizeFileResult, fileId: String?,
                                           error: Exception? = null) {
        sendResponse(clientSocket, result, fileId, error)

        closeClientSocket(clientSocket)
    }

    private fun closeClientSocket(clientSocket: Socket) {
        socketHandler.closeSocket(clientSocket)
    }

}