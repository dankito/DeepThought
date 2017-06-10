package net.dankito.utils


interface IThreadPool {

    fun runAsync(runnable: Runnable)

    fun shutDown()

}