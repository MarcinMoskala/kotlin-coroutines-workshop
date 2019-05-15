import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    println("Started!")
    test()
    println("Done.")
}

suspend fun test() {
    delay(1000)
}