package com.example.oktodo.metro

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class BusanChosenActivity : AppCompatActivity() {


    lateinit var textView: TextView
    lateinit var stationTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busan_chosen)

        textView = findViewById(R.id.B_subText)
        stationTextView = findViewById(R.id.BusanStation)
        val mainBtn = findViewById<ImageView>(R.id.icon_home)

        mainBtn.setOnClickListener {
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        val stationName = intent.getStringExtra("BusanStationName")
        val stationUpdown = intent.getStringExtra("BusanStationUpdown")
        val stationDay = intent.getStringExtra("BusanStationDay")

        // 리사이클러 뷰 헤더에 데이터연결해서 출력
        stationTextView.append("${stationName}\n${stationUpdown} 시간표")

        val stationData = loadBusanStationData(stationName, stationUpdown, stationDay)

        CoroutineScope(Dispatchers.IO).launch {
            fetchBusanStationData(stationData)
        }
    }

    private fun loadBusanStationData(
        stationName: String?,
        stationUpdown: String?,
        stationDay: String?
    ): String {
        val inputStream = resources.openRawResource(R.raw.station_busan_data)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val stations = jsonObject.getJSONArray("stations")


        for (i in 0 until stations.length()) {
            val station = stations.getJSONObject(i)
            if (station.getString("name") == stationName) {
                val urls = station.getJSONObject("urls")
                val urlForDayType = urls.getJSONObject(stationDay)
                return urlForDayType.getString(stationUpdown)
            }
        }
        return ""
    }

    private suspend fun fetchBusanStationData(stationData: String) {
        var url = URL(stationData)
        var conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connect()

        var input = conn.inputStream
        var isr = InputStreamReader(input, "EUC-KR")

        var br = BufferedReader(isr)
        var str: String? = null
        var buf = StringBuffer()

        while (br.readLine().also { str = it } != null) {
            buf.append(str)
        }

        val root = JSONObject(buf.toString())
        val response = root.getJSONObject("response")
        val body = response.getJSONObject("body")
        val itemList = body.getJSONArray("item")

        CoroutineScope(Dispatchers.Main).launch {

            var counter = 0

            for (i in 0 until itemList.length()) {
                val item = itemList.getJSONObject(i)
                val hour = item.getString("hour")
                val time = item.getString("time")

                textView.append(String.format("%s시 %s분      ", hour, time))
                counter++

                if (counter % 3 == 0) {
                    textView.append("\n")
                }
            }
        }
    }
}