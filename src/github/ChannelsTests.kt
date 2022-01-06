package github

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("FunctionName")
internal class ChannelsTest {
    private val user1 = User("AAA", 123)
    private val user2 = User("BBB", 1)
    private val user1Doubled = User("AAA", 246)
    private val user2Doubled = User("BBB", 2)
    private val repo1 = Repo(10, "R1")
    private val repo2 = Repo(11, "R2")

    private val service = FakeStaticSyncService(listOf(repo1, repo2), listOf(user1, user2))

    @Test
    fun getContributionsTest() = runTest {
        val channel = getContributionsChannel(service)
        assertEquals(listOf(listOf(user1, user2), listOf(user1, user2)), channel.toList())
    }

    @Test
    fun getAggregatedContributionsChannelTest() = runTest {
        val channel = getAggregatedContributionsChannel(service)
        assertEquals(listOf(listOf(user1, user2), listOf(user1Doubled, user2Doubled)), channel.toList())
    }

    class FakeStaticSyncService(private val repos: List<Repo>, private val users: List<User>) : GitHubService {
        override suspend fun getOrgRepos() = repos

        override suspend fun getRepoContributors(repo: String) = users
    }

    class FakeDelayedAsyncService(private val repos: List<Repo>, private val users: List<User>) : GitHubService {

        override suspend fun getOrgRepos(): List<Repo> {
            delay(DELAY_TIME_MS)
            return repos
        }

        override suspend fun getRepoContributors(repo: String): List<User> {
            delay(DELAY_TIME_MS)
            return users
        }

        companion object {
            val DELAY_TIME_MS = 30L
        }
    }

    object EmptyService : GitHubService {
        override suspend fun getOrgRepos() = emptyList<Repo>()

        override suspend fun getRepoContributors(repo: String) = emptyList<User>()
    }
}
