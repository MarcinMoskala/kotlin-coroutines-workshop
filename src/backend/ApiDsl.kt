package backend

import kotlinx.coroutines.CoroutineScope

fun api(config: ApiConfig.()->Unit) = ApiConfig().also(config)

fun cleanupApi() {
    publicHandles = mapOf()
}

private var publicHandles = mapOf<Endpoint, suspend CoroutineScope.(body: Any?)->Any>()

data class Endpoint(val method: String, val path: String)

class ApiConfig {
    var handles = mapOf<Endpoint, suspend CoroutineScope.(body: Any?)->Any>()

    fun get(path: String, handle: suspend CoroutineScope.(body: Any?)->Any) {
        addHandle("get", path, handle)
    }

    fun post(path: String, handle: suspend CoroutineScope.(body: Any?)->Any) {
        addHandle("post", path, handle)
    }

    fun start() {
        publicHandles = publicHandles + handles
    }

    private fun addHandle(method: String, path: String, handle: suspend CoroutineScope.(body: Any?)->Any) {
        handles = handles + (Endpoint(method, path) to handle)
    }
}

@Suppress("SuspendFunctionOnCoroutineScope")
suspend fun CoroutineScope.get(path: String, body: Any? = null) = this.respond("get", path, body)

@Suppress("SuspendFunctionOnCoroutineScope")
suspend fun CoroutineScope.post(path: String, body: Any? = null) = this.respond("post", path, body)

suspend fun CoroutineScope.respond(method: String, path: String, body: Any?): Any? {
    val handle = publicHandles[Endpoint(method, path)] ?: throw NoSuchMethodError()
    return try {
        handle(this, body)
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}