package com.example.weatherapitest

import retrofit2.http.GET
import retrofit2.http.Query

// このAPIにはこうゆうリクエストを送るという仕様を定義する
interface WeatherApiService {
    // このURLにGETリクエストを送る
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        // URLの?キー =値 部分(クエリーパラメーター)を指定
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): String

}