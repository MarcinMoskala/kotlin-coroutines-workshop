package examples.t3

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals

class ShowUserUseCase(
    private val repo: UserDataRepository,
    private val view: UserDataView,
    private val ioDispatcher: CoroutineContext =
        Dispatchers.IO
) {

    suspend fun showUserData() = withContext(ioDispatcher) {
        val name = async { repo.getName() }
        val friends = async { repo.getFriends() }
        val profile = async { repo.getProfile() }
        val user = User(
            name = name.await(),
            friends = friends.await(),
            profile = profile.await()
        )
        view.show(user)
    }
}

class ShowUserDataTest {

    @Test
    fun `should load data concurrently`() = runTest {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()
        val useCase = ShowUserUseCase(
            repo,
            view,
            EmptyCoroutineContext
        )

        // when
        useCase.showUserData()

        // then
        assertEquals(1000, currentTime)
    }

    @Test
    fun `should show user data on view`() = runTest {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()
        val useCase = ShowUserUseCase(
            repo,
            view,
            EmptyCoroutineContext
        )

        // when
        useCase.showUserData()

        // then
        val expectedUser = User(
            name = "Ben",
            friends = listOf(Friend("some-friend-id-1")),
            profile = Profile("Example description")
        )
        assertEquals(listOf(expectedUser), view.showed)
    }

    class FakeUserDataRepository : UserDataRepository {
        override suspend fun getName(): String {
            delay(1000)
            return "Ben"
        }

        override suspend fun getFriends(): List<Friend> {
            delay(1000)
            return listOf(Friend("some-friend-id-1"))
        }

        override suspend fun getProfile(): Profile {
            delay(1000)
            return Profile("Example description")
        }
    }

    class FakeUserDataView : UserDataView {
        var showed = listOf<User>()

        override fun show(user: User) {
            showed = showed + user
        }
    }
}

interface UserDataRepository {
    suspend fun getName(): String
    suspend fun getFriends(): List<Friend>
    suspend fun getProfile(): Profile
}

interface UserDataView {
    fun show(user: User)
}

data class User(
    val name: String,
    val friends: List<Friend>,
    val profile: Profile
)

data class Friend(val id: String)
data class Profile(val description: String)