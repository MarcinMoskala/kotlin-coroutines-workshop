import kotlinx.coroutines.*
import kotlin.random.Random

class User(val name: String)

class NetworkService {
    suspend fun getUser(): User {
        delay(2)
        return User(Random.nextLong().toString())
    }
}

class UserDownloader(private val api: NetworkService) {
    private val users = mutableListOf<User>()

    fun all(): List<User> = users

    suspend fun downloadNext(num: Int) = coroutineScope {
        repeat(num) {
            val newUser = api.getUser()
            users.add(newUser)
        }
    }
}

fun main() = runBlocking(Dispatchers.Default) {
    val downloader = UserDownloader(NetworkService())
    coroutineScope {
        repeat(1000) {
            launch {
                downloader.downloadNext(1000)
            }
        }
    }
    print(downloader.all().size)
}
