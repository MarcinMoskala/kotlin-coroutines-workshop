package examples.c1

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val channel = Channel<Int>()
    launch {
        repeat(5) { index ->
            channel.send(index * 2)
            delay(1000)
        }
    }

    repeat(5) {
        val received = channel.receive()
        print(received)
    }
}