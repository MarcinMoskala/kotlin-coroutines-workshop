import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    val details = getUserDetails()
    val tweets = async { getTweets() }
    print("User: $details")
    print("Tweets: ${tweets.await()}")
}

suspend fun CoroutineScope.getUserDetails(): String {
    val one = async { getUserName() }
    val two = async { getFollowersNumber() }
    return "The answer is ${one.await() + two.await()}"
}

class Details(val name: String, val followers: Int)
class Tweet(val text: String)

fun getFollowersNumber(): Int = error("Service exception")

suspend fun getUserName(): String {
    delay(500)
    return "marcinmoskala"
}

suspend fun getTweets(): List<Tweet> {
    return listOf(Tweet("Hello, world"))
}