package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.message.IMessageHandler
import net.dankito.utils.ThreadPool
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


class RequestReceiverTest {


    protected lateinit var underTest: RequestReceiver


    protected lateinit var requestReceiver2: RequestReceiver


    @Before
    @Throws(Exception::class)
    fun setUp() {
        val socketHandler = Mockito.mock(SocketHandler::class.java)
        val messageHandler = Mockito.mock(IMessageHandler::class.java)
        val messageSerializer = Mockito.mock(IMessageSerializer::class.java)
        val threadPool = ThreadPool()

        underTest = RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool)

        requestReceiver2 = RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        underTest.close()

        requestReceiver2.close()
    }


    @Test
    @Throws(Exception::class)
    fun start_DesiredPortOutOfExpectedRange() {
        val startResultHolder = AtomicBoolean(true)
        val countDownLatch = CountDownLatch(1)


        val portOutOfRange = Math.pow(2.0, 16.0).toInt()
        underTest.start(portOutOfRange, object : RequestReceiverCallback {
            override fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                startResultHolder.set(couldStartReceiver)
                countDownLatch.countDown()
            }
        })


        try {
            countDownLatch.await(1, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }

        assertThat(startResultHolder.get(), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun start_DesiredPortAlreadyBound() {
        val countDownLatchRequestReceiver2 = CountDownLatch(1)

        val alreadyBoundPort = TEST_PORT
        requestReceiver2.start(alreadyBoundPort, object : RequestReceiverCallback {
            override fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                countDownLatchRequestReceiver2.countDown()
            }
        })

        try {
            countDownLatchRequestReceiver2.await(1, TimeUnit.SECONDS)
        } catch (ignored: Exception) { }


        val selectedPortHolder = AtomicInteger()
        val countDownLatchRequestReceiverUnderTest = CountDownLatch(1)


        underTest.start(alreadyBoundPort, object : RequestReceiverCallback {
            override fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                if (couldStartReceiver) {
                    selectedPortHolder.set(messagesReceiverPort)
                    countDownLatchRequestReceiverUnderTest.countDown()
                }
            }
        })


        try {
            countDownLatchRequestReceiverUnderTest.await(1, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }

        assertThat(selectedPortHolder.get(), `is`(alreadyBoundPort + 1))
    }


    @Test
    @Throws(Exception::class)
    fun close_SocketGetClosed() {
        val testPort = TEST_PORT

        val countDownLatchReceiver1 = CountDownLatch(1)

        underTest.start(testPort, object : RequestReceiverCallback {
            override fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                if (couldStartReceiver) {
                    countDownLatchReceiver1.countDown()
                }
            }
        })

        try {
            countDownLatchReceiver1.await(1, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }


        underTest.close()


        testIfSocketGotClosed(testPort)
    }

    protected fun testIfSocketGotClosed(testPort: Int) {
        // now start second receiver on some port to check if port is available again
        val countDownLatchReceiver2 = CountDownLatch(1)
        val receiver2SelectedPortHolder = AtomicInteger()

        requestReceiver2.start(testPort, object : RequestReceiverCallback {
            override fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                if (couldStartReceiver) {
                    receiver2SelectedPortHolder.set(messagesReceiverPort)
                    countDownLatchReceiver2.countDown()
                }
            }
        })

        try {
            countDownLatchReceiver2.await(1, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }

        assertThat(receiver2SelectedPortHolder.get(), `is`(testPort))
    }

    companion object {

        protected val TEST_PORT = 54321
    }

}