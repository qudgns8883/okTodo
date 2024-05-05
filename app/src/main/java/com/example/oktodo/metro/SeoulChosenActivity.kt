package com.example.oktodo.metro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivitySeoulChosenBinding
import com.example.oktodo.metro.data.TrainSchedule
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.URL

class SeoulChosenActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrainScheduleAdapter
    private lateinit var binding: ActivitySeoulChosenBinding
    private var isDrawerOpen = false
    private lateinit var drawerLayout: DrawerLayout




    // loadStationData : 로컬에 저장되어있는 (res/raw/ ~~~) 를 가져옴 ( 직접 지정해둔 api 링크를 불러오는거 )
    // 그러나 이방식은 url을 데이터파일에 하나씩 우겨넣는 하드코딩이기때문에 유지보수가 힘들고 유연성이 떨어짐..
    // 방법을 바꾸기위한 공부가 필요함
    // fetchTrainSchedule : API 링크 안에있는 제이슨객체의 데이터를 가져오는거


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySeoulChosenBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mainBtn = findViewById<ImageView>(R.id.icon_home)


        mainBtn.setOnClickListener{
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 어댑터 인스턴스화 및 리사이클러 뷰에 설정
        adapter = TrainScheduleAdapter(emptyList())
        recyclerView.adapter = adapter

        // SeoulSubLineActivity 에서 intent로 가져온값 (역목록 스피너에서 선택된 값)
        val stationName = intent.getStringExtra("stationName")
        val stationData = loadStationData(stationName)


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
            CardViewClickListener.setupCardViewClickListeners(headerView, this@SeoulChosenActivity, this@SeoulChosenActivity)

            // View Binding을 사용하여 NavigationView에 리스너 설정
            binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this@SeoulChosenActivity))



            val trainScheduleList = fetchTrainSchedule(stationData)
            adapter.updateData(trainScheduleList)
        }
    }

    // inputStream : res/raw/ 파일에 저장되어있는 station_data 라는 파일의 이름을 읽어옴
    // jsonString : 위에서 생성한 inputStream을 이용하여 파일의 내용을 String으로 읽어와서 jsonString 변수에 저장함
    // jsonObject : 읽어들인 문자열을 JSON 객체로 파싱함
    // stations : 파싱한 제이슨객체(jsonObject)에서 "station" 이라는 이름을가진 JSON 배열을 추출함
    // 즉 rew/raw/station_data.json에 있는 데이터내용들을 제이슨객체로 파싱후 station이란 이름의 배열을 추출하는 과정

    private fun loadStationData(stationName: String?): String {
        val inputStream = resources.openRawResource(R.raw.station_data)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val stations = jsonObject.getJSONArray("stations")


        // 제이슨객체의 station 배열을 for문으로 각각의 제이슨객체를 순회함
        for (i in 0 until stations.length()) {
            val station = stations.getJSONObject(i)
            // station 배열을 for문으로 돌면서 매개변수로받은 stationName 이 name과 같을 경우
            // station 그 name객체의 url을 반환함)
            if (station.getString("name") == stationName) {
                return station.getString("url")
            }
        }
        return ""
    }



    private suspend fun fetchTrainSchedule(stationData: String): MutableList<TrainSchedule> =
        withContext(Dispatchers.IO) {

            val trainScheduleList = mutableListOf<TrainSchedule>() //TrainSchedule 객체들을 담을 리스트 초기화


            // url : 매개변수로 받은 stationData(API링크)를 URL 객체로 변경
            // conn : 해당 URL에 연결시작
            // input : 연결을통해 입력스트림을 얻음
            // content : 입력 스트림에서 모든 텍스트를 읽고 자동으로 스트림을닫음 (use는 전부 읽고나서 알아서닫아줌)
            // root : content(읽어온 텍스트 데이터)를 JSON 객체로 파싱
            // trainList : 파싱된 JSON 객체에서 realtimeArrivalList 라는 key에 해당하는 JSON 배열을 가져옴


            try {
                val url = URL(stationData)
                val conn = url.openConnection()
                val input = conn.getInputStream()
                val content = input.bufferedReader().use(BufferedReader::readText)

                val root = JSONObject(content)
                val trainList = root.getJSONArray("realtimeArrivalList")


                // 반복문으로 trainList (JSON 배열) 의 각항목을 순회하며
                // 항목의 데이터를 각각의 변수에 담음 (stationName, destinaton, arrivalTime, lineName)
                // schedule 이란 이름으로 TrainSchedule(데이터 클래스)에 각 변수에 담은 데이터를 넣어주고
                // trainScheduleList.add(schedule)로 TrainSchedule 객체를 리스트에 추가해줌

                for (i in 0 until trainList.length()) {
                    val train = trainList.getJSONObject(i)

                    val stationName = train.getString("statnNm")  // 역 이름
                    val destination = train.getString("trainLineNm")  // 목적지
                    val arrivalTime = train.getString("barvlDt")  // 도착 예정 시간
                    val lineName = train.getString("subwayId")  // 호선

                    val arrivalTimeInMinutes = Math.round(arrivalTime.toFloat() / 60)

                    val schedule =
                        TrainSchedule(stationName, destination, "$arrivalTimeInMinutes", lineName)
                    trainScheduleList.add(schedule)
                }
            } catch (e: Exception) {
//                Toast.makeText(applicationContext, "네트워크 에러!!!", Toast.LENGTH_SHORT).show()
                Log.d("test check error","네트워크 에러")
            }
            return@withContext trainScheduleList
        }
}