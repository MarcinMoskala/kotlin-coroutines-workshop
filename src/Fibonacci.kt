import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

val fibonacci = sequence {
    yield(1)
    yield(1)
    var m1 = 1
    var m2 = 1
    while (true) {
        val next = m1 + m2
        yield(m2 + m1)
        m2 = m1
        m1 = next
    }
}

@Suppress("FunctionName")
internal class FibonacciTests {

    @Test
    fun `First two numbers should be 1 and 1`() {
        assertEquals(listOf(1, 1), fibonacci.take(2).toList())
    }

    @Test
    fun `Check first 11 numbers`() {
        assertEquals(listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89), fibonacci.take(11).toList())
    }
}