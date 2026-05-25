package com.example.weatherapitest

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,

    // jsonのキー名 -> current_weather
    // Kotlinのプロパティ名 -> currentWeather
    // 2つが異なっている
    // @SerializedName
    // -> このプロパティはJsonのcurrent_weatherキーに対応する
    // @SerializedNameが無いとGsonが対応するキーを見つけられない
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather

)
data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Int,
    val weathercode: Int,

    @SerializedName("is_day")
    val isDay: Int,

    val time:String
)
