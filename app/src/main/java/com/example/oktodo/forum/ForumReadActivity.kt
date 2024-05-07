package com.example.oktodo.forum

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ForumActivityReadBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil.toggleDrawer

class ForumReadActivity : AppCompatActivity() {
    lateinit var binding: ForumActivityReadBinding
    private val viewModel: ForumMainViewModel by viewModels()
    private lateinit var drawerLayout: FrameLayout
    private var isDrawerOpen = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForumActivityReadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 토글
        drawerLayout = findViewById(R.id.navigation_drawer)
        val showNavigationButton = findViewById<View>(R.id.menu_icon)

        showNavigationButton.setOnClickListener {
            toggleDrawer() // 네비게이션 뷰를 보이도록 변경
        }

        // Intent에서 포럼 정보 가져오기
        val forumContent = intent.getStringExtra("forumContent")
        val forumCno = intent.getStringExtra("forumCno")
        val forumCategory = intent.getStringExtra("forumCategory")
        val forumPlace1 = intent.getStringExtra("forumPlace1")
        val forumPlace2 = intent.getStringExtra("forumPlace2")
        val postMno = intent.getStringExtra("postMno")
        val mno = intent.getStringExtra("mno")

        // 교통 or 날씨 라디오버튼
        val inputCategory = when (forumCategory) {
            "교통" -> R.id.f_rg_btn1
            "날씨" -> R.id.f_rg_btn2
            else -> -1
        }

        // 가져온 포럼 정보를 화면에 표시하기
        binding.addEditView.setText(forumContent)
        binding.fRadioGroup.check(inputCategory)
        binding.locationTextView.setText(forumPlace1)
        binding.locationTextView2.setText(forumPlace2)


        if (mno == "default_value") { // mno null(예외 처리된 값)
            binding.checkBtn.visibility = View.GONE // 수정 버튼 안 보이게 함
        } else if (postMno == mno) { // 작성자 일치
            binding.checkBtn.setOnClickListener {
                // Write로 값 넘겨주기
                val intent = Intent(this, ForumWriteActivity::class.java)
                intent.putExtra("forumCno", forumCno)
                intent.putExtra("forumContent", forumContent)
                intent.putExtra("forumCategory", forumCategory)
                intent.putExtra("forumPlace1", forumPlace1)
                intent.putExtra("forumPlace2", forumPlace2)
                intent.putExtra("mno", mno)
                startActivity(intent)
            }
        } else { // 작성자 불일치
            binding.checkBtn.visibility = View.GONE
        }

        binding.resetBtn.setOnClickListener {
            finish()
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
}