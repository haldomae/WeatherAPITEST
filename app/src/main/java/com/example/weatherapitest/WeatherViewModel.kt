package com.example.weatherapitest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// APIには3~4くらい状態がある(初期状態、通信中、成功、失敗など)
// APIの状態をバラバラで管理するとバグになりやすい
// sealed classを使うとどれか1つの状態しか存在できないようにする
// 画面の状態をsealed classで定義
sealed class WeatherUiState{
    // object -> インスタンスが1つだけ存在するクラス(データを持たない状態の時に使う)
    object Idle: WeatherUiState() // 初期状態
    object Loading: WeatherUiState() // 読み込み中

    // data class -> データを持つ状態に使う
    data class Success(val weather: WeatherResponse): WeatherUiState()
    data class Error(val message: String): WeatherUiState()
}

class WeatherViewModel: ViewModel() {
    // MutableStateFlow -> 値を変更でき、変更されるとすべての箇所に通知される
    // private val _uiState -> 内部のみ参照できる
    private val _uiState
    = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)

    // StateFlow -> 値を読み取ることしかできず、値が変わったときに通知を受け取れる
    // val uiState -> UIに公開(変更不可として公開)
    val uiState: StateFlow<WeatherUiState> = _uiState

    // ボタン押された時にUIから呼ばれる関数
    // suspendはつけなくていい
    fun fetchWeather(){
        // viewModelScope -> ViewModelに紐づいたCoroutineスコープ
        // 画面回転されても通信が継続される
        viewModelScope.launch {
            // まずはローディング状態に変更
            // UIが変化を検知して、ロード状態にする
            _uiState.value = WeatherUiState.Loading

            try {
                val response = withContext(Dispatchers.IO){
                    RetrofitInstance.api.getCurrentWeather(
                        latitude = 35.6762,
                        longitude = 139.6503
                    )
                }
                // 成功したとき、WeatherResponseをSuccessで包んで状態を更新する
                // UIが変化を検知してWeatherCardを表示する
                _uiState.value = WeatherUiState.Success(response)
            }catch (e: Exception){
                _uiState.value = WeatherUiState.Error(e.message ?: "不明なエラー")
            }
        }
    }
}