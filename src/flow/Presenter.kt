package flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals

class NewsListPresenter(
    private val view: NewsListView,
    private val newsRepository: NewsRepository,
    private val refreshClicks: Flow<Unit>,
    private val pullRefreshActions: Flow<Unit>,
    private val dispatcher: CoroutineContext
) {

    var a = 20

    fun setUp() {
        // TODO
        CoroutineScope(dispatcher).launch {
            flowOf(refreshClicks, pullRefreshActions)
                .flattenMerge()
//                    .flowOn(Dispatchers.IO)
//                    .flowOn(Dispatchers.Main)
                .onEach { view.showRefresh() }
                .catch { print(it) }
//                    .flowOn(Dispatchers.IO)
                .collect {

                }

            delay(50)
            a = 30

        }
//        refreshClicks.concatWith(pullRefreshActions)
    }
}

interface NewsListView {
    fun showRefresh()
    fun hideRefresh()
}

interface NewsRepository {
    // Blocking operation
    fun getNews(): List<News>
}

class News

class NewsListPresenterTests {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testCoroutineDispatcher.cleanupTestCoroutines()
    }


//    @Test
//    fun `Classic clicks generation test`() = runBlockingTest {
//        val f1 = flow {
//            delay(1_000)
//            emit(Unit)
//            delay(9_000)
//            emit(Unit)
//        }
//        val f2 = flow {
//            delay(5_000)
//            emit(Unit)
//        }
//        val view = ThreadCheckingNewsListView()
//        val repo = NewsRepositoryFake()
//        val presenter = NewsListPresenter(view, repo, f1, f2)
//        presenter.setUp()
//    }

    @Test
    fun `Loading is displayed and hidden on the main thread`() = testCoroutineDispatcher.runBlockingTest {
        val f1 = flowOf(Unit)
        val f2 = flowOf<Unit>()
        val view = ThreadCheckingNewsListView()
        val repo = NewsRepositoryFake()
        val presenter = NewsListPresenter(view, repo, f1, f2, testCoroutineDispatcher)
        presenter.setUp()
        delay(100)
        assertEquals(30, presenter.a)
    }

    class ThreadCheckingNewsListView : NewsListView {
        override fun showRefresh() {
            if (Thread.currentThread().name != "UIThread") throw IncorrectThreadException("It should be started on the main thread")
        }

        override fun hideRefresh() {
            if (Thread.currentThread().name != "UIThread") throw IncorrectThreadException("It should be started on the main thread")
        }

        class IncorrectThreadException(message: String) : Error(message)
    }

    class NewsRepositoryFake : NewsRepository {
        override fun getNews(): List<News> = listOf(News(), News(), News())
    }
}