package github

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("FunctionName")
internal class AggregatedTest {
    private val user1 = User("AAA", 123)
    private val user2 = User("BBB", 1)
    private val repo1 = Repo(10, "R1")
    private val repo2 = Repo(11, "R2")

    @Test
    fun `Function works without errors`() = runBlockingTest {
        getAggregatedContributions(EmptyService)
    }

    @Test
    fun `When no repositories or no users, returns empty lists`() = runBlockingTest {
        val list1 = getAggregatedContributions(EmptyService)
        assertEquals(emptyList(), list1)
        val list2 = getAggregatedContributions(FakeStaticSyncService(listOf(), listOf(user1)))
        assertEquals(emptyList(), list2)
        val list3 = getAggregatedContributions(FakeStaticSyncService(listOf(repo1), listOf()))
        assertEquals(emptyList(), list3)
        val list4 = getAggregatedContributions(FakeStaticSyncService(listOf(repo1), listOf()))
        assertEquals(emptyList(), list4)
    }

    @Test
    fun `Lists all unique users`() = runBlockingTest {
        val list = getAggregatedContributions(FakeStaticSyncService(listOf(repo1), listOf(user1, user2)))
        assertEquals(listOf(user1, user2), list)
    }

    @Test
    fun `Accumulates contributions of a single user`() = runBlockingTest {
        val list = getAggregatedContributions(FakeStaticSyncService(listOf(repo1), listOf(user1, user1)))
        assertEquals(listOf(User(user1.login, user1.contributions * 2)), list)
    }

    @Test
    fun `Accumulates contributions of multiple users user`() = runBlockingTest {
        val list = getAggregatedContributions(FakeStaticSyncService(listOf(repo1, repo2), listOf(user1, user1, user2)))
        val expected = listOf(
                User(user1.login, user1.contributions * 4),
                User(user2.login, user2.contributions * 2)
        ).sortedBy { it.contributions }
        assertEquals(expected, list.sortedBy { it.contributions })
    }

    @Test
    fun `Prepared for multithreading`() = runBlockingTest {
        val service = FakeDelayedAsyncService(List(100) { repo1 }, List(100) { user1 })
        var res = getAggregatedContributions(service)
        delay(500)
        assertEquals(listOf(User(user1.login, user1.contributions * 100 * 100)), res)
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