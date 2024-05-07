package com.example.oktodo.forum

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ForumActivityWriteBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil.toggleDrawer
import java.util.Calendar

class ForumWriteActivity : AppCompatActivity() {
    lateinit var binding: ForumActivityWriteBinding
    private val viewModel: ForumMainViewModel by viewModels()
    private var selectedLocation: String = "지역"
    private var selectedLocation2: String = "지역2"
    private lateinit var popupMenu: PopupMenu
    private lateinit var drawerLayout: FrameLayout
    private var isDrawerOpen = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForumActivityWriteBinding.inflate(layoutInflater)
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
        // intent에서 값을 가져오고, null인 경우 대체값을 사용
        val mno = intent?.getStringExtra("mno") ?: "default_value"
        val forumContent = intent.getStringExtra("forumContent")
        val forumCno = intent.getStringExtra("forumCno")
        val forumCategory = intent.getStringExtra("forumCategory")
        // 팝업1-팝업2 순으로 열리게 구현해놔서 수정시 팝업2만 수정 불가 (코드 수정 필요)
//        val forumPlace1 = intent.getStringExtra("forumPlace1")
//        val forumPlace2 = intent.getStringExtra("forumPlace2")

        // 교통 or 날씨 라디오버튼
        val inputCategory = when (forumCategory) {
            "교통" -> R.id.f_rg_btn1
            "날씨" -> R.id.f_rg_btn2
            else -> -1
        }

        // 가져온 포럼 정보를 화면에 표시하기
        binding.addEditView.setText(forumContent)
        binding.fRadioGroup.check(inputCategory)

        // PopupMenu 사용
        val myLocationView = findViewById<View>(R.id.myLocationView)
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        val locationTextView2 = findViewById<TextView>(R.id.locationTextView2)

        // 초기 선택된 지역 표시
        locationTextView.text = selectedLocation
        locationTextView2.text = selectedLocation2
        // 팝업1-팝업2 순으로 열리게 구현해놔서 수정시 팝업2만 수정 불가 (코드 수정 필요)
//        if(forumPlace1 != null) {
//            locationTextView.text = forumPlace1
//        } else {
//            locationTextView.text = selectedLocation
//        }
//        if(forumPlace2 != null) {
//            locationTextView2.text = forumPlace2
//        } else {
//            locationTextView2.text = selectedLocation2
//        }

        // 팝업 메뉴 초기화
        popupMenu = PopupMenu(this, myLocationView)

        // 서울 또는 부산을 클릭하면 해당 지역으로 selectedLocation 변수를 업데이트
        locationTextView.setOnClickListener {
            selectLocationPopupMenu()
            popupMenu.show()
        }

        locationTextView2.setOnClickListener {
            updatePopupMenu() // 팝업 메뉴 갱신
            popupMenu.show()
        }

        // save 관련
        binding.checkBtn.setOnClickListener {
            // Intent에서 포럼 정보 가져오기
            val forumCno = intent.getStringExtra("forumCno")
            val inputData = binding.addEditView.text.toString()
            val currentTime = Calendar.getInstance().time // 현재 시간
            // 교통 or 날씨 라디오버튼
            val checkedRadioButtonId_f = binding.fRadioGroup.checkedRadioButtonId
            val inputCategory = when (checkedRadioButtonId_f) {
                R.id.f_rg_btn1 -> "교통"
                R.id.f_rg_btn2 -> "날씨"
                else -> ""
            }
            val inputPlace1 = binding.locationTextView.text.toString()
            val inputPlace2 = binding.locationTextView2.text.toString()

            if (forumCno != null) {
                viewModel.updateForum(
                    text = inputData,
                    cno = forumCno.toLong(),
                    forumCategory = inputCategory,
                    forumPlace1 = inputPlace1,
                    forumPlace2 = inputPlace2
                )
            } else {
                if (mno != null) {
                    viewModel.addForum(
                        mno = mno,
                        inputData,
                        forumTime = currentTime,
                        forumCategory = inputCategory,
                        forumPlace1 = inputPlace1,
                        forumPlace2 = inputPlace2
                    )
                }
            }
            val resultIntent = intent
            resultIntent.putExtra("result", inputData)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
            val intent = Intent(this, ForumMainActivity::class.java)
            startActivity(intent)
        }

        binding.resetBtn.setOnClickListener {
            finish()
        }

        // delete 관련
        binding.deleteFab.setOnClickListener {
            if (forumCno != null) {
                viewModel.deleteForum(forumCno.toLong())
                val intent = Intent(this, ForumMainActivity::class.java)
                startActivity(intent)
            }
        }
        // 선택된 할 일이 없을 때는 지우기 버튼 감추기
        if (forumCno == null) {
            binding.deleteFab.visibility = View.GONE
        } else {
            binding.deleteFab.visibility = View.VISIBLE
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

    private fun selectLocationText() {
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        val locationTextView2 = findViewById<TextView>(R.id.locationTextView2)
        locationTextView.text = selectedLocation
        locationTextView2.text = selectedLocation2
    }

    private fun selectLocationPopupMenu() {
        popupMenu.menu.clear()
        popupMenu.menuInflater.inflate(R.menu.popup_menu_location, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            selectedLocation = menuItem.title.toString()
            selectedLocation2 = "지역2" // 서울이나 부산이 변경되면서 selectedLocation2를 초기화
            selectLocationText()
            true
        }
    }

    // 선택된 지역에 따라 팝업 메뉴 갱신
    private fun updatePopupMenu() {
        popupMenu.menu.clear()
        // 선택된 지역에 따라 다른 메뉴를 표시
        when (selectedLocation) {
            "서울" -> popupMenu.menuInflater.inflate(R.menu.popup_menu_seoul, popupMenu.menu)
            "부산" -> popupMenu.menuInflater.inflate(R.menu.popup_menu_busan, popupMenu.menu)
        }
        popupMenu.setOnMenuItemClickListener { menuItem ->
            selectedLocation2 = menuItem.title.toString()
            selectLocationText()
            true
        }
    }
}