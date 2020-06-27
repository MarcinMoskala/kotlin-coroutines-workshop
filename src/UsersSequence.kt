package sequence

import org.junit.Test
import kotlin.test.assertEquals

data class User(val name: String)

interface UserRepository {
    fun takePage(num: Int): List<User>
}

fun makeUsersSequence(repository: UserRepository): Sequence<User> = TODO()

@Suppress("FunctionName")
internal class UsersSequenceTests {

    @Test
    fun test() {
        val size = 10_000
        val pageSize = 10
        val repo = FakeUserRepository(size, pageSize)
        val s = makeUsersSequence(repo)
        assertEquals(size, s.count())
        assertEquals(size / pageSize + 1, repo.timesUsed)
    }

    class FakeUserRepository(val size: Int, val pageSize: Int) : UserRepository {
        val users = List(size) { User("User$it") }
        var timesUsed = 0

        override fun takePage(num: Int): List<User> = users.dropLast(pageSize * num).take(pageSize).also { timesUsed++ }
    }
}