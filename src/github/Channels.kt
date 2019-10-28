package github

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach

fun main() = runBlocking(Dispatchers.Default) {
    val username = "<Your Github username>"
    // Link https://github.com/settings/tokens/new
    // No permissions needed
    val token = "<Github token>"
    val service: GitHubService = createGitHubService(username, token)

    val usersChannel = Channel<List<User>>()
    launch {
        getContributions(service, usersChannel)
    }
    for (contributions in usersChannel) {
        println(contributions)
    }

//    val aggregatedUsersChannel = Channel<List<User>>()
//    launch {
//        getAggregatedContributionsChannel(service, aggregatedUsersChannel)
//    }
//    for (aggregatedContributions in aggregatedUsersChannel) {
//        println(aggregatedContributions.sortedByDescending { it.contributions })
//    }
}

suspend fun getContributions(service: GitHubService, usersChannel: SendChannel<List<User>>) {
    TODO()
}

suspend fun getAggregatedContributionsChannel(service: GitHubService, usersChannel: SendChannel<List<User>>) {
    TODO()
}