package examples

import kotlinx.coroutines.*

var counter = 0

fun main() = runBlocking {
    GlobalScope.massiveRun {
        counter++
    }
    println("Counter = ${counter}")
}

suspend fun CoroutineScope.massiveRun(action: suspend () -> Unit) {
    val jobs = List(1000) {
        launch {
            repeat(1000) { action() }
        }
    }
    jobs.forEach { it.join() }
}
