package continuation

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.coroutines.resume
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("FunctionName")
class ContinuationStealTests {

    private val fakeText = "This is some text"

    class FakeConsole : Console() {
        val printed = mutableListOf<Any?>()

        override fun println(text: Any?) {
            printed += text
        }
    }

    @Test
    fun `At the beginning function says "Before"`() = runBlockingTest {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        delay(100)
        assertEquals("Before", fakeConsole.printed.first())
        job.cancel()
    }

    @Test
    fun `At the end function says "After"`() = runBlockingTest {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        continuation?.resume(fakeText)
        assertEquals("After", fakeConsole.printed.last())
    }

    @Test
    fun `In the middle, we suspend function`() = runBlockingTest {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        assertEquals(mutableListOf<Any?>("Before"), fakeConsole.printed)
        job.cancel()
    }

    @Test
    fun `Function should return continuation`() = runBlockingTest {
        val fakeConsole = FakeConsole()
        launch {
            continuationSteal(fakeConsole)
        }
        assertNotNull(continuation).resume(fakeText)
        assertEquals("After", fakeConsole.printed.last())
    }

    @Test
    fun `Only "Before" is printed before resume`() = runBlockingTest {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        assertEquals("Before", fakeConsole.printed.first())
        job.cancel()
    }

    @Test
    fun `After resume function should print text to resume`() = runBlockingTest {
        val fakeConsole = FakeConsole()
        launch {
            continuationSteal(fakeConsole)
        }
        continuation?.resume(fakeText)
        assertEquals(3, fakeConsole.printed.size)
        assertEquals(fakeText, fakeConsole.printed[1])
    }
}
