package underthehood.v4

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
        result?.throwOnFailure()
        println("Before")
        continuation.label = 1
        val res = getUserId(token, continuation)
        if (res == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
        result = Success(res)
    }
    if (continuation.label == 1) {
        result?.throwOnFailure()
        userId = (result as Success<String>).value
        println("Got userId: $userId")
        continuation.label = 2
        continuation.userId = userId
        val res = getUserName(userId, continuation)
        if (res == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
        result = Success(res)
    }
    if (continuation.label == 2) {
        result?.throwOnFailure()
        userName = (result as Success<String>).value
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

    override fun resumeWith(result: kotlin.Result<String>) {
        this.result = result.toCustomResult()
        val res = try {
            val r = printUser(token, this)
            if (r == COROUTINE_SUSPENDED) return
            Success(r)
        } catch (e: Throwable) {
            Failure(e)
        }
        val kotlinResult = when (res) {
            is Success<*> -> kotlin.Result.success(res.value as Unit)
            is Failure -> kotlin.Result.failure(res.exception)
        }
        completion.resumeWith(kotlinResult)
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

// I used my own instead of built in, because it still cannot be returned
sealed class Result<out T>
data class Success<out T>(val value: T) : Result<T>()
data class Failure(val exception: Throwable) : Result<Nothing>()

fun <T> kotlin.Result<T>.toCustomResult(): Result<T> =
    if (this.isSuccess) Success(this.getOrThrow())
    else Failure(this.exceptionOrNull()!!)

private fun Result<*>.throwOnFailure() {
    if (this is Failure) throw this.exception
}

private val COROUTINE_SUSPENDED = Any()