package com.example.addnewsapp.api

import com.example.addnewsapp.models.NewsResponse
import com.example.addnewsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewAPI {

    @GET("v2/top-headlines")
    suspend fun  getHeadLines(
        @Query("country")
        countryCode: String = "us",
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int= 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ):Response<NewsResponse>
}