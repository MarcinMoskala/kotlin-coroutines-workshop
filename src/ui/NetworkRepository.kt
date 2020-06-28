package ui

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class DiscUserRepository(private val discReader: DiscReader) : UserRepository {
    override suspend fun getUser(): UserData = UserData(discReader.read("userName"))
}

interface DiscReader {
    fun read(key: String): String
}

@Suppress("FunctionName")
class DiscUserRepositoryTests {

    @Test
    fun `should read data from disc using DiscReader`() = runBlocking {
        val name = "Marcin"
        val repo = DiscUserRepository(ImmediateDiscReader(mapOf("userName" to name)))
        val res = repo.getUser()
        assertEquals(name, res.name)
    }

    class ImmediateDiscReader(val map: Map<String, String>) : DiscReader {
        override fun read(key: String): String = map[key] ?: error("Element not found")
    }

    @Test
    fun `should be prepared for many reads at the same time`() = runBlocking<Unit> {
        val name = "Marcin"
        val repo = DiscUserRepository(OneSecDiscReader(mapOf("userName" to name)))
        val time = measureTimeMillis {
            coroutineScope {
                repeat(10) {
                    launch {
                        repo.getUser()
                    }
                }
            }
        }
        assert(time < 2000) { "Should take less than 2000, took $time" }
    }

    @Test
    fun `should be prepared for 200 reads at the same time`() = runBlocking<Unit> {
        val name = "Marcin"
        val repo = DiscUserRepository(OneSecDiscReader(mapOf("userName" to name)))
        val time = measureTimeMillis {
            coroutineScope {
                repeat(200) {
                    launch {
                        repo.getUser()
                    }
                }
            }
        }
        assert(time < 2000) { "Should take less than 2000, took $time" }
    }

    class OneSecDiscReader(val map: Map<String, String>) : DiscReader {
        override fun read(key: String): String {
            Thread.sleep(1000)
            return map[key] ?: error("Element not found")
        }
    }
}