package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    println("Started producing")
    val channel = flow<Int> {
        println("Flow started")
        for (i in 1..3) {
            delay(100)
            emit(i)
        }
    }

    delay(100)
    println("Calling flow...")
    channel.collect { value -> println(value) }
    println("Consuming again...")
    channel.collect { value -> println(value) }
}