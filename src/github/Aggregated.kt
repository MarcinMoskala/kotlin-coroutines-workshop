package github

import kotlinx.coroutines.runBlocking

fun main() = runBlocking() {
    val username = "<Your Github username>"
    // Link https://github.com/settings/tokens/new
    // No permissions needed
    val token = "<Github token>"
    val service: GitHubService = createGitHubService(username, token)

    val users = getAggregatedContributions(service)
    val sortedUsers = users.sortedByDescending { it.contributions }
    println("Aggregated contributions:")
    for ((index, user) in sortedUsers.withIndex()) {
        println("$index: ${user.login} with ${user.contributions} contributions")
    }
}

suspend fun getAggregatedContributions(service: GitHubService): List<User> = TODO()
