package continuation

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

    @Test(timeout = 500)
    fun `At the beginning function says "Before"`() {
        val fakeConsole = FakeConsole()
        continuationSteal<String>(fakeConsole)
        assertEquals("Before", fakeConsole.printed.first())
    }

    @Test(timeout = 500)
    fun `At the end function says "After"`() {
        val fakeConsole = FakeConsole()
        val cont = continuationSteal<String>(fakeConsole)
        cont?.resume(fakeText)
        assertEquals("After", fakeConsole.printed.last())
    }

    @Test(timeout = 500)
    fun `In the middle, we suspend function`() {
        val fakeConsole = FakeConsole()
        val cont = continuationSteal<String>(fakeConsole)
        assertEquals(mutableListOf<Any?>("Before"), fakeConsole.printed)
    }

    @Test(timeout = 500)
    fun `Function should return continuation`() {
        val fakeConsole = FakeConsole()
        val cont = continuationSteal<String>(fakeConsole)
        assertNotNull(cont)
        cont.resume(fakeText)
        assertEquals("After", fakeConsole.printed.last())
    }

    @Test(timeout = 500)
    fun `Only "Before" is printed before resume`() {
        val fakeConsole = FakeConsole()
        val cont = continuationSteal<String>(fakeConsole)
        assertNotNull(cont)
        assertEquals("Before", fakeConsole.printed.first())
    }

    @Test(timeout = 500)
    fun `After resume function should print text to resume`() {
        val fakeConsole = FakeConsole()
        val cont = continuationSteal<String>(fakeConsole)
        cont?.resume(fakeText)
        assertEquals(3, fakeConsole.printed.size)
        assertEquals(fakeText, fakeConsole.printed[1])
    }
}