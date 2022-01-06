package flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

// Produces a flow of Unit
// For instance producingUnits(5) -> [Unit, Unit, Unit, Unit, Unit]
fun producingUnits(num: Int): Flow<Unit> = TODO()

// Adds a delay of time `timeMillis` between elements
fun <T> Flow<T>.delayEach(timeMillis: Long): Flow<T> = TODO()

// Should transform Unit's to toggled boolean value starting from true
// For instance flowOf(Unit, Unit, Unit, Unit).toNextNumbers() -> [true, false, true, false]
fun Flow<Unit>.toToggle(): Flow<Boolean> = TODO()

// Should transform Unit's to next numbers startling from 1
// For instance flowOf(Unit, Unit, Unit, Unit).toNextNumbers() -> [1, 2, 3, 4]
fun Flow<Unit>.toNextNumbers(): Flow<Int> = TODO()

// Produces not only elements, but the whole history till now
// For instance flowOf(1, "A", 'C').withHistory() -> [[], [1], [1, A], [1, A, C]]
fun <T> Flow<T>.withHistory(): Flow<List<T>> = TODO()

// Should create a flow that every `tickEveryMillis` should emit next numbers from `startNum` to `endNum`
fun makeTimer(tickEveryMillis: Long, startNum: Int, endNum: Int): Flow<Int> = TODO()

// Based on two light switches, should decide if the general light should be switched on.
// Should be if one is true and another is false
fun makeLightSwitch(switch1: Flow<Boolean>, switch2: Flow<Boolean>): Flow<Boolean> = TODO()

// Based on two light switches, should decide if the general light should be switched on.
// Should be if one is turned on and another is off
// At the beginning, both switches are off, and each action toggles
fun makeLightSwitchToggle(switch1: Flow<Unit>, switch2: Flow<Unit>): Flow<Boolean> = TODO()

fun polonaisePairing(track1: Flow<Person>, track2: Flow<Person>): Flow<Pair<Person, Person>> = TODO()

data class Person(val name: String)

@Suppress("FunctionName")
class FlowTests {

    @Test()
    fun producingUnitsTests() = runTest {
        assertEquals(listOf(), producingUnits(0).toList())
        assertEquals(listOf(Unit), producingUnits(1).toList())
        assertEquals(listOf(Unit, Unit), producingUnits(2).toList())
        assertEquals(listOf(Unit, Unit, Unit), producingUnits(3).toList())
        for (i in 1..100 step 7) {
            assertEquals(List(i) { Unit }, producingUnits(i).toList())
        }
    }

    @Test()
    fun toToggleTests() = runTest {
        assertEquals(listOf(), producingUnits(0).toToggle().toList())
        assertEquals(listOf(true), producingUnits(1).toToggle().toList())
        assertEquals(listOf(true, false), producingUnits(2).toToggle().toList())
        assertEquals(listOf(true, false, true), producingUnits(3).toToggle().toList())
        assertEquals(listOf(true, false, true, false), producingUnits(4).toToggle().toList())
    }

    @Test()
    fun toNextNumbersTests() = runTest {
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
    fun withHistoryTests() = runTest {
        assertEquals(listOf(listOf()), producingUnits(0).withHistory().toList())
        assertEquals(listOf(listOf(), listOf(Unit)), producingUnits(1).withHistory().toList())
        assertEquals(listOf(listOf(), listOf(Unit), listOf(Unit, Unit)), producingUnits(2).withHistory().toList())

        assertEquals(
            listOf(listOf(), listOf(1), listOf(1, 2)),
            producingUnits(2).toNextNumbers().withHistory().toList()
        )
        assertEquals(
            listOf(listOf(), listOf(true), listOf(true, false)),
            producingUnits(2).toToggle().withHistory().toList()
        )

        val flow = flow {
            emit("A")
            delay(100)
            emit(10)
            emit("C")
        }
        assertEquals(listOf(listOf(), listOf("A"), listOf("A", 10), listOf("A", 10, "C")), flow.withHistory().toList())
    }

    @Test()
    fun flowDelayEachTests() = runTest {
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
    fun makeTimerTests() = runTest {
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
    fun `makeTimer if delayed in between, do not provide old values but only shows the last one`() = runTest {
        val maxValue = 20
        val res = makeTimer(100, 1, maxValue)
            .onEach {
                if (it == 1) delay(50) // To make it clearly after timer delay
                // We don't need to check more often than every 0.5s
                delay(500)
            }
            .toList()

        assertEquals(listOf(1, 6, 11, 16, 20), res)
    }

    @Test()
    fun makeLightSwitchTests() = runTest {
        val switchOne = flow<Boolean> {
            emit(true)
            delay(1000)
            emit(false)
            delay(10)
            emit(true)
            delay(500) // 1500
            emit(false)
        }
        val switchTwo = flow<Boolean> {
            emit(false)
            delay(200)
            emit(true)
            delay(1000) // 1200
            emit(false)
        }

        var lightOn = false
        launch {
            makeLightSwitch(switchOne, switchTwo).collect {
                lightOn = it
            }
        }

        delay(50)
        assertEquals(true, lightOn)
        delay(200) // 250
        assertEquals(false, lightOn)
        delay(800) // 1050
        assertEquals(false, lightOn)
        delay(200) // 1250
        assertEquals(true, lightOn)
        delay(300) // 1550
        assertEquals(false, lightOn)
    }

    @Test()
    fun makeLightSwitchToggleTests() = runTest {
        val switchOne = flow<Unit> {
            emit(Unit)
            delay(1000)
            emit(Unit)
            delay(10)
            emit(Unit)
            delay(500) // 1500
            emit(Unit)
        }
        val switchTwo = flow<Unit> {
            emit(Unit)
            delay(200)
            emit(Unit)
            delay(1000) // 1200
            emit(Unit)
        }

        var lightOn = false
        launch {
            makeLightSwitchToggle(switchOne, switchTwo).collect {
                lightOn = it
            }
        }

        delay(50)
        assertEquals(true, lightOn)
        delay(200) // 250
        assertEquals(false, lightOn)
        delay(800) // 1050
        assertEquals(false, lightOn)
        delay(200) // 1250
        assertEquals(true, lightOn)
        delay(300) // 1550
        assertEquals(false, lightOn)
    }

    @Test()
    fun polonaisePairingTests() = runTest {
        val track1 = flow<Person> {
            emit(Person("A"))
            emit(Person("B"))
            delay(1000)
            emit(Person("C"))
            emit(Person("D"))
        }
        val track2 = flow<Person> {
            emit(Person("1"))
            delay(600)
            emit(Person("2"))
            delay(1000)
            emit(Person("3"))
        }

        val res = polonaisePairing(track1, track2).toList()
        val expected = listOf("A" to "1", "B" to "2", "C" to "3").map { Person(it.first) to Person(it.second) }
        assertEquals(expected, res)

        var lastPair: Pair<Person, Person>? = null
        launch {
            polonaisePairing(track1, track2).collect { lastPair = it }
        }

        assertEquals(Person("A") to Person("1"), lastPair)
        delay(200) // 200
        assertEquals(Person("A") to Person("1"), lastPair)

        delay(500) // 700
        assertEquals(Person("B") to Person("2"), lastPair)
        delay(500) // 1200
        assertEquals(Person("B") to Person("2"), lastPair)

        delay(500) // 1700
        assertEquals(Person("C") to Person("3"), lastPair)
    }
}
