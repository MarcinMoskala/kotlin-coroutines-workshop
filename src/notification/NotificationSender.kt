@file:OptIn(ExperimentalCoroutinesApi::class)

package notification

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import kotlin.test.assertEquals

class NotificationsSender(
    private val client: NotificationsClient,
    private val exceptionCollector: ExceptionCollector,
    dispatcher: CoroutineDispatcher,
) {
    private val handler = CoroutineExceptionHandler { _, t -> exceptionCollector.collectException(t) }
    val scope = CoroutineScope(SupervisorJob() + handler + dispatcher)

    fun sendNotifications(notifications: List<Notification>) {
        for (notification in notifications) {
            scope.launch {
                client.send(notification)
            }
        }
    }

    fun cancel() {
        scope.coroutineContext.cancelChildren()
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
        val testDispatcher = StandardTestDispatcher()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector, testDispatcher)
        val notifications = List(20) { Notification("ID$it") }

        // when
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceUntilIdle()
        testDispatcher.scheduler.runCurrent()

        // then
        assertEquals(notifications, fakeNotificationsClient.sent)

        val time = testDispatcher.scheduler.currentTime
        assert(time >= 200) { "Function should block until all notifications are sent (it takes $time)" }
        assert(time < 400) { "20 notifications should be sent concurrently, so they should take around 200ms, but it takes $time" }
    }

    @Test
    fun `should support cancellation`() {
        val fakeNotificationsClient = FakeNotificationsClient(delayTime = 1000)
        val fakeExceptionCollector = FakeExceptionCollector()
        val testDispatcher = StandardTestDispatcher()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector, testDispatcher)
        val notifications = List(20) { Notification("ID$it") }

        // when
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceTimeBy(500)
        sender.cancel()

        // then
        assert(sender.scope.coroutineContext.job.children.all { it.isCancelled })

    }

    @Test
    fun `should not cancel other notifications, when one has exception`() {
        val fakeNotificationsClient = FakeNotificationsClient(delayTime = 100, failEvery = 10)
        val fakeExceptionCollector = FakeExceptionCollector()
        val testDispatcher = StandardTestDispatcher()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector, testDispatcher)
        val notifications = List(100) { Notification("ID$it") }

        // when
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceUntilIdle()

        // then
        assertEquals(90, fakeNotificationsClient.sent.size)
    }

    @Test
    fun `should send info about failed notifications`() {
        val fakeNotificationsClient = FakeNotificationsClient(delayTime = 100, failEvery = 10)
        val fakeExceptionCollector = FakeExceptionCollector()
        val testDispatcher = StandardTestDispatcher()
        val sender = NotificationsSender(fakeNotificationsClient, fakeExceptionCollector, testDispatcher)
        val notifications = List(100) { Notification("ID$it") }

        // when
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceUntilIdle()

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

    override suspend fun send(notification: Notification) {
        if (delayTime > 0) delay(delayTime)
        usedThreads += Thread.currentThread().name
        counter++
        if (counter % failEvery == 0) {
            throw FakeFailure(notification)
        }
        sent += notification
    }
}

class FakeFailure(val notification: Notification) : Throwable("Planned fail for notification ${notification.id}")

class FakeExceptionCollector : ExceptionCollector {
    var collected = emptyList<Throwable>()

    override fun collectException(throwable: Throwable) = synchronized(this) {
        collected += throwable
    }
}