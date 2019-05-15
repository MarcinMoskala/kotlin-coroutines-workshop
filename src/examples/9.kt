package examples

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*

suspend fun makeAsyncCalculations(): String {
    val one = GlobalScope.async { doSomethingUsefulOne() }
    val two = GlobalScope.async { doSomethingUsefulTwo() }
    return "The answer is ${one.await() + two.await()}"
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000)
    println("I am done")
    return 1
}

val random = Random()

suspend fun doSomethingUsefulTwo(): Int {
    delay(100)
    if (random.nextBoolean()) throw Error() else return 2
}

fun main() = runBlocking {
    val value = makeAsyncCalculations()
    println(value)
}