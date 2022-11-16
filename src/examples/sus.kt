package examples.sus

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

suspend fun main() {
    println("Before")



    println("After")
}

//private val executor = Executors.newSingleThreadScheduledExecutor {
//    Thread(it, "scheduler").apply { isDaemon = true }
//}

//executor.schedule({}, 1000, TimeUnit.MILLISECONDS)

//class User(val name: String)
//class ApiException: Throwable()
//
//fun fetchUser(callback: (User) -> Unit) {
//    thread {
//        Thread.sleep(1000)
//        callback(User("Test"))
//    }
//}
//fun fetchUser(onSuccess: (User) -> Unit, onError: (Throwable) -> Unit) {
//    thread {
//        Thread.sleep(1000)
//        if (Random.nextBoolean()) {
//            onSuccess(User("Test"))
//        } else {
//            onError(ApiException())
//        }
//    }
//}