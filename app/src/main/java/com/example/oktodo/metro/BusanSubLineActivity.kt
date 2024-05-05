package com.example.oktodo.metro

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.oktodo.databinding.ActivityBusanSubLineBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView

class BusanSubLineActivity : AppCompatActivity() {

    private lateinit var selectedBusanStation: String
    private lateinit var selectedBusanUpdownStation: String
    private lateinit var selectedBusanDayStation: String
    private lateinit var button: Button
    private lateinit var backBtn: Button
    private lateinit var busan_line_spinner: Spinner
    private lateinit var busan_station_spinner: Spinner
    private lateinit var busan_station_line_updown_spinner: Spinner
    private lateinit var busan_station_line_day_spinner: Spinner
    private lateinit var binding: ActivityBusanSubLineBinding
    private var isDrawerOpen = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBusanSubLineBinding.inflate(layoutInflater)

        setContentView(binding.root)

        busan_station_spinner = findViewById(R.id.spinner_busan_station_selector)
        busan_line_spinner = findViewById(R.id.spinner_busan_line_selector)
        busan_station_line_updown_spinner = findViewById(R.id.spinner_busan_updown_selector)
        busan_station_line_day_spinner = findViewById(R.id.spinner_busan_day_selector)
        button = findViewById(R.id.button_search)
        backBtn = findViewById(R.id.busanBackBtn)


        val mainBtn = findViewById<ImageView>(R.id.icon_home)

        mainBtn.setOnClickListener {
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


        ArrayAdapter.createFromResource(
            this,
            R.array.busan_station_line_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            busan_line_spinner.adapter = adapter
        }


        // Spinner 아이템 선택

        busan_line_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val b_lines = when(parent.getItemAtPosition(position).toString()) {
                    "호선을 선택해 주세요" -> R.array.not_choice
                    "1호선" -> R.array.busan_line1_station_options
                    "2호선" -> R.array.busan_line2_station_options
                    "3호선" -> R.array.busan_line3_station_options
                    "4호선" -> R.array.busan_line4_station_options
                    else -> 0
                }
                if(b_lines != 0) {
                    ArrayAdapter.createFromResource(
                        this@BusanSubLineActivity,
                        b_lines,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        busan_station_spinner.adapter = adapter
                    }
                }

                val updownOption = when(parent.getItemAtPosition(position).toString()) {
                    "1호선" -> R.array.busan_station_1line_updown_options
                    "2호선" -> R.array.busan_station_2line_updown_options
                    "3호선" -> R.array.busan_station_3line_updown_options
                    "4호선" -> R.array.busan_station_4line_updown_options
                    else -> R.array.not_choice
                }
                ArrayAdapter.createFromResource(
                    this@BusanSubLineActivity,
                    updownOption,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    busan_station_line_updown_spinner.adapter = adapter
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        busan_station_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBusanStation = parent.getItemAtPosition(position).toString()
                Log.d("check", selectedBusanStation)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        busan_station_line_updown_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBusanUpdownStation = parent.getItemAtPosition(position).toString()
                Log.d("check", selectedBusanUpdownStation)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        busan_station_line_day_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBusanDayStation = parent.getItemAtPosition(position).toString()
                Log.d("check", selectedBusanDayStation)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.busan_station_1line_day_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            busan_station_line_day_spinner.adapter = adapter
        }



        button.setOnClickListener {
            if(selectedBusanStation != "-") {
                val intent = Intent(this@BusanSubLineActivity, BusanChosenActivity::class.java).apply {
                    putExtra("BusanStationName", selectedBusanStation)
                    putExtra("BusanStationUpdown",selectedBusanUpdownStation)
                    putExtra("BusanStationDay",selectedBusanDayStation)
                }
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "호선을 선택해 주세요", Toast.LENGTH_SHORT).show()
            }
        }

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

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))

    }
}