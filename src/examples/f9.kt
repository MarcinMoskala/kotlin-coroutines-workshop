package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlin.system.measureTimeMillis

suspend fun main() {
    measureTimeMillis {
        ('A'..'C').asFlow()
                .onEach { delay(1500) }
                .flatMapLatest { flowFrom(it) }
                .collect { print(it) } // A_0 C_0 C_1 C_2
    }.let(::print) // 1058
}