package com.example.weatherapitest

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create

// object -> シングルトン
// そのクラスのインスタンスがアプリ全体で1つしか作られない
// Retrofitの設定は毎回作成する必要がなくなる
object RetrofitInstance {
    private const val BASE_URL
    = "https://api.open-meteo.com/"

    // by lazy -> 遅延初期化
    // apiに初めてアクセスしたときだけ{}が実行される
    // アプリ起動時に不要なものを作らないので、メモリと起動時間の節約になる
    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}