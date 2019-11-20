package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

suspend fun main() {
    measureTimeMillis {
        ('A'..'C').asFlow()
                .flatMapConcat { flowFrom(it) }
                .collect { print(it) } // A_0 A_1 A_2 B_0 B_1 B_2 C_0 C_1 C_2
    }.let(::print) // 9060
}

fun flowFrom(elem: Any) = flowOf(0, 1, 2)
        .onEach { delay(1000) }
        .map { "${it}_${elem} " }