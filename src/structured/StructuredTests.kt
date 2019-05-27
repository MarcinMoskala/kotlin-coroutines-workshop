package structured

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.Test
import kotlin.test.assertEquals

@ObsoleteCoroutinesApi
@Suppress("FunctionName")
class StructuredTests {

    class FakeFactoryControl(
            val machineProducer: ()->Machine
    ) : FactoryControl {
        var createdMachines = listOf<Machine>()
        var codesStored = listOf<String>()
        private var finished = false

        override fun makeMachine(): Machine {
            require(!finished)
            return machineProducer()
                    .also { createdMachines = createdMachines + it }
        }

        override fun storeCode(code: String) {
            require(!finished)
            codesStored = codesStored + code
        }

        fun finish() {
            finished = true
        }
    }

    class PerfectMachine : Machine {
        var timesUsed = 0
        private var finished = false

        override fun produce(): String {
            require(!finished)
            return (timesUsed++).toString()
        }
        fun finish() {
            finished = true
        }
    }

    class FailingMachine : Machine {
        override fun produce(): String = throw ProductionError()
    }

    @Test(timeout = 500)
    fun `PerfectMachine produces next numbers`() {
        val machine = PerfectMachine()
        assertEquals("0", machine.produce())
        assertEquals("1", machine.produce())
        assertEquals("2", machine.produce())
        assertEquals("3", machine.produce())
        assertEquals("4", machine.produce())
    }

    @Test(timeout = 500)
    fun `FakeFactoryControl produces machines using producer`() {
        val perfectFactoryControl = FakeFactoryControl(machineProducer = ::PerfectMachine)
        val machine1 = perfectFactoryControl.makeMachine()
        assertEquals("0", machine1.produce())
        assertEquals("1", machine1.produce())
        assertEquals("2", machine1.produce())
        val machine2 = perfectFactoryControl.makeMachine()
        assertEquals("0", machine2.produce())
        assertEquals("1", machine2.produce())
        assertEquals("2", machine2.produce())

        val failingFactoryControl = FakeFactoryControl(machineProducer = ::FailingMachine)
        val machine3 = failingFactoryControl.makeMachine()
        assertThrows<ProductionError> { machine3.produce() }
    }

    @Test
    fun `Function creates a new machine every 800ms up to 5 and no more if they are all perfect`() {
        val control = FakeFactoryControl(machineProducer = ::PerfectMachine)
        val context = TestCoroutineContext()
        runBlocking(context) {
            setupFactory(control)
            for(i in 0..5) {
                context.triggerActions()
                assertEquals(i, control.createdMachines.size)
                context.advanceTimeBy(800)
            }
            for(i in 0..10) {
                context.triggerActions()
                assertEquals(5, control.createdMachines.size)
                context.advanceTimeBy(800)
            }
        }
    }

    @Test
    fun `Function creates a new machine every 800ms every time if all machines are failing`() {
        val control = FakeFactoryControl(machineProducer = ::FailingMachine)
        val context = TestCoroutineContext()
        runBlocking(context) {
            setupFactory(control)
            for(i in 0..100) {
                context.triggerActions()
                assertEquals(i, control.createdMachines.size)
                context.advanceTimeBy(800)
            }
        }
    }

    @Test
    fun `Function creates a new machine after 800ms if less then 5`() {
        var correctMachines = 0
        var nextIsCorrect = false
        val control = FakeFactoryControl(machineProducer = {
            val next = if(nextIsCorrect) {
                correctMachines ++
                PerfectMachine()
            } else {
                FailingMachine()
            }
            nextIsCorrect = !nextIsCorrect
            next
        })
        val context = TestCoroutineContext()
        runBlocking(context) {
            setupFactory(control)
            context.advanceTimeBy(20_000)
            context.triggerActions()
            assertEquals(5, control.createdMachines.filterIsInstance<PerfectMachine>().size)

            // Is not producing any new
            val producedPre = control.createdMachines.size
            context.advanceTimeBy(2_000)
            context.triggerActions()
            val producedPost = control.createdMachines.size
            assertEquals(producedPre, producedPost, "It should not produce any new machines when there are already 5 perfect")
        }
    }

    @Test
    fun `The first code should be created after time to create machine and time to produce code`() {
        val perfectMachine = PerfectMachine()
        val control = FakeFactoryControl(machineProducer = { perfectMachine })
        val context = TestCoroutineContext()
        runBlocking(context) {
            setupFactory(control)
            context.advanceTimeTo(800 + 1000)
            context.triggerActions()
            assertEquals(1, perfectMachine.timesUsed)
        }
    }

    /*
         800     1600    1800    2400   2600  2800  3200 3400 3600 3800
          m1 ----------> CODE --------------> CODE --------------> CODE
                  m1 -----------------> CODE ---------------> CODE -----
                                  m3 ------------------> CODE ----------
                                                     m4 ----------------
   Codes                    1              2     3          4    5    6
     */
    @Test
    fun `Every machine produces code every second`() {
        val perfectMachine = PerfectMachine()
        val control = FakeFactoryControl(machineProducer = { perfectMachine })
        val context = TestCoroutineContext()
        fun checkAt(timeMillis: Long, codes: Int) {
            context.advanceTimeTo(timeMillis)
            context.triggerActions()
            assertEquals(codes, perfectMachine.timesUsed)
        }
        runBlocking(context) {
            setupFactory(control)
            checkAt(800, 0)
            checkAt(1600, 0)
            checkAt(1800, 1)
            checkAt(2400, 1)
            checkAt(2600, 2)
            checkAt(2800, 3)
            checkAt(3200, 3)
            checkAt(3400, 4)
            checkAt(3600, 5)
            checkAt(3800, 6)
        }
    }

    @Test
    fun `Created codes are stored no later then 100ms after created`() {
        val perfectMachine = PerfectMachine()
        val control = FakeFactoryControl(machineProducer = { perfectMachine })
        val context = TestCoroutineContext()
        fun checkAt(timeMillis: Long, codes: Int) {
            context.advanceTimeTo(timeMillis)
            context.triggerActions()
            assertEquals(codes, control.codesStored.size)
        }
        runBlocking(context) {
            setupFactory(control)
            checkAt(900, 0)
            checkAt(1700, 0)
            checkAt(1900, 1)
            checkAt(2500, 1)
            checkAt(2700, 2)
            checkAt(2900, 3)
            checkAt(3300, 3)
            checkAt(3500, 4)
            checkAt(3700, 5)
            checkAt(3900, 6)
        }
    }

    @Test
    fun `When there are 20 codes stored, process ends`() {
        val perfectMachine = PerfectMachine()
        val control = FakeFactoryControl(machineProducer = { perfectMachine })
        val context = TestCoroutineContext()
        runBlocking(context) {
            setupFactory(control)
            context.advanceTimeTo(6800) // Time when 20'th code is produced
            context.triggerActions()
            assertEquals(20, control.codesStored.size)
            perfectMachine.finish() // To not let it be used anymore
            control.finish() // To not let it be used anymore
            context.advanceTimeBy(1_000)
            context.triggerActions()
            assertEquals(20, control.codesStored.size)
        }
    }

    private inline fun <reified T> assertThrows(body: ()->Unit) {
        val error = try {
            body()
            Any()
        } catch (t: Throwable) {
            t
        }
        assertEquals(T::class, error::class)
    }
}