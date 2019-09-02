package examples.c5

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}

fun main() = runBlocking {
    val channel = Channel<String>()
    launch { sendString(channel, "foo", 200L) }
    launch { sendString(channel, "BAR!", 500L) }
    repeat(1000) {
        println(channel.receive())
    }
    coroutineContext.cancelChildren()
}