package com.example.oktodo.todoList

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.TodoActivityMainBinding
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView

class TodoMainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: TodoActivityMainBinding
    private lateinit var drawerLayout: FrameLayout
    private var isDrawerOpen = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = TodoActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val mainView = findViewById<View>(R.id.home_icon)
        mainView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // NavigationView의 헤더 뷰를 얻음
        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val headerView = navigationView.getHeaderView(0) // index 0으로 첫 번째 헤더 뷰를 얻음

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))

        // 토글
        drawerLayout = findViewById(R.id.navigation_drawer)
        val showNavigationButton = findViewById<View>(R.id.menu_icon)

        showNavigationButton.setOnClickListener {
            toggleDrawer() // 네비게이션 뷰를 보이도록 변경
        }
    }

    private fun toggleDrawer() {
        val drawerLayout = findViewById<FrameLayout>(R.id.navigation_drawer)
        if(isDrawerOpen) {
            drawerLayout.visibility = View.GONE
        } else {
            drawerLayout.visibility = View.VISIBLE
        }
        isDrawerOpen = !isDrawerOpen
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            if (isDrawerOpen) {
                val rect = Rect()
                drawerLayout.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    // 네비게이션 드로어 밖을 터치하면 드로어를 닫습니다.
                    toggleDrawer()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}