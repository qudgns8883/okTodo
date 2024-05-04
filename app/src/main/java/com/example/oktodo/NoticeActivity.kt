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
import com.example.oktodo.databinding.ActivityNoticeBinding
import com.example.oktodo.todoList.TodoMainActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

class NoticeActivity   : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
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
        //네비바 항목 페이지 이동
        binding.mainDrawerView.setNavigationItemSelectedListener(this)

        // 네비게이션 뷰에서 icon_todo ImageView를 찾아 클릭 이벤트 설정
        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val headView = navigationView.getHeaderView(0)
        val todoView = headView.findViewById<ImageView>(R.id.icon_todo)
        todoView.setOnClickListener {
            val intent = Intent(this@NoticeActivity, TodoMainActivity::class.java)
            startActivity(intent)
        }

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
