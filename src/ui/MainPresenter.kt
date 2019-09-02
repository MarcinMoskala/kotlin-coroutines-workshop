package ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class MainPresenter(
        val view: MainView,
        val repo: NetworkRepository
) : BasePresenter(view::onError) {

    fun onCreate() {
        // TODO Uncomment
//        launch {
//            val user = repo.getUser()
//            view.showUserData(user)
//        }
//        launch {
//            val news = repo.getNews()
//                    .sortedByDescending { it.date }
//            view.showNews(news)
//        }
    }
}

interface MainView {
    fun onError(throwable: Throwable): Unit
    fun showUserData(user: UserData)
    fun showNews(news: List<News>)
}

interface NetworkRepository {
    suspend fun getUser(): UserData
    suspend fun getNews(): List<News>
}

class UserData()
class News(val date: Date)