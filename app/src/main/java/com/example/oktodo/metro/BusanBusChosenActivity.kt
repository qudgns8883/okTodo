package com.example.oktodo.metro

import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.R
import com.example.oktodo.metro.data.BusInfo
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.XMLFormatter

class BusanBusChosenActivity : AppCompatActivity() {

    private val url = "http://apis.data.go.kr/6260000/BusanBIMS/bitArrByArsno"
    private val serviceKey =
        "JwUvpcd%2FCCe%2B3OPRMv9uJQu1uaExf23jTY3Nkg7dSAAjVI3x9PyNlO6WXq3TvOhTCqSFXLlDY98BBjR%2Fm6EGHQ%3D%3D"
    lateinit var busInfo: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: BusInfoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busan_bus_chosen)

        val userInput = intent.getStringExtra("userInput")
        Log.d("check userInput", "${userInput}")
        val busAPI = "${url}?serviceKey=${serviceKey}&arsno=${userInput}"
        Log.d("check busAPI", busAPI)

        sendBusRequest(busAPI)

        recyclerView = findViewById(R.id.busInfoRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BusInfoAdapter(emptyList())
        recyclerView.adapter = adapter

    }

    private fun sendBusRequest(busAPI: String) {

        busInfo = findViewById(R.id.busInfo)

        Thread {
            try {
                val url = URL(busAPI)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val inputStream = conn.inputStream
                // XML 데이터 파싱
                val busInfos = parseXml(inputStream)
                // UI 업데이트를 위한 메인 스레드 호출
                runOnUiThread {
                    // 어댑터에 새로운 데이터 설정
                    adapter = BusInfoAdapter(busInfos)
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // XML 데이터 파싱하는 함수
    fun parseXml(inputStream: InputStream): List<BusInfo> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()

        val busInfos = mutableListOf<BusInfo>()

        parser.require(XmlPullParser.START_TAG, null, "response")
        while (parser.next() != XmlPullParser.END_TAG) { // response 태그 내부 처리
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "body" -> busInfos.addAll(readBody(parser)) // body 태그 처리
                else -> skip(parser)
            }
        }

        return busInfos
    }

    fun readItem(parser: XmlPullParser): BusInfo {
        parser.require(XmlPullParser.START_TAG, null, "item")

        var bstopid: String = ""
        var nodenm: String = ""
        var min1: String = ""
        var min2: String = ""
        var station1: String = ""
        var station2: String = ""
        var lineno: String = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "bstopid" -> bstopid = readText(parser)
                "nodenm" -> nodenm = readText(parser)
                "min1" -> min1 = readText(parser)
                "min2" -> min2 = readText(parser)
                "station1" -> station1 = readText(parser)
                "station2" -> station2 = readText(parser)
                "lineno" -> lineno = readText(parser)
                else -> skip(parser)
            }
        }
        return BusInfo(bstopid, nodenm, min1, min2, station1, station2, lineno)
        // 데이터클래스에 선언한 변수 순서와 똑같이 순서를 맞춰줘야함
    }
    fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    fun readBody(parser: XmlPullParser): List<BusInfo> {
        parser.require(XmlPullParser.START_TAG, null, "body")
        val busInfos = mutableListOf<BusInfo>()

        while (parser.next() != XmlPullParser.END_TAG) { // body 태그 내부 처리
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "items" -> busInfos.addAll(readItems(parser)) // items 태그 처리
                else -> skip(parser)
            }
        }
        return busInfos
    }

    fun readItems(parser: XmlPullParser): List<BusInfo> {
        parser.require(XmlPullParser.START_TAG, null, "items")
        val busInfos = mutableListOf<BusInfo>()

        while (parser.next() != XmlPullParser.END_TAG) { // items 태그 내부 처리
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "item" -> busInfos.add(readItem(parser)) // item 태그 처리
                else -> skip(parser)
            }
        }
        return busInfos
    }


    fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
