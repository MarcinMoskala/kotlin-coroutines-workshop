package examples

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

suspend fun main() = coroutineScope<Unit> {
    fun getThreadName() = Thread.currentThread().name
    launch {
        println("main runBlocking      : I'm working in thread ${getThreadName()}")
    }
    launch(Dispatchers.Unconfined) {
        println("Unconfined            : I'm working in thread ${getThreadName()}")
    }
    launch(Dispatchers.Default) {
        println("Default               : I'm working in thread ${getThreadName()}")
    }
    launch(newSingleThreadContext("MyOwnThread")) {
        println("newSingleThreadContext: I'm working in thread ${getThreadName()}")
    }
}
