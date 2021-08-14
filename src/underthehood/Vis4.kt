package underthehood.v4

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

//sampleStart
fun printUser(token: String, continuation: Continuation<Nothing>): Any {
    val continuation =
        if (continuation is MyFunctionContinuation) continuation
        else MyFunctionContinuation(continuation as Continuation<Unit>, token)

    var result: Result<Any>? = continuation.result
    var userId: String? = continuation.userId
    val userName: String

    if (continuation.label == 0) {
        result?.throwOnFailure()
        println("Before")
        continuation.label = 1
        val res = getUserId(token, continuation)
        if (res == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
        result = Result.success(res)
    }
    if (continuation.label == 1) {
        result!!.throwOnFailure()
        userId = result.getOrNull() as String
        println("Got userId: $userId")
        continuation.label = 2
        continuation.userId = userId
        val res = getUserName(userId, continuation)
        if (res == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
        result = Result.success(res)
    }
    if (continuation.label == 2) {
        result!!.throwOnFailure()
        userName = result.getOrNull() as String
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
    var result: Result<Any>? = null
    var userId: String? = null

    override fun resumeWith(result: Result<String>) {
        this.result = result
        val res = try {
            val r = printUser(token, this)
            if (r == COROUTINE_SUSPENDED) return
            Result.success(r as Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
        completion.resumeWith(res)
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
object ApiException : Throwable("Fake API exception")

fun getUserId(token: String, continuation: Continuation<String>): Any {
    executor.schedule({ continuation.resume("SomeId") }, 1000, TimeUnit.MILLISECONDS)
    return COROUTINE_SUSPENDED
}

fun getUserName(userId: String, continuation: Continuation<String>): Any {
    executor.schedule({
//        continuation.resume("SomeName")
        continuation.resumeWithException(ApiException)
    }, 1000, TimeUnit.MILLISECONDS)
    return COROUTINE_SUSPENDED
}

fun toStart() {
    val EMPTY_CONTINUATION = object : Continuation<Unit> {
        override val context: CoroutineContext = EmptyCoroutineContext
        // Here would

        override fun resumeWith(result: kotlin.Result<Unit>) {
            if (result.isFailure) {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }
    printUser("SomeToken", EMPTY_CONTINUATION)
    Thread.sleep(3000) // No structured concurrency, so it is needed
}

private fun Result<*>.throwOnFailure() {
    if (isFailure) throw exceptionOrNull()!!
}

private val COROUTINE_SUSPENDED = Any()