package com.example.oktodo.forum

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ForumActivityMainBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil.toggleDrawer
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout

class ForumMainActivity  : AppCompatActivity() {
    private lateinit var binding: ForumActivityMainBinding
    private val viewModel: ForumMainViewModel by viewModels()
    private lateinit var drawerLayout: FrameLayout
    private var isDrawerOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ForumActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 로그인 확인
        val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("IsLoggedIn", false)
        val mno = if (isLoggedIn) {
            prefs.getString("mno", "").toString()
        } else {
            "default_value"
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

        // NavigationView 메뉴 텍스트 업데이트 코드 추가
        NavigationMenuClickListener(this).updateMenuText(navigationView)

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))

        // 탭 생성 및 추가
        val tabLayout = findViewById<TabLayout>(R.id.forum_tabs)

        // 시작 시 첫 번째 탭이 선택된 상태로 하기 위해서 추가
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.forum_tabContent, ForumFirstFragment())
            addToBackStack(null)
            commit()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // 탭 버튼을 선택할 때 이벤트
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val transaction = supportFragmentManager.beginTransaction()
                when (tab?.position) {
                    0 -> transaction.replace(R.id.forum_tabContent, ForumFirstFragment())
                    1 -> transaction.replace(R.id.forum_tabContent, ForumSecondFragment())
                    2 -> transaction.replace(R.id.forum_tabContent, ForumThirdFragment())
                }
                transaction.addToBackStack(null)
                transaction.commit()
            }

            // 다른 탭버튼을 눌러 선택된 탭 버튼이 해제될 때 이벤트
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            // 선택된 탭 버튼을 다시 선택할 때 이벤트
            override fun onTabReselected(tab: TabLayout.Tab?) {
                val transaction = supportFragmentManager.beginTransaction()
                when (tab?.position) {
                    0 -> transaction.replace(R.id.forum_tabContent, ForumFirstFragment())
                    1 -> transaction.replace(R.id.forum_tabContent, ForumSecondFragment())
                    2 -> transaction.replace(R.id.forum_tabContent, ForumThirdFragment())
                }
                transaction.addToBackStack(null)
                transaction.commit()
            }
        })

        // 글 작성 버튼
        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.loadForumData() // 새로운 데이터를 가져오기 위해 ViewModel에서 데이터 새로고침
            } else {
                viewModel.loadForumData()
            }
        }

        binding.mainFab.setOnClickListener {
            // mno값 null 판단
            if (mno == "default_value") { // null(예외 처리된 값)
                Toast.makeText(applicationContext, "Login please", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ForumWriteActivity::class.java)
                intent.putExtra("mno", mno)
                requestLauncher.launch(intent)
            }
        }

        // 버튼 클릭 리스너 설정
        binding.searchFab.setOnClickListener {
            val options = arrayOf("전체", "교통", "날씨")
            val defaultOptionIndex = 0 // "전체"를 기본 선택으로 설정

            // AlertDialog.Builder를 사용하여 다이얼로그 생성
            val builder = AlertDialog.Builder(this)
            builder.setTitle("검색") // 다이얼로그 제목 설정

            // 라디오 버튼 목록 설정
            builder.setSingleChoiceItems(options, defaultOptionIndex) { dialog, which ->
                // 선택된 옵션에 대한 처리만 수행하고 다이얼로그를 종료하지 않음
            }

            // 검색을 위한 EditText 추가
            val input = EditText(this)
            input.hint = "검색어를 입력하세요" // placeholder 설정
            builder.setView(input)

            // "확인" 버튼 설정
            builder.setPositiveButton("확인") { dialog, which ->
                val searchText = input.text.toString()
                val selectedOptionIndex = (dialog as AlertDialog).listView.checkedItemPosition // 선택된 옵션의 인덱스 가져오기
                val searchText2 = options[selectedOptionIndex] // 선택된 옵션의 텍스트 가져오기

                // 검색어를 포함한 번들을 생성하여 프래그먼트로 전달
                val bundle = Bundle()
                bundle.putString("searchText", searchText) // 입력받은 text

                when (searchText2) {
                    "전체" -> {
                        // "전체" 옵션이 선택된 경우 firstfragment로 이동
                        tabLayout.getTabAt(0)?.select() // 해당 탭으로 이동
                        val fragment = ForumFirstFragment()
                        fragment.arguments = bundle
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.forum_tabContent, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()

                    }
                    "교통" -> {
                        // "교통" 옵션이 선택된 경우 secondfragment로 이동
                        tabLayout.getTabAt(1)?.select()
                        val fragment = ForumSecondFragment()
                        fragment.arguments = bundle
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.forum_tabContent, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()

                    }
                    "날씨" -> {
                        // "날씨" 옵션이 선택된 경우 thirdfragment로 이동
                        tabLayout.getTabAt(2)?.select()
                        val fragment = ForumThirdFragment()
                        fragment.arguments = bundle
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.forum_tabContent, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
            }

            // "취소" 버튼 설정
            builder.setNegativeButton("취소") { dialog, which ->
                dialog.cancel() // 다이얼로그를 닫음
            }

            // 다이얼로그 보이기
            builder.show()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_forum_main, menu)
//        return super.onCreateOptionsMenu(menu)

        val inflater = menuInflater
        inflater.inflate(R.menu.menu_forum_main, menu)
        val menuItem = menu?.findItem(R.id.forum_search)
        val searchView = menuItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어 변경 이벤트
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // 키보드의 검색 버튼을 클릭한 순간의 이벤트
                return true
            }
        })
        return true
    }
}