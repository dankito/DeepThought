package net.dankito.utils

import java.util.concurrent.Executors


class ThreadPool : IThreadPool {

    private val threadPool = Executors.newCachedThreadPool()


    override fun runAsync(runnable: Runnable) {
        threadPool.execute(runnable)
    }

    override fun shutDown() {
        if (threadPool != null && threadPool.isShutdown) {
            threadPool.shutdownNow()
        }
    }

}