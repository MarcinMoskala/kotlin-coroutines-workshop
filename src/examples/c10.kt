package examples.c6

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    val channel = Channel<Int>(4)
    val sender = launch {
        repeat(10) {
            println("Sending $it")
            channel.send(it)
        }
    }
    delay(1000)
    sender.cancel()
}