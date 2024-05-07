package com.example.oktodo.metro

import android.content.Intent
import android.os.Bundle
import android.util.Xml
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivityBusanBusBinding
import com.example.oktodo.databinding.ActivityBusanBusChosenBinding
import com.example.oktodo.metro.data.BusInfo
import com.example.oktodo.metro.data.BusNumberConverter
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class BusanBusChosenActivity : AppCompatActivity() {

    private val url = "http://apis.data.go.kr/6260000/BusanBIMS/bitArrByArsno" //api 주소
    private val serviceKey =
        "JwUvpcd%2FCCe%2B3OPRMv9uJQu1uaExf23jTY3Nkg7dSAAjVI3x9PyNlO6WXq3TvOhTCqSFXLlDY98BBjR%2Fm6EGHQ%3D%3D" // 서비스키
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: BusInfoAdapter
    private var isDrawerOpen = false
    private lateinit var mainBtn: ImageView
    private lateinit var leaveBtn: Button
    private lateinit var binding: ActivityBusanBusChosenBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBusanBusChosenBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val userInput = intent.getStringExtra("userInput")
        // userInput이 널이아닐때만 BusNumberConverter.convertToDigit 함수를 발동시켜서 입력받은값에 해당하는 정류장번호로 변환
        val digitInput = userInput?.let { BusNumberConverter.convertToDigit(it) }
        val busAPI = "${url}?serviceKey=${serviceKey}&arsno=${digitInput}"
        // 버스정류장 목록이 너무많아서 시연용으로 일부데이터만 입력받을수있게 설정해놓았음.

        // 버스 API 요청
        sendBusRequest(busAPI)

        // 요청후 응답받은 버스 API 정보를 리사이클러뷰를 통해 출력
        recyclerView = findViewById(R.id.busInfoRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BusInfoAdapter(emptyList())
        recyclerView.adapter = adapter

        mainBtn = findViewById(R.id.icon_home)
        leaveBtn = findViewById(R.id.leaveBtn)

        // 홈화면으로 이동
        mainBtn.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // 뒤로가기
        leaveBtn.setOnClickListener {
            finish()
        }

        // 메뉴 아이콘 클릭 시 네비게이션 드로어의 가시성을 토글
        binding.menuIcon.setOnClickListener {
            isDrawerOpen = DrawerUtil.toggleDrawer(binding.navigationDrawer, isDrawerOpen)
        }

        // 메인 레이아웃에 터치 리스너를 설정
        // 경고를 무시: 이 경우 performClick을 호출하지 않는 것이 의도된 동작
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isDrawerOpen) {
                if (!DrawerUtil.isPointInsideView(event.rawX, event.rawY, binding.navigationDrawer)) {
                    isDrawerOpen = DrawerUtil.closeDrawer(binding.navigationDrawer, isDrawerOpen)
                }
            }
            false
        }

        // 리사이클러 뷰 를 클릭하면 토글닫는 이벤트가 발동하지않아서 리사이클러뷰에다가 따로 이벤트를 한번더준거임
        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isDrawerOpen) {
                if (!DrawerUtil.isPointInsideView(event.rawX, event.rawY, binding.navigationDrawer)) {
                    isDrawerOpen = DrawerUtil.closeDrawer(binding.navigationDrawer, isDrawerOpen)
                }
            }
            false
        }

        // NavigationView의 헤더 뷰를 얻음
        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val headerView = navigationView.getHeaderView(0) // index 0으로 첫 번째 헤더 뷰를 얻음

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))



    }

    // 버스 API 요청후 응답받은 정보를 리사이클러 뷰 어댑터에 업데이트 해주는 함수
    private fun sendBusRequest(busAPI: String) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(busAPI)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val inputStream = conn.inputStream
                // XML 데이터 파싱
                val busInfos = parseXml(inputStream)

                // UI 업데이트를 위한 메인 스레드 호출
                withContext(Dispatchers.Main) {
                    adapter = BusInfoAdapter(busInfos)
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
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
