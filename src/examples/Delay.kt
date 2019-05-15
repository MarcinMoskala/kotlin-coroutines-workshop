package examples

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

private val excecutor = Executors.newSingleThreadScheduledExecutor {
    Thread(it, "scheduler").apply { isDaemon = true }
}!!

suspend fun customDelay(time: Long): Unit = suspendCoroutine { cont ->
    excecutor.schedule({ cont.resume(Unit) }, time, TimeUnit.MILLISECONDS)
}