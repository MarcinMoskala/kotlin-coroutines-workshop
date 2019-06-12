package actors

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.util.*

// We have a worker who makes machines every 800ms as long as there is less than 5 of them.
//   He won't produce more than 1000 machines. Please, use `repeat(1000)` instead of `while(true)`
// Every machine produces a code using `structured.produce` function every second. It saves this code to shared space.
//   In case of an error, it stops working.
//   Machine won't produce more than 1000 codes. Please, use `repeat(1000)` instead of `while(true)`
// We have a single manager that takes codes one after another and stores them using `control.storeCode`.
//   Note that is it time consuming operation.
//   He is the only one who can do that.
//   In case of no codes, he sleeps for 100ms
//   He ends everything when there are 20 codes stored.
//   He won't do it more than 1000 times. Please, use `repeat(1000)` instead of `while(true)`

fun main() = runBlocking<Unit> {
    setupFactory(StandardFactoryControl())
}

fun CoroutineScope.setupFactory(control: FactoryControl) {
    val managerChannel = managerActor(control)
    val workerChannel = workerActor(control, managerChannel)

    launch {
        repeat(1000) {
            delay(800)
            workerChannel.send(WakeUp)
        }
        managerChannel.close()
        workerChannel.close()
    }
}

fun CoroutineScope.managerActor(control: FactoryControl) = actor<ManagerMessages> {
    var codesStored = 0
    for (msg in channel) {
        when (msg) {
            is NewCode -> {
                control.storeCode(msg.code)
                codesStored++
                if(codesStored >= 20) {
                    coroutineContext.cancel()
                }
            }
        }
    }
}

fun CoroutineScope.workerActor(control: FactoryControl, managerChannel: SendChannel<ManagerMessages>) = actor<WorkerMessages> {
    var machinesNum = 0
    for (msg in channel) {
        when (msg) {
            WakeUp -> if(machinesNum < 5) {
                createMachine(channel, managerChannel, control)
                machinesNum++
            }
            MachineBroken -> machinesNum--
        }
    }
}

fun CoroutineScope.createMachine(workerChannel: SendChannel<WorkerMessages>, managerChannel: SendChannel<ManagerMessages>, control: FactoryControl) {
    val machine = control.makeMachine()
    launch {
        try {
            repeat(1000) {
                delay(1000)
                val code = machine.produce()
                managerChannel.send(NewCode(code))
            }
        } catch (productionError: ProductionError) {
            workerChannel.send(MachineBroken)
        }
    }
}

interface WorkerMessages
object WakeUp: WorkerMessages
object MachineBroken: WorkerMessages

interface ManagerMessages
class NewCode(val code: String): ManagerMessages

interface FactoryControl {
    fun makeMachine(): Machine
    fun storeCode(code: String)
}

class StandardFactoryControl : FactoryControl {
    private var broken = false
    private var waiting = false
    private var codes = listOf<String>()

    override fun makeMachine(): Machine = StandardMachine()
            .also { println("Newly created machine") }

    override fun storeCode(code: String) {
        if (waiting || broken) {
            println("Factory control is broken due to 2 attempts to store code at the same time")
            broken = true
            throw BrokenMachineError()
        }
        waiting = true
        Thread.sleep(500)
        waiting = false
        codes = codes + code
        println("Newly stored code is $code")
    }
}

interface Machine {
    @Throws(ProductionError::class)
    fun produce(): String
}

class StandardMachine : Machine {
    private var broken = false

    override fun produce(): String = when {
        broken -> throw BrokenMachineError()
        random.nextInt(8) == 0 -> {
            broken = true
            println("Machine broken")
            throw ProductionError()
        }
        else -> (1..5).map { letters[random.nextInt(letters.size)] }.joinToString(separator = "")
                .also { println("Newly produced code $it") }
    }

    companion object {
        private val letters = ('a'..'z') + ('0'..'9')
        private val random = Random()
    }
}

class ProductionError() : Throwable()
class BrokenMachineError() : Throwable()