package net.dankito.util


interface IThreadPool {

    fun runAsync(runnable: () -> Unit)

    fun shutDown()

}