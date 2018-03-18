package net.dankito.utils.services.network.download

import com.github.axet.wget.DirectMultipart
import com.github.axet.wget.RetryWrap
import com.github.axet.wget.SpeedInfo
import com.github.axet.wget.WGet
import com.github.axet.wget.info.BrowserInfo
import com.github.axet.wget.info.DownloadInfo
import com.github.axet.wget.info.URLInfo
import net.dankito.utils.IThreadPool
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean


class WGetFileDownloader(private val threadPool: IThreadPool) : IFileDownloader {

    companion object {
        private val log = LoggerFactory.getLogger(WGetFileDownloader::class.java)
    }


    override fun downloadAsync(url: String, destination: File, callback: (DownloadState) -> Unit) {
        threadPool.runAsync {
            download(url, destination, callback)
        }
    }

    override fun download(url: String, destination: File, callback: (DownloadState) -> Unit) {
        setOptions()

        val stop = AtomicBoolean(false)
        try {
            val info = DownloadInfo(URL(url))
            val status = WGetStatus(info, callback)

            info.extract(stop, status)
            info.enableMultipart()

            val wget = WGet(info, destination)
            // will block until download finishes
            wget.download(stop, status)
        } catch (e: Exception) {
            log.error("Could not download file $url", e)

            callback(DownloadState(true, false, error = e))
        }
    }

    private fun setOptions() {
        // i love static properties *g*
        DirectMultipart.THREAD_COUNT = 3
        SpeedInfo.SAMPLE_LENGTH = 1000
        SpeedInfo.SAMPLE_MAX = 20
        BrowserInfo.USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36"
        DownloadInfo.PART_LENGTH = 5 * 1024 * 1024 // bytes
        URLInfo.READ_TIMEOUT = 5 * 1000 // milliseconds
        URLInfo.CONNECT_TIMEOUT = 5 * 1000 // milliseconds
        RetryWrap.RETRY_COUNT = 5 /// 5 times then fail or -1 for infinite
        RetryWrap.RETRY_DELAY = 3 // seconds between retries
    }


    inner class WGetStatus(private val info: DownloadInfo, private val callback: (DownloadState) -> Unit) : Runnable {
        private var speedInfo = SpeedInfo()
        private var last: Long = 0

        init {
            speedInfo.start(0)
        }

        override fun run() {
            if(info.state != URLInfo.States.DOWNLOADING) {
                log.info("New download state: ${info.state}")
            }

            when(info.state) {
                URLInfo.States.EXTRACTING, URLInfo.States.EXTRACTING_DONE -> { }
                URLInfo.States.DOWNLOADING -> handleStatusDownloading()
                URLInfo.States.RETRYING -> log.info(info.state.toString() + " r:" + info.retry + " d:" + info.delay)
                URLInfo.States.ERROR -> handleError()
                URLInfo.States.DONE -> handleStatusDone()
                else -> { }
            }
        }

        private fun handleStatusDownloading() {
            speedInfo.step(info.count)
            val now = System.currentTimeMillis()

            if(now - 1000 > last) {
                last = now

                var parts = ""

                if(info.parts != null) { // not null if multipart enabled
                    for(p in info.parts) {
                        when(p.state) {
                            DownloadInfo.Part.States.DOWNLOADING -> parts += String.format("Part#%d(%.2f) ", p.number,
                                    p.count / p.length.toFloat())
                            DownloadInfo.Part.States.ERROR, DownloadInfo.Part.States.RETRYING -> parts += String.format("Part#%d(%s) ", p.number,
                                    p.exception.message + " r:" + p.retry + " d:" + p.delay)
                            else -> {
                            }
                        }
                    }
                }

                val progress = info.count / info.length.toFloat()

                log.info(String.format("%.2f %s (%s / %s)", progress, parts,
                        formatSpeed(speedInfo.currentSpeed.toLong()), formatSpeed(speedInfo.averageSpeed.toLong())))

                callback(DownloadState(false, false, progress * 100))
            }
        }

        private fun handleStatusDone() {
            // finish speed calculation by adding remaining bytes speed
            speedInfo.end(info.count)
            // print speed
            log.info(String.format("%s average speed (%s)", info.state,
                    formatSpeed(speedInfo.averageSpeed.toLong())))
            callback(DownloadState(true, true, 100f))
        }

        private fun handleError() {
            log.info("Error = ${info.exception}", info.exception)
        }

        private fun formatSpeed(s: Long): String {
            if (s > 0.1 * 1024.0 * 1024.0 * 1024.0) {
                val f = s.toFloat() / 1024f / 1024f / 1024f
                return String.format("%.1f GB", f)
            }
            else if (s > 0.1 * 1024.0 * 1024.0) {
                val f = s.toFloat() / 1024f / 1024f
                return String.format("%.1f MB", f)
            }
            else {
                val f = s / 1024f
                return String.format("%.1f kb", f)
            }
        }

    }

}