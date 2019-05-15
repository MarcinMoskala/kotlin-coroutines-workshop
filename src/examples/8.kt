package examples

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    fun getThreadName() = Thread.currentThread().name
    launch {
        println("main runBlocking      : I'm working in thread ${getThreadName()}")
    }
    launch(Dispatchers.Default) {
        println("Default               : I'm working in thread ${getThreadName()}")
        launch(Dispatchers.Unconfined) {
            println("Unconfined            : I'm working in thread ${getThreadName()}")
        }
    }

    launch(newSingleThreadContext("MyOwnThread")) {
        println("newSingleThreadContext: I'm working in thread ${getThreadName()}")
    }
}