package backend

fun api(config: ApiConfig.()->Unit) = ApiConfig().also(config)

var publicHandles = mapOf<Endpoint, suspend (body: Any?)->Any>()

data class Endpoint(val method: String, val path: String)

class ApiConfig {
    var handles = mapOf<Endpoint, suspend (body: Any?)->Any>()

    fun get(path: String, handle: suspend (body: Any?)->Any) {
        addHandle("get", path, handle)
    }

    fun post(path: String, handle: suspend (body: Any?)->Any) {
        addHandle("post", path, handle)
    }

    fun start() {
        publicHandles += handles
    }

    private fun addHandle(method: String, path: String, handle: suspend (body: Any?)->Any) {
        handles += Endpoint(method, path) to handle
    }
}

suspend fun get(path: String, body: Any? = null) = respond("get", path, body)

suspend fun post(path: String, body: Any? = null) = respond("post", path, body)

suspend fun respond(method: String, path: String, body: Any?): Any? {
    val handle = publicHandles[Endpoint(method, path)] ?: throw NoSuchMethodError()
    return try {
        handle(body)
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}