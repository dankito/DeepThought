package net.dankito.utils

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class AsyncProducerConsumerQueue<T>(private val countThreadsToUse: Int, maxItemsToQueue: Int = NO_LIMIT_ITEMS_TO_QUEUE,
                                    minimumMillisecondsToWaitBeforeConsumingItem: Int = WAITING_BEFORE_CONSUMING_ITEM_DISABLED, autoStart: Boolean = true,
                                    private val consumerListener: (item: T) -> Unit) {

    companion object {

        val WAITING_BEFORE_CONSUMING_ITEM_DISABLED = 0

        val NO_LIMIT_ITEMS_TO_QUEUE = Integer.MAX_VALUE // no limit

        private val log = LoggerFactory.getLogger(AsyncProducerConsumerQueue::class.java)
    }


    private var producedItemsQueue: BlockingQueue<T>

    private var minimumMillisecondsToWaitBeforeConsumingItem = WAITING_BEFORE_CONSUMING_ITEM_DISABLED

    private var waitBeforeConsumingItemTimer = Timer("WaitBeforeConsumingItemTimer")

    private var consumerThreads: MutableList<Thread> = ArrayList()


    init {
        this.producedItemsQueue = LinkedBlockingQueue<T>(maxItemsToQueue)
        this.minimumMillisecondsToWaitBeforeConsumingItem = minimumMillisecondsToWaitBeforeConsumingItem

        if(autoStart) {
            start()
        }
    }


    val isEmpty: Boolean
        get() = queuedItemsCount == 0

    val queuedItemsCount: Int
        get() = producedItemsQueue.size

    val isRunning: Boolean
        get() = consumerThreads.size > 0


    /**
     * To restart processing after a call to {@link #stop()} or start processing when constructor flag autoStart has been set to false, call this method.
     */
    fun start() {
        startConsumerThreads(countThreadsToUse)
    }

    /**
     * Stops processing.
     * If processing should be restarted, call method {@link #restart()}.
     */
    fun stop() {
        for(consumerThread in consumerThreads) {
            try {
                consumerThread.interrupt()
            } catch (ignored: Exception) { }
        }

        consumerThreads.clear()
    }

    /**
     * Stops processing and clears all items in queue.
     * If processing should be restarted, call method {@link #restart()}.
     */
    fun stopAndClearQueue() {
        val remainingItemsInQueue = ArrayList(producedItemsQueue)
        producedItemsQueue.clear()

        stop()

        // TODO: really consume remaining items even though stop() has already been called?
        for(item in remainingItemsInQueue) {
            consumeItem(item)
        }
    }


    protected fun startConsumerThreads(countThreads: Int) {
        for (i in 0..countThreads - 1) {
            startConsumerThread()
        }
    }

    protected fun startConsumerThread() {
        val consumerThread = Thread(Runnable { consumerThread() }, "AsyncProducerConsumerQueue" + consumerThreads.size)

        consumerThreads.add(consumerThread)

        consumerThread.start()
    }

    protected fun consumerThread() {
        while (Thread.interrupted() == false) {
            try {
                val nextItemToConsume = producedItemsQueue.take()
                consumeItem(nextItemToConsume)
            } catch (e: Exception) {
                if (e is InterruptedException == false) { // it's quite usual that on stopping thread an InterruptedException will be thrown
                    log.error("An error occurred in consumerThread()", e)
                } else
                // Java, i love you! After having externally called Thread.interrupt(), InterruptedException will be thrown but you have to call Thread.currentThread().interrupt() manually
                    Thread.currentThread().interrupt()
            }

        }

        log.info("consumerThread() stopped")
    }

    protected fun consumeItem(nextItemToConsume: T) {
        if (minimumMillisecondsToWaitBeforeConsumingItem <= WAITING_BEFORE_CONSUMING_ITEM_DISABLED) {
            passConsumedItemOnToListener(nextItemToConsume)
        } else {
            waitBeforeConsumingItemTimer.schedule(object : TimerTask() {
                override fun run() {
                    passConsumedItemOnToListener(nextItemToConsume)
                }
            }, minimumMillisecondsToWaitBeforeConsumingItem.toLong())
        }
    }

    protected fun passConsumedItemOnToListener(nextItemToConsume: T) {
        try {
            consumerListener(nextItemToConsume)
        } catch (e: Exception) { // urgently catch exceptions. otherwise if an uncaught exception occurs during handling, response loop would catch this exception and stop proceeding
            log.error("An error occurred while consuming produced item " + nextItemToConsume, e)
        }

    }


    fun add(producedItem: T): Boolean {
        // use offer() instead of put() and take() instead of poll(int), see http://supercoderz.in/2012/02/04/using-linkedblockingqueue-for-high-throughput-java-applications/
        return producedItemsQueue.offer(producedItem)
    }

}
