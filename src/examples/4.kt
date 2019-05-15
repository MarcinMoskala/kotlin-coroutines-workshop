package examples

import kotlinx.coroutines.*

fun CoroutineScope.log(msg: String) = println("[${coroutineContext[CoroutineName]?.name}] $msg")

fun main() = runBlocking(CoroutineName("main")) {
    log("Started main coroutine")
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        "KOKO"
    }
    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        6
    }
    log("The answer for v1 = ${v1.await()}")
}
