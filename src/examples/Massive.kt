package examples

import kotlin.concurrent.thread

//fun main() = runBlocking {
//    repeat(100_000) {
//        launch {
//            delay(1000L)
//            print(".")
//        }
//    }
//}

// No! Don't do it! Very bed idea on threads
fun main() {
    repeat(100_000) {
        thread {
            Thread.sleep(1000L)
            print(".")
        }
    }
}