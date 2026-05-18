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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapitest.ui.theme.WeatherAPITESTTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAPITESTTheme {
                Step1Screen()
            }
        }
    }
}

@Composable
fun Step1Screen(){
    // APIから取得したJsonを格納する
    // Jsonが入ったら画面を再描画し、Jsonデータを画面に表示する
    var jsonText by remember { mutableStateOf("まだ取得していません") }

    // Composable関数の中でCoroutineを起動するには「スコープ」が必要
    // スコープ -> 誰がこのCoroutineを管理するのかを決めている単位
    // rememberCoroutineScope() -> Composable専用のCoroutineスコープを取得
    // rememberが付いている -> 再描画されても同じスコープが使われる
    // このComposableが消えると、このスコープで動いているCoroutineも自動的に消える
    // スコープが必要な理由
    // - スコープなしの場合
    //  - 画面が閉じられた後もCoroutineが動き続ける
    //  -> 画面が無いのにAPIを叩き続ける
    // - スコープありの場合
    //  - 画面が閉じられらCoroutineが止まる
    //  -> 画面が消えたら通信が止まる
    val scope = rememberCoroutineScope()

    // API表示部分
    Column(
        modifier = Modifier
            .fillMaxSize() // 画面全体を埋める
            .padding(16.dp) // 外側に16dpの余白
            .verticalScroll( // 縦スクロールを有効にする
                rememberScrollState() // 縦スクロールを管理する
            ),
        horizontalAlignment
        = Alignment.CenterHorizontally // 子要素を水平方向に中央ぞろえ
    ) {
        Text(
            text = "天気を取得 Ver1.0",
            style = MaterialTheme.typography.headlineMedium
        )
        // 余白
        Spacer(modifier = Modifier.height(16.dp))

        // 天気情報を取得するボタン
        Button(
            onClick = {
                // Coroutineを起動
                scope.launch {
                    // 非同期で処理が実行される箇所
                    // 時間がかかっても、メインスレッド(画面描画)はブロックされない
                    // {}の中は別のスレッドで動く
                    // {}の中が終わった時に結果がメインスレッドの方に渡される
                    jsonText = fetchWeatherJson()
                }
                // launchはすぐ返る(中の処理完了を待たない)
                // そのため、ボタンを押した瞬間に次の処理を進める
            }
        ) {
            Text(text = "天気を取得")
        }

        // Jsonの表示場所
        Text(
            text = jsonText,
            style = MaterialTheme.typography.bodyLarge // Jsonは長いので短めのフォント
        )
    }
}

// APIからデータ取得の関数
// suspend -> 一時停止できる
// suspend fun -> Coroutineの中からしか実行できない(通常の場所から呼ぶとエラーになる)

suspend fun fetchWeatherJson(): String{
    // withContext(Dispatchers.IO)
    // -> {}の中だけIOスレッドに切り替えて実行する
    // IOスレッド
    // -> ネットワーク、ファイル操作に適したスレッド
    // {}の処理の最後に書いたものがwithContextの戻り値
    // 処理が終わるとメインスレッドに戻る
    return withContext(Dispatchers.IO){
        try{
            val result = RetrofitInstance.api.getCurrentWeather(
                latitude = 35.6762,
                longitude = 139.6503
            )
            // Jsonデータ
            result
        }catch (e: Exception){
            // ?: -> エラーメッセージがない場合は、「不明なエラーと表示」
            "エラー : ${e.message ?: "不明なエラー"}"
        }
        // withContextはtryかcatchのどちらかの結果を返す
        // どちらもStringなので、withContextの戻り値もStringでよい
    }
}