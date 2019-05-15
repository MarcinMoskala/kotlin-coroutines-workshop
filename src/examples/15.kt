package examples

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    supervisorScope {
        launch(CoroutineExceptionHandler { _, _ ->  }) {
            delay(1000)
            throw AssertionError("Cancelled")
        }
        launch {
            delay(2000)
            println("AAA")
        }
    }
}