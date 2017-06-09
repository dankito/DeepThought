package net.dankito.utils


interface IThreadPool {
    fun runAsync(runnable: () -> Unit)
    fun shutDown()
}