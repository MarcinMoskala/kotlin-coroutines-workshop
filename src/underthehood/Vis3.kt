package underthehood.v3

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

//sampleStart
fun printUser(token: String, continuation: Continuation<Nothing>): Any {
    val continuation =
        if (continuation is MyFunctionContinuation) continuation
        else MyFunctionContinuation(continuation as Continuation<Unit>, token)

    var result = continuation.result
    var userId = continuation.userId
    val userName: String

    if (continuation.label == 0) {
        println("Before")
        continuation.label = 1
        result = getUserId(token, continuation)
        if (result == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
    }
    if (continuation.label == 1) {
        userId = result as String
        println("Got userId: $userId")
        continuation.label = 2
        continuation.userId = userId
        result = getUserName(userId, continuation)
        if (result == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
    }
    if (continuation.label == 2) {
        userName = result as String
        println(User(userId as String, userName))
        println("After")
        return Unit
    }
    error("Impossible")
}

class MyFunctionContinuation(val completion: Continuation<Unit>, val token: String) : Continuation<String> {
    override val context: CoroutineContext // Don't think about it now
        get() = completion.context

    var label = 0
    var result: Any? = null
    var userId: String? = null

    override fun resumeWith(result: Result<String>) {
        if (result.isSuccess) {
            this.result = result.getOrNull()
            val res = printUser(token, this)
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

data class User(val id: String, val name: String)

fun getUserId(token: String, continuation: Continuation<String>): Any {
    executor.schedule({ continuation.resume("SomeId") }, 1000, TimeUnit.MILLISECONDS)
    return COROUTINE_SUSPENDED
}

fun getUserName(userId: String, continuation: Continuation<String>): Any {
    executor.schedule({ continuation.resume("SomeName") }, 1000, TimeUnit.MILLISECONDS)
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
    printUser("SomeToken", EMPTY_CONTINUATION)
    Thread.sleep(3000) // No structured concurrency, so it is needed
}

private val COROUTINE_SUSPENDED = Any()