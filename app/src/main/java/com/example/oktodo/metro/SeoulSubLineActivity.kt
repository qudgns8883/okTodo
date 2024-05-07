package com.example.oktodo.metro

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivitySeoulSubLineBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView


class SeoulSubLineActivity : AppCompatActivity() {


    private lateinit var selectedStation: String
    private lateinit var button: Button
    private lateinit var backBtn: Button
    private lateinit var line_spinner: Spinner
    private lateinit var station_spinner: Spinner
    private lateinit var binding: ActivitySeoulSubLineBinding
    private var isDrawerOpen = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivitySeoulSubLineBinding.inflate(layoutInflater)

        setContentView(binding.root)

        station_spinner = findViewById(R.id.spinner_station_selector)
        line_spinner = findViewById(R.id.spinner_line_selector)
        button = findViewById(R.id.checkBtn)
        backBtn = findViewById(R.id.seoulBackBtn)

        val mainBtn = findViewById<ImageView>(R.id.icon_home)
        mainBtn.setOnClickListener {
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


        // 서울 호선(line) 고르는 스피너 어댑터, 스피너 선택항목 목록(array)은
        // res/values/strings.xml에 있음
        ArrayAdapter.createFromResource(
            this,
            R.array.station_line_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            line_spinner.adapter = adapter
        }

        // Spinner 아이템 선택 리스너
        line_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) { // 호선(line) 스피너에서 선택한 값(station_line_options)에 따라서
                // lines에 담을 아이템목록을 변경시킴
                val lines = when (parent.getItemAtPosition(position).toString()) {
                    "호선을 선택해 주세요" -> R.array.not_choice
                    "1호선" -> R.array.line1_station_options
                    "2호선" -> R.array.line2_station_options
                    "3호선" -> R.array.line3_station_options
                    "4호선" -> R.array.line4_station_options
                    "5호선" -> R.array.line5_station_options
                    "6호선" -> R.array.line6_station_options
                    "7호선" -> R.array.line7_station_options
                    "8호선" -> R.array.line8_station_options
                    "9호선" -> R.array.line9_station_options
                    "경의중앙선" -> R.array.gyeongui_jungang_station_options
                    "공항철도" -> R.array.AREX_station_options
                    "경춘선" -> R.array.gyeongchun_line_station_options
                    "수인분당선" -> R.array.suin_bundang_line_station_options
                    "신분당선" -> R.array.sinbundang_line_station_options
                    "경강선" -> R.array.gyeongchun_line_station_options
                    "우이신설선" -> R.array.ui_sinseol_line_station_options
                    "서해선" -> R.array.seohae_line_station_options
                    else -> 0
                }
                if (lines != 0) {
                    // if문을이용한 2중 스피너
                    // 호선(line) 스피너에 선택한 값에따라 역목록(lines)에 담을 역목록(R.array.---)을 변경시키고
                    // 선택된 호선의 역목록들이 출력될수있게, 호선(line)을 고르면 해당 호선에맞는 역목록(lines)로 스피너가 출력되게함
                    ArrayAdapter.createFromResource(
                        this@SeoulSubLineActivity,
                        lines,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        station_spinner.adapter = adapter
                    }
                }
            }
            // 아무것도 선택안했을때
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        // 역 목록 선택 스피너 리스너 (선택된 역목록을 selectedStation 변수에 담아둔다)
        station_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedStation = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // 완료 버튼 : 스피너에서 선택된 지하철역 selectedStation의 값이 있을경우 stationName 이란 이름으로
        // intent에 담아서 SeoulChosenActivity(결과출력화면)로 보낸다
        // 선택된 지하철역이 없을경우 Toast로 호선을 선택하라고 출력시킴
        button.setOnClickListener {
            if (selectedStation != "-") {
                val intent = Intent(this@SeoulSubLineActivity, SeoulChosenActivity::class.java).apply {
                    putExtra("stationName", selectedStation)
                }
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "호선을 선택해 주세요", Toast.LENGTH_SHORT).show()
            }
        }

        //뒤로가기 버튼
        backBtn.setOnClickListener {
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

        // NavigationView의 헤더 뷰를 얻음
        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val headerView = navigationView.getHeaderView(0) // index 0으로 첫 번째 헤더 뷰를 얻음

        // NavigationView 메뉴 텍스트 업데이트 코드 추가
        NavigationMenuClickListener(this).updateMenuText(navigationView)

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))

    }
}
