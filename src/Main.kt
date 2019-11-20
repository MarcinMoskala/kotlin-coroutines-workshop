import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    delay(1000)
    print("Hello, World")
}

//suspend fun main() = coroutineScope<Unit> {
//    delay(1000)
//    print("Hello, World")
//}