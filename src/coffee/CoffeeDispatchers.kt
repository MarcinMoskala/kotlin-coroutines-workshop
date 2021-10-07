package coffee.dispatchers

import kotlinx.coroutines.*
import java.util.concurrent.Executors

val dispatcher = Executors.newSingleThreadExecutor()
    .asCoroutineDispatcher()
//val dispatcher = Dispatchers.Default
//val dispatcher = Dispatchers.IO
//val dispatcher = Executors.newFixedThreadPool(100)
//    .asCoroutineDispatcher()

suspend fun longOperation() {
    val size = 350 // ~0.1 second on my MacBook
    val list = List(size) { it }
    val listOfLists = List(size) { list }
    val listOfListsOfLists = List(size) { listOfLists }
    listOfListsOfLists.hashCode()
//    Thread.sleep(1000)
//    delay(1000)
}

suspend fun serveOrders(orders: List<Order>) =
    coroutineScope {
        for (order in orders) {
            launch(dispatcher) {
                val coffee = makeCoffee(order)
                println("Coffee $coffee for ${order.customer} made by ${Thread.currentThread().name}")
            }
        }
    }

suspend fun main() = withContext(dispatcher) {
    val orders = List(100) { Order("Customer$it", CoffeeType.values().random()) }
    val startTime = System.currentTimeMillis()

    serveOrders(orders)

    val endTime = System.currentTimeMillis()
    println("Done, took ${endTime - startTime}")
}

private suspend fun makeCoffee(order: Order): Coffee {
    val groundCoffee = groundCoffee()
    val espresso = makeEspresso(groundCoffee)
    val coffee = when (order.type) {
        CoffeeType.ESPRESSO -> espresso
        CoffeeType.LATE -> {
            val milk = brewMilk()
            Latte(milk, espresso)
        }
    }
    return coffee
}

suspend fun groundCoffee(): GroundCoffee {
    longOperation()
    return GroundCoffee()
}

suspend fun brewMilk(): Milk {
    longOperation()
    return Milk()
}


suspend fun makeEspresso(ground: GroundCoffee): Espresso {
    longOperation()
    return Espresso(ground)
}

data class Order(val customer: String, val type: CoffeeType)
enum class CoffeeType { ESPRESSO, LATE }
class Milk
class GroundCoffee

sealed class Coffee
class Espresso(ground: GroundCoffee) : Coffee() {
    override fun toString(): String = "Espresso"
}

class Latte(milk: Milk, espresso: Espresso) : Coffee() {
    override fun toString(): String = "Latte"
}