package flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

// Produces a flow of Unit
// For instance producingUnits(5) -> [Unit, Unit, Unit, Unit, Unit]
fun producingUnits(num: Int): Flow<Unit> = TODO()

// Adds a delay of time `timeMillis` between elements
fun <T> Flow<T>.delayEach(timeMillis: Long): Flow<T> = TODO()

// Produces not only elements, but the whole history till now
// For instance flowOf(1, "A", 'C').withHistory() -> [[1], [1, A], [1, A, C]]
fun <T> Flow<T>.withHistory(): Flow<List<T>> = TODO()

// Should transform Unit's to toggled boolean value starting from true
// For instance flowOf(Unit, Unit, Unit, Unit).toNextNumbers() -> [true, false, true, false]
fun Flow<Unit>.toToggle(): Flow<Boolean> = TODO()

// Should transform Unit's to next numbers startling from 1
// For instance flowOf(Unit, Unit, Unit, Unit).toNextNumbers() -> [1, 2, 3, 4]
fun Flow<Unit>.toNextNumbers(): Flow<Int> = TODO()

// Should create a flow that every `tickEveryMillis` should emit next numbers from `startNum` to `endNum`
// For instance
fun makeTimer(tickEveryMillis: Long, startNum: Int, endNum: Int): Flow<Int> = TODO()

@Suppress("FunctionName")
class FlowTests {

    @Test()
    fun producingUnitsTests() = runBlockingTest {
        assertEquals(listOf(), producingUnits(0).toList())
        assertEquals(listOf(Unit), producingUnits(1).toList())
        assertEquals(listOf(Unit, Unit), producingUnits(2).toList())
        assertEquals(listOf(Unit, Unit, Unit), producingUnits(3).toList())
        for (i in 1..100 step 7) {
            assertEquals(List(i) { Unit }, producingUnits(i).toList())
        }
    }

    @Test()
    fun toToggleTests() = runBlockingTest {
        assertEquals(listOf(), producingUnits(0).toToggle().toList())
        assertEquals(listOf(true), producingUnits(1).toToggle().toList())
        assertEquals(listOf(true, false), producingUnits(2).toToggle().toList())
        assertEquals(listOf(true, false, true), producingUnits(3).toToggle().toList())
        assertEquals(listOf(true, false, true, false), producingUnits(4).toToggle().toList())
    }

    @Test()
    fun toNextNumbersTests() = runBlockingTest {
        assertEquals(listOf(), producingUnits(0).toNextNumbers().toList())
        assertEquals(listOf(1), producingUnits(1).toNextNumbers().toList())
        assertEquals(listOf(1, 2), producingUnits(2).toNextNumbers().toList())
        assertEquals(listOf(1, 2, 3), producingUnits(3).toNextNumbers().toList())
        for (i in 1..100 step 7) {
            val list = List(i) { it + 1 }
            assertEquals(list, list.map { Unit }.asFlow().toNextNumbers().toList())
        }
    }

    @Test()
    fun withHistoryTests() = runBlockingTest {
        assertEquals(listOf(listOf()), producingUnits(0).withHistory().toList())
        assertEquals(listOf(listOf(), listOf(Unit)), producingUnits(1).withHistory().toList())
        assertEquals(listOf(listOf(), listOf(Unit), listOf(Unit, Unit)), producingUnits(2).withHistory().toList())

        assertEquals(listOf(listOf(), listOf(1), listOf(1, 2)), producingUnits(2).toNextNumbers().withHistory().toList())
        assertEquals(listOf(listOf(), listOf(true), listOf(true, false)), producingUnits(2).toToggle().withHistory().toList())

        val flow = flow {
            emit("A")
            delay(100)
            emit(10)
            emit("C")
        }
        assertEquals(listOf(listOf(), listOf("A"), listOf("A", 10), listOf("A", 10, "C")), flow.withHistory().toList())
    }

    @Test()
    fun flowDelayEachTests() = runBlockingTest {
        val emittedNum = AtomicInteger()

        producingUnits(100)
                .delayEach(1000)
                .onEach { emittedNum.incrementAndGet() }
                .launchIn(this)

        assertEquals(0, emittedNum.get())

        // After 1 500ms there should be one element
        delay(1_500)
        assertEquals(1, emittedNum.get())

        // After another 2 000ms there should be two more elements
        delay(2_000)
        assertEquals(3, emittedNum.get())

        // After another 12 000ms there should be twelve more elements
        delay(12_000)
        assertEquals(15, emittedNum.get())
    }

    @Test()
    fun makeTimerTests() = runBlockingTest {
        val mutex = Mutex()
        var ticked = listOf<Int>()
        makeTimer(1000, 10, 20)
                .onEach {
                    mutex.withLock { ticked += it }
                }
                .launchIn(this)

        assertEquals(listOf(10), mutex.withLock { ticked })

        // After 1 500ms there should be one element
        delay(1_500)
        assertEquals(listOf(10, 11), mutex.withLock { ticked })

        // After another 2 000ms there should be two more elements
        delay(2_000)
        assertEquals(listOf(10, 11, 12, 13), mutex.withLock { ticked })

        // After another 12 000ms there should be twelve more elements
        delay(12_000)
        assertEquals((10..20).toList(), mutex.withLock { ticked })
    }

    @Test()
    fun `makeTimer if delayed in between, do not provide old values but only shows the last one`() = runBlockingTest {
        val maxValue = 20
        val res = makeTimer(100, 1, maxValue)
                .onEach {
                    if(it == 1) delay(50) // To make it clearly after timer delay
                    // We don't need to check more often than every 0.5s
                    delay(500)
                }
                .toList()

        assertEquals(listOf(1, 6, 11, 16, 20), res)
    }
}