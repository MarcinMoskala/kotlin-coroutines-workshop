package examples

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test

suspend fun main() {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    delay(2000L)
}

class MyTest {
    @Test
    fun testMySuspendingFunction() {
        var a = "AA"
//        delay(1000)
        assert(1 == 1)
    }
}