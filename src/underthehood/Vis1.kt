package underthehood.v1

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

//sampleStart
fun myFunction(continuation: Continuation<Unit>): Any {
    val continuation =
        if (continuation is MyFunctionContinuation) continuation
        else MyFunctionContinuation(continuation)
    if (continuation.label == 0) {
        println("Before")
        continuation.label = 1
        if (delay(1000, continuation) == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
    }
    if (continuation.label == 1) {
        println("After")
        return Unit
    }
    error("Impossible")
}

class MyFunctionContinuation(val completion: Continuation<Unit>) : Continuation<Unit> {
    override val context: CoroutineContext // Don't think about it now
        get() = completion.context

    var label = 0

    override fun resumeWith(result: Result<Unit>) {
        if (result.isSuccess) {
            val res = myFunction(this)
            completion.resume(res as Unit)
        }
        // ... (we will talk about it later)
    }
}

fun main() {
    toStart()
}
//sampleEnd

private val executor = Executors.newSingleThreadScheduledExecutor {
    Thread(it, "scheduler").apply { isDaemon = true }
}

fun delay(timeMillis: Long, continuation: Continuation<Unit>): Any {
    executor.schedule({ continuation.resume(Unit) }, timeMillis, TimeUnit.MILLISECONDS)
    return COROUTINE_SUSPENDED
}

fun toStart() {
    val EMPTY_CONTINUATION = object : Continuation<Unit> {
        override val context: CoroutineContext = EmptyCoroutineContext
        // Here would

        override fun resumeWith(result: Result<Unit>) {
            // This is root coroutine, we don't need anything in this example
        }
    }
    myFunction(EMPTY_CONTINUATION)
    Thread.sleep(2000) // No structured concurrency, so it is needed
}

val COROUTINE_SUSPENDED = Any()