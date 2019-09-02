package request

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

/*
   TODO: This function should return the best student on the [semester].
 */
suspend fun getBestStudent(semester: String, repo: StudentsRepository): Student = TODO()

data class Student(val id: Int, val result: Double, val semester: String)

interface StudentsRepository {
    suspend fun getStudentIds(semester: String): List<Int>
    suspend fun getStudent(id: Int): Student
}

class ScopeTest {

    @Test
    fun `Function does return the best student in the semester`() = runBlocking {
        val semester = "19L"
        val best = Student(2, 95.0, semester)
        val repo = ImmediateFakeStudentRepo(listOf(
                Student(1, 90.0, semester),
                best,
                Student(3, 50.0, semester)
        ))
        val chosen = getBestStudent(semester, repo)
        assertEquals(best, chosen)
    }

    @Test
    fun `Requests do not wait for each other`() = runBlocking {
        val repo = WaitingFakeStudentRepo()
        assertTimeAround(1200) {
            val chosen = getBestStudent("AAA", repo)
        }
    }

    @Test
    fun `Cancellation works fine`() = runBlocking {
        val repo = WaitingFakeStudentRepo()
        val job = launch {
            val chosen = getBestStudent("AAA", repo)
        }
        delay(300)
        job.cancel()
        delay(1000)
        assertEquals(0, repo.returnedStudents)
    }
}

class ImmediateFakeStudentRepo(
        private val students: List<Student>
) : StudentsRepository {

    override suspend fun getStudentIds(semester: String): List<Int> =
            students.filter { it.semester == semester }
                    .map { it.id }

    override suspend fun getStudent(id: Int): Student =
            students.first { it.id == id }
}

inline fun assertTimeAround(expectedTime: Int, upperMargin: Int = 100, body: () -> Unit) {
    val actualTime = measureTimeMillis(body)
    assert(actualTime in expectedTime..(expectedTime + upperMargin)) {
        "Operation should take around $expectedTime, but it took $actualTime"
    }
}

class WaitingFakeStudentRepo : StudentsRepository {
    var returnedStudents = 0

    override suspend fun getStudentIds(semester: String): List<Int> {
        delay(200)
        return (1..5).toList()
    }

    override suspend fun getStudent(id: Int): Student {
        delay(1000)
        returnedStudents++
        return Student(12, 12.0, "AAA")
    }
}