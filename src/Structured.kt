import kotlinx.coroutines.runBlocking
import java.util.Random

// We have a worker who makes machines every 800ms as long as there is less than 5 of them
// Every machine produces a code using `produce` function every second. It saves this code to shared space. In case of an error, it ends working.
// We have a single manager that once a 2 seconds takes all produced codes and prints them all. After 5 times it ends all jobs (including machines and worker).

fun main() = runBlocking<Unit> {
    TODO()
}

class RandomError() : Throwable()

private val letters = ('a'..'z') + ('0'..'9')
private val random = Random()

private fun produce(): String = when (random.nextInt(8)) {
    0 -> throw RandomError()
    else -> (1..5).map { letters[random.nextInt(letters.size)] }.joinToString(separator = "")
}