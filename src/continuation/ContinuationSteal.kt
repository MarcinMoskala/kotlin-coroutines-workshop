@file:UseExperimental(ExperimentalCoroutinesApi::class)
package continuation

import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

fun main(): Unit = runBlocking<Unit> {
    val cont = continuationSteal<String>()
    delay(1000)
    cont?.resume("This is some text")
}

fun <T> continuationSteal(console: Console = Console()): Continuation<T>? = runBlocking {
    var continuation: Continuation<T>? = null
    GlobalScope.launch(Dispatchers.Unconfined) {
        console.println("Before")
        // TODO: Suspend in here and store continuation in continuation.
        // TODO: After continuation resume, print using `console` the value that was passed.
        console.println("After")
    }
    continuation
}

open class Console {

    open fun println(text: Any?) {
        kotlin.io.println(text)
    }
}