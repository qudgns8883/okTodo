package com.example.oktodo

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.databinding.ActivityNoticeBinding
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

class NoticeActivity   : AppCompatActivity() {
    lateinit var adapter : ListAdapter
    private lateinit var binding: ActivityNoticeBinding
    private lateinit var drawerLayout: FrameLayout
    private var isDrawerOpen = false
    private var noticeList = mutableListOf<Notice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//      데이터 받아오기
        val db = Firebase.firestore

        val docRef = db.collection("Notice")
        docRef.get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    Log.d("test!!!", "${document.id} => ${document.data}")
                    val notice = Notice(
                        title = document.data.get("title").toString(),
                        content = document.data.get("content").toString(),
//                        regTime = dateToString(document.data.get("regTime"))
                        regTime = dateToString(document.getTimestamp("regTime")?.toDate())
                    )
                    noticeList.add(notice)
                    adapter = ListAdapter(noticeList)
                    binding.noticeList.adapter=adapter
                }
            }
            .addOnFailureListener { exception ->
                Log.d("test!!!", "failed with ", exception)
            }

        //아이콘 이동
        val mainView = findViewById<View>(R.id.icon_home)
        mainView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
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
    }
    //    onCreate 끝
    private fun dateToString(date: Date?):String{
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
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
}
