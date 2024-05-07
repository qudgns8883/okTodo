package com.example.oktodo.metro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.MainActivity
import java.net.URL
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivityBusanBusBinding
import com.example.oktodo.databinding.ActivityBusanSubLineBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.nio.Buffer

class BusanBusActivity : AppCompatActivity() {

    private lateinit var busEditText: EditText
    private lateinit var userInput: String
    private lateinit var busBtn: Button
    private lateinit var backBtn: Button
    private var isDrawerOpen = false
    private lateinit var binding: ActivityBusanBusBinding
    private lateinit var mainBtn: ImageView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBusanBusBinding.inflate(layoutInflater)

        setContentView(binding.root)

        busEditText = findViewById(R.id.busEditText)
        busBtn = findViewById(R.id.button_bus_search)
        backBtn = findViewById(R.id.busanBusBackBtn)
        mainBtn = findViewById(R.id.icon_home)


        // 입력받은 값을 intent에 담아 userInput 이라는 이름으로 BusanBusChosenActivity로 보냄
        busBtn.setOnClickListener {
            userInput = busEditText.text.toString()
            intent = Intent(this,BusanBusChosenActivity::class.java).apply {
                putExtra("userInput", userInput)
            }
            startActivity(intent)
        }

        busEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // EditText에서 엔터(완료) 키가 눌러졌을 때 키보드 숨기기
                v.hideKeyboard()
                true
            } else false
        }

        // 뒤로가기버튼
        backBtn.setOnClickListener {
            finish()
        }

        // 메인화면버튼 (홈버튼)
        mainBtn.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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

    fun View.hideKeyboard() {
        val imm = context.getSystemService( Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
