package examples

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

suspend fun makeAsyncCalculationsInCoroutineScope(): String = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    "The answer is ${one.await() + two.await()}"
}

fun main() = runBlocking {
    val value = makeAsyncCalculationsInCoroutineScope()
    println(value)
}