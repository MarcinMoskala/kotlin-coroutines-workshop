package coffee

import kotlinx.coroutines.coroutineScope

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

suspend fun main() = coroutineScope<Unit> {
    val orders = List(100) { Order("Customer$it", CoffeeType.values().random()) }
    val startTime = System.currentTimeMillis()
    var ordersLeft = orders.size

    serveOrders(orders) { coffee, customer, barista ->
        println("Coffee $coffee for $customer made by $barista")
        ordersLeft--
        if (ordersLeft == 0) {
            val endTime = System.currentTimeMillis()
            println("Done, took ${endTime - startTime}")
        }
    }
}

// TODO - speed it up
suspend fun serveOrders(orders: List<Order>, serveCoffee: (coffee: Coffee, customer: String, barista: String) -> Unit) {
    for (order in orders) {
        val groundCoffee = groundCoffee()
        val espresso = makeEspresso(groundCoffee)
        val coffee = when (order.type) {
            CoffeeType.ESPRESSO -> espresso
            CoffeeType.LATE -> {
                val milk = brewMilk()
                Latte(milk, espresso)
            }
        }
        serveCoffee(coffee, order.customer, "Bob")
    }
}

fun groundCoffee(): GroundCoffee {
    longOperation()
    return GroundCoffee()
}

fun brewMilk(): Milk {
    longOperation()
    return Milk()
}


fun makeEspresso(ground: GroundCoffee): Espresso {
    longOperation()
    return Espresso(ground)
}

fun longOperation() {
//    val size = 820 // ~1 second on my MacBook
    val size = 350 // ~0.1 second on my MacBook
    val list = List(size) { it }
    val listOfLists = List(size) { list }
    val listOfListsOfLists = List(size) { listOfLists }
    listOfListsOfLists.hashCode()
//    Thread.sleep(1000)
}