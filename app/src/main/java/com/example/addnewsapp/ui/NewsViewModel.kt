package com.example.addnewsapp.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.addnewsapp.models.Article
import com.example.addnewsapp.models.NewsResponse
import com.example.addnewsapp.repository.NewsRepository
import com.example.addnewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(app: Application, val newsRepository: NewsRepository): AndroidViewModel(app) {

    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlineResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                headlinesPage++
                if (headlineResponse == null){
                    headlineResponse = resultResponse
                } else{
                    val  oldArticle = headlineResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticle?.addAll(newArticles)
                }
                return Resource.Success(headlineResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }
    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
    @SuppressLint("MissingPermission")
    fun internetConnection(context: Context): Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)-> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else->false
                }
            } ?: false
        }
    }


    private suspend fun headlinesInternet(countryCode:String){
        headlines.postValue(Resource.Loading())
        try {
            if(internetConnection(this.getApplication())) {
                val response = newsRepository.getHeadlines((countryCode), headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("Tidak ada Internet"))
            }
        } catch (t: Throwable) {
            when(t) {
                is IOException-> headlines.postValue(Resource.Error("Tidak bisa konek"))
                else -> headlines.postValue(Resource.Error("Tidak ada sinyal"))
            }
        }
    }
    private suspend fun searchNewsInternet(searchQuery: String){
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
            try {
                if (internetConnection(this.getApplication())) {
                    val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                    searchNews.postValue(handleHeadlinesResponse(response))
                } else {
                    searchNews.postValue(Resource.Error("Tidak ada Internet"))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> searchNews.postValue(Resource.Error("Tidak bisa konek"))
                    else -> searchNews.postValue(Resource.Error("Tidak ada sinyal"))
                }
            }

    }
}

