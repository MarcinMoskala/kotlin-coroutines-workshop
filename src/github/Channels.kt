package github

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val username = "marcinmoskala"
private const val token = "f79e01cd92d606a8369c4523d22a384ef4f16b71"
private val service: GitHubService = createGitHubService(username, token)


fun main(): Unit = runBlocking(Dispatchers.Default) {
    val usersChannel = getContributionsChannel(service)
    for (contributions in usersChannel) {
        println(contributions)
    }

    val aggregatedUsersChannel = getAggregatedContributionsChannel(service)
    for (aggregatedContributions in aggregatedUsersChannel) {
        println(aggregatedContributions.sortedByDescending { it.contributions })
    }
}

suspend fun CoroutineScope.getContributionsChannel(service: GitHubService): ReceiveChannel<List<User>> = produce {
    // TODO
}

suspend fun CoroutineScope.getAggregatedContributionsChannel(service: GitHubService): ReceiveChannel<List<User>> = produce {
    // TODO
}
