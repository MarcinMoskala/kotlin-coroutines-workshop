package notification

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class NotificationsSender(
    val client: NotificationsClient,
    val exceptionCollector: ExceptionCollector,
) {

    fun sendNotifications(notifications: List<Notification>) {
    }

    fun cancel() {
    }
}

data class Notification(val id: String)

interface NotificationsClient {
    suspend fun send(notification: Notification)
}

interface ExceptionCollector {
    fun collectException(throwable: Throwable)
}

class NotificationsSenderTest {

    @Test
    fun `should send 20 notifications concurrently`() {
        val fakeNotificationsClient = FakeNotificationsClient(delayTime = 200)
        val fakeExceptionCollector = FakeExceptionCollector()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector)
        val notifications = List(20) { Notification("ID$it") }

        // when
        val time = measureTimeMillis {
            sender.sendNotifications(notifications)
        }

        // then
        assert(time >= 200) { "Function should block until all notifications are sent (it takes $time)" }
        assert(time < 400) { "20 notifications should be sent concurrently, so they should take around 200ms, but it takes $time" }
    }

    @Test
    fun `should support cancellation`() {
        val fakeNotificationsClient = FakeNotificationsClient(delayTime = 1000)
        val fakeExceptionCollector = FakeExceptionCollector()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector)
        val notifications = List(20) { Notification("ID$it") }

        // when
        thread {
            Thread.sleep(500)
            sender.cancel()
        }
        val time = measureTimeMillis {
            sender.sendNotifications(notifications)
        }

        // then
        assert(time in 500..800) { "Cancellation after 500, should take above 500, takes $time" }
    }

    @Test
    fun `should not cancel other notifications, when one has exception`() {
        val fakeNotificationsClient = FakeNotificationsClient(delayTime = 100, failEvery = 10)
        val fakeExceptionCollector = FakeExceptionCollector()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector)
        val notifications = List(100) { Notification("ID$it") }

        // when
        sender.sendNotifications(notifications)

        // then
        assertEquals(90, fakeNotificationsClient.sent.size)
    }

    @Test
    fun `should send info about failed notifications`() {
        val fakeNotificationsClient = FakeNotificationsClient(delayTime = 100, failEvery = 10)
        val fakeExceptionCollector = FakeExceptionCollector()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector)
        val notifications = List(100) { Notification("ID$it") }

        // when
        sender.sendNotifications(notifications)

        // then
        assertEquals(10, fakeExceptionCollector.collected.size)
    }
}

class FakeNotificationsClient(
    val delayTime: Long = 0L,
    val failEvery: Int = Int.MAX_VALUE
) : NotificationsClient {
    var sent = emptyList<Notification>()
    var counter = 0
    var usedThreads = emptyList<String>()
    val dispatcher = Dispatchers.IO.limitedParallelism(1)

    override suspend fun send(notification: Notification) {
        if (delayTime > 0) delay(delayTime)
        usedThreads += Thread.currentThread().name
        withContext(dispatcher) {
            counter++
            if (counter % failEvery == 0) {
                throw FakeFailure(notification)
            }
            sent += notification
        }
    }
}

class FakeFailure(val notification: Notification) : Throwable("Planned fail for notification ${notification.id}")

class FakeExceptionCollector : ExceptionCollector {
    var collected = emptyList<Throwable>()

    override fun collectException(throwable: Throwable) = synchronized(this) {
        collected += throwable
    }
}