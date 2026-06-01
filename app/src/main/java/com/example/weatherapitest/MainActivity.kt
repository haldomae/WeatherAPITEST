package com.example.weatherapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapitest.ui.theme.WeatherAPITESTTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAPITESTTheme {
                Step3Screen()
            }
        }
    }
}

//@Composable
//fun Step1Screen(){
//    // APIから取得したJsonを格納する
//    // Jsonが入ったら画面を再描画し、Jsonデータを画面に表示する
//    var jsonText by remember { mutableStateOf("まだ取得していません") }
//
//    // Composable関数の中でCoroutineを起動するには「スコープ」が必要
//    // スコープ -> 誰がこのCoroutineを管理するのかを決めている単位
//    // rememberCoroutineScope() -> Composable専用のCoroutineスコープを取得
//    // rememberが付いている -> 再描画されても同じスコープが使われる
//    // このComposableが消えると、このスコープで動いているCoroutineも自動的に消える
//    // スコープが必要な理由
//    // - スコープなしの場合
//    //  - 画面が閉じられた後もCoroutineが動き続ける
//    //  -> 画面が無いのにAPIを叩き続ける
//    // - スコープありの場合
//    //  - 画面が閉じられらCoroutineが止まる
//    //  -> 画面が消えたら通信が止まる
//    val scope = rememberCoroutineScope()
//
//    // API表示部分
//    Column(
//        modifier = Modifier
//            .fillMaxSize() // 画面全体を埋める
//            .padding(16.dp) // 外側に16dpの余白
//            .verticalScroll( // 縦スクロールを有効にする
//                rememberScrollState() // 縦スクロールを管理する
//            ),
//        horizontalAlignment
//        = Alignment.CenterHorizontally // 子要素を水平方向に中央ぞろえ
//    ) {
//        Text(
//            text = "天気を取得 Ver1.0",
//            style = MaterialTheme.typography.headlineMedium
//        )
//        // 余白
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 天気情報を取得するボタン
//        Button(
//            onClick = {
//                // Coroutineを起動
//                scope.launch {
//                    // 非同期で処理が実行される箇所
//                    // 時間がかかっても、メインスレッド(画面描画)はブロックされない
//                    // {}の中は別のスレッドで動く
//                    // {}の中が終わった時に結果がメインスレッドの方に渡される
//                    jsonText = fetchWeatherJson()
//                }
//                // launchはすぐ返る(中の処理完了を待たない)
//                // そのため、ボタンを押した瞬間に次の処理を進める
//            }
//        ) {
//            Text(text = "天気を取得")
//        }
//
//        // Jsonの表示場所
//        Text(
//            text = jsonText,
//            style = MaterialTheme.typography.bodyLarge // Jsonは長いので短めのフォント
//        )
//    }
//}


@Composable
fun Step2Screen(){
    // WeatherResponseかつnullを許容する型にする
    // アプリ起動時にはAPI叩いてないので、最初はnullにする
    // mutableStateOf<WeatherResponse?>は型を明示的に指定する
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }

    // エラーメッセージ(初期表示の文字は無し)
    var errorMessage by remember { mutableStateOf("") }

    // 通信中かどうかのフラグ(初期値はfalse(通信していない))
    var isLoading by remember { mutableStateOf(false) }

    // スコープの設定
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "天気を取得する Ver2.0",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading= true
                    errorMessage = ""
                    try {
                        weather = fetchWeather()
                    }catch (e: Exception){
                        errorMessage = "エラー:${e.message ?: "不明なエラー"}"
                    }finally {
                        isLoading = false
                    }
                }
            }
        ) {
            Text(text = "天気を取得する")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 優先順位を決めて、該当の処理を実行する(When)
        when {
            // グルグル回る
            isLoading ->
                CircularProgressIndicator()
            errorMessage.isNotEmpty() ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            // 天気情報を表示
            // !!はweatherがnullでないことを保証する(!=で確認済)
            weather != null -> WeatherCard(weather!!)
        }


    }
}

@Composable
fun Step3Screen(
    viewModel: WeatherViewModel = viewModel()
){
    // viewModel.uiState -> 作成したおいたViewModelが公開しているStateFlow<WeatherUiState>
    // collectAsState() -> StateFlowをComposeが監視でき、StateFlowの値が変わる度にComposableが再度実行される
    // by -> .valueを省略するために必要
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "天気を取得する Ver3.0",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // ViewModelに用意されている関数を実行するだけ
                // 何を取ってくるか、どう処理するかはViewModelに任せる
                // UIは「ボタンが押された」だけを伝える
                viewModel.fetchWeather()
            }
        ) {
            Text(text = "天気を取得する")
        }


    }

}

// 天気情報を表示する
@Composable
fun WeatherCard(response: WeatherResponse){
    val w  =response.currentWeather

    // 天気コードを日本語に変換する(APIの仕様上)
    val weatherDescription = when (w.weathercode){
        0 -> "快晴"
        1, 2, 3 -> "晴れ ~ 曇り"
        45, 48 -> "霧"
        51, 53, 55 -> "霧雨"
        61, 63, 65 -> "雨"
        71, 73, 75 -> "雪"
        95 -> "雷雨"
        else -> "不明"
    }

    val dayOrNight = if (w.isDay == 1) "昼間" else "夜間"

    // データを表示
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "東京の天気",
            style = MaterialTheme.typography.titleLarge)
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        Text(
            text = "${w.temperature} ℃",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "天気 ${weatherDescription}")
        Text(text = "風速 ${w.windspeed} km/h")
        Text(text = "風向き ${w.winddirection}°")
        Text(text = "時刻 ${w.time}")
        Text(text = "昼夜 $dayOrNight")
    }
}

// API通信を行う関数
suspend fun fetchWeather(): WeatherResponse{
    return withContext(Dispatchers.IO){
        RetrofitInstance.api.getCurrentWeather(
            latitude = 35.6762,
            longitude = 139.6503
        )
    }
}
// APIからデータ取得の関数
// suspend -> 一時停止できる
// suspend fun -> Coroutineの中からしか実行できない(通常の場所から呼ぶとエラーになる)

//suspend fun fetchWeatherJson(): String{
//    // withContext(Dispatchers.IO)
//    // -> {}の中だけIOスレッドに切り替えて実行する
//    // IOスレッド
//    // -> ネットワーク、ファイル操作に適したスレッド
//    // {}の処理の最後に書いたものがwithContextの戻り値
//    // 処理が終わるとメインスレッドに戻る
//    return withContext(Dispatchers.IO){
//        try{
//            val result = RetrofitInstance.api.getCurrentWeather(
//                latitude = 35.6762,
//                longitude = 139.6503
//            )
//            // Jsonデータ
//            result
//        }catch (e: Exception){
//            // ?: -> エラーメッセージがない場合は、「不明なエラーと表示」
//            "エラー : ${e.message ?: "不明なエラー"}"
//        }
//        // withContextはtryかcatchのどちらかの結果を返す
//        // どちらもStringなので、withContextの戻り値もStringでよい
//    }
//}