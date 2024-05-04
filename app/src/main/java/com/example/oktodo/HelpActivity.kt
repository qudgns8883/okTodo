package com.example.oktodo

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.databinding.ActivityHelpBinding
import com.example.oktodo.todoList.TodoMainActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout

class HelpActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private lateinit var binding: ActivityHelpBinding
    private lateinit var drawerLayout: FrameLayout
    private var isDrawerOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        //카테고리 선택 탭 생성 및 추가
        val tabLayout = findViewById<TabLayout>(R.id.forum_tabs)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // 탭 버튼을 선택할 때 이벤트
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val helpImage = binding.helpTabContent
                when (tab?.position) {
                    0 -> helpImage.setImageResource(R.drawable.help_sun)
                    1 -> helpImage.setImageResource(R.drawable.help_todo)
                    2 -> helpImage.setImageResource(R.drawable.help_sub)
                    3 -> helpImage.setImageResource(R.drawable.help_com)
                }
            }

            // 다른 탭버튼을 눌러 선택된 탭 버튼이 해제될 때 이벤트
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            // 선택된 탭 버튼을 다시 선택할 때 이벤트
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        //아이콘 이동
        val mainView = findViewById<View>(R.id.icon_home)
        mainView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

//        val btn = findViewById<Button>(R.id.btn)
//        btn.setOnClickListener {
//           val database = Firebase.database("https://ok-todo-default-rtdb.firebaseio.com")
//            val myRef = database.getReference("Notice")
//            myRef.push().child("title").setValue("공지1")
//            myRef.push().child("content").setValue("내용1")
//        }

        // 토글
        drawerLayout = findViewById(R.id.navigation_drawer)
        val showNavigationButton = findViewById<View>(R.id.menu_icon)

        showNavigationButton.setOnClickListener {
            toggleDrawer() // 네비게이션 뷰를 보이도록 변경
        }
        //네비바 항목 페이지 이동
        binding.mainDrawerView.setNavigationItemSelectedListener(this)

        // 네비게이션 뷰에서 icon_todo ImageView를 찾아 클릭 이벤트 설정
        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val headView = navigationView.getHeaderView(0)
        val todoView = headView.findViewById<ImageView>(R.id.icon_todo)
        todoView.setOnClickListener {
            val intent = Intent(this@HelpActivity, TodoMainActivity::class.java)
            startActivity(intent)
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

    //네비바 페이지 이동
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        var intent : Intent? =  null

        when(item.itemId)
        {
            R.id.userMyPage->
                Log.d("aaa","마이페이지 선택")
            R.id.help->
                intent = Intent(this,HelpActivity::class.java)
            R.id.notice->
                intent = Intent(this,NoticeActivity::class.java)
            R.id.logout->
                Log.d("aaa","로그아웃 선택")

        }
        startActivity(intent)
        return super.onContextItemSelected(item)
    }



}