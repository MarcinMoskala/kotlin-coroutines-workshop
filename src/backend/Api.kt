package backend

import kotlinx.coroutines.runBlocking
import java.lang.Error

data class User(val name: String)

fun main() {
    val database = Database()
    val emailService = EmailService()
    api {
        get("users") {
            "[]"
        }
        post("user") { body ->
            val user = body as? User ?: throw Error("Passed user is not correct")
            print("I just get $user")
            "OK"
        }
        get("user/count") {
            0
        }
    }.start()

    runBlocking {
        println("User count is ${get("user/count")}")
        println("Users are ${get("users")}")
        post("user", User("Marcin"))
    }
}