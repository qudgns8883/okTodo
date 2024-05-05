package com.example.oktodo.metro

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivityMetroBinding
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView

class MetroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMetroBinding
    private lateinit var drawerLayout: FrameLayout
    private var isDrawerOpen = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMetroBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_metro)

        val mainBtn: ImageView = findViewById(R.id.icon_home)
        val busanBtn: Button = findViewById(R.id.busanBtn)
        val seoulBtn: Button = findViewById(R.id.seoulBtn)

        busanBtn.setOnClickListener {
            val intent = Intent(this, BusanSubLineActivity::class.java)
            startActivity(intent)
        }

        seoulBtn.setOnClickListener {
            val intent = Intent(this, SeoulSubLineActivity::class.java)
            startActivity(intent)
        }

        mainBtn.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        // 토글
        drawerLayout = findViewById(R.id.navigation_drawer)
        val showNavigationButton = findViewById<View>(R.id.menu_icon)

        showNavigationButton.setOnClickListener {
            toggleDrawer() // 네비게이션 뷰를 보이도록 변경
        }

        // NavigationView의 헤더 뷰를 얻음
        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val headerView = navigationView.getHeaderView(0) // index 0으로 첫 번째 헤더 뷰를 얻음

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))

        val mainLayout: View = findViewById(android.R.id.content)
        mainLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isDrawerOpen) {
                // 터치된 위치가 drawerLayout 내부인지 확인
                if (!isPointInsideView(event.rawX, event.rawY, drawerLayout)) {
                    closeDrawer()
                }
            }
            // 여기서 false를 반환하면 다른 곳에서도 터치 이벤트를 받을 수 있습니다.
            false
        }
        // 원래 노란줄뜨는거임~
    }

    private fun toggleDrawer() {
        val drawerLayout = findViewById<FrameLayout>(R.id.navigation_drawer)
        if (isDrawerOpen) {
            drawerLayout.visibility = View.GONE
        } else {
            drawerLayout.visibility = View.VISIBLE
        }
        isDrawerOpen = !isDrawerOpen
    }

    private fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val rect =
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
        return rect.contains(x.toInt(), y.toInt())
    }

    private fun closeDrawer() {
        drawerLayout.visibility = View.GONE
        isDrawerOpen = false
    }
}