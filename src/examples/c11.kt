package examples.c7

import examples.massiveRun
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

sealed class CounterMsg
object IncCounter : CounterMsg()
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg()

fun CoroutineScope.counterActor(): Channel<CounterMsg> {
    val channel = Channel<CounterMsg>()
    launch {
        var counter = 0
        for (msg in channel) {
            when (msg) {
                is IncCounter -> counter++
                is GetCounter -> msg.response.complete(counter)
            }
        }
    }
    return channel
}

fun main() = runBlocking<Unit> {
    val channel = counterActor()
    GlobalScope.massiveRun { channel.send(IncCounter) }
    val response = CompletableDeferred<Int>()
    channel.send(GetCounter(response))
    println("Counter = ${response.await()}")
    channel.close()
}