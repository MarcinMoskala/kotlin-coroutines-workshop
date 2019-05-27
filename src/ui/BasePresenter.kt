package ui

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals

val UI = newSingleThreadContext("UIThread") // Normally it will be Dispatchers.Main

// TODO: Edit only this class
abstract class BasePresenter(
        private val onError: (Throwable) -> Unit = {}
) {

    fun onDestroy() {}
}

@Suppress("FunctionName")
class BasePresenterTests {

    class FakePresenter(
            private val jobInterceptor: (() -> Unit)? = null,
            onError: (Throwable) -> Unit = {}
    ) : BasePresenter(onError) {

        var cancelledJobs = 0

        fun onCreate() {
            launch {
                try {
                    delay(100)
                    jobInterceptor?.invoke()
                    delay(2000)
                } finally {
                    cancelledJobs += 1
                }
            }
            launch {
                try {
                    delay(100)
                    jobInterceptor?.invoke()
                    delay(2000)
                } finally {
                    cancelledJobs += 1
                }
            }
        }
    }

    @Test
    fun `onDestroy cancels all jobs`() = runBlocking {
        val presenter = FakePresenter()
        presenter.onCreate()
        delay(200)
        presenter.onDestroy()
        delay(200)
        assertEquals(2, presenter.cancelledJobs)
    }

    @Test
    fun `Coroutines run on main thread`() = runBlocking {
        var threads = listOf<Thread>()
        val presenter = FakePresenter(
                jobInterceptor = {
                    threads += Thread.currentThread()
                }
        )
        presenter.onCreate()
        delay(100)
        presenter.onDestroy()
        delay(100)
        threads.forEach {
            assert(it.name.startsWith("UIThread")) { "We should switch to UI thread, and now we are on ${it.name}" }
        }
    }

    @Test
    fun `When a job throws an error, it is handled`(): Unit = runBlocking {
        val error = Error()
        var errors = listOf<Throwable>()
        val presenter = FakePresenter(
                jobInterceptor = { throw error },
                onError = { errors += it }
        )
        presenter.onCreate()
        delay(200)
        assertEquals(listOf(error, error), errors)
    }
}