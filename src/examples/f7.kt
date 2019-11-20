package examples

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlin.system.measureTimeMillis

suspend fun main() {
    measureTimeMillis {
        ('A'..'C').asFlow()
                .flatMapMerge { flowFrom(it) }
                .collect { print(it) } // 0_B 0_A 0_C 1_B 1_C 1_A 2_C 2_B 2_A
    }.let(::print) // 3117
}