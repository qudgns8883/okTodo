package com.example.oktodo.metro

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivityBusanChosenBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class BusanChosenActivity : AppCompatActivity() {


    private lateinit var textView: TextView
    private lateinit var stationTextView: TextView
    private lateinit var binding: ActivityBusanChosenBinding
    private lateinit var scrollView: ScrollView
    private var isDrawerOpen = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBusanChosenBinding.inflate(layoutInflater)

        setContentView(binding.root)

        scrollView = findViewById(R.id.busanScrollView)
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

            scrollView.setOnTouchListener { _, event ->
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
            CardViewClickListener.setupCardViewClickListeners(headerView, this@BusanChosenActivity, this@BusanChosenActivity)

            // View Binding을 사용하여 NavigationView에 리스너 설정
            binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this@BusanChosenActivity))



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