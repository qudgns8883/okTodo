package com.example.oktodo.forum

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ForumActivityMainBinding
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
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

        // intent에서 값을 가져오고, null인 경우 대체값을 사용
        val mno = intent?.getStringExtra("mno") ?: "default_value"

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

        // 탭 생성 및 추가
        val tabLayout = findViewById<TabLayout>(R.id.forum_tabs)

        // 시작 시 첫 번째 탭이 선택된 상태로 하기 위해서 추가
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.forum_tabContent, ForumFourthFragment.userInstance(mno)) // 액티비티에서 프래그먼트로 값 넘겨주기
            addToBackStack(null)
            commit()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // 탭 버튼을 선택할 때 이벤트
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val transaction = supportFragmentManager.beginTransaction()
                when (tab?.position) {
                    0 -> transaction.replace(R.id.forum_tabContent, ForumFourthFragment.userInstance(mno))
                    1 -> transaction.replace(R.id.forum_tabContent, ForumSecondFragment.userInstance(mno))
                    2 -> transaction.replace(R.id.forum_tabContent, ForumThirdFragment.userInstance(mno))
                }
                transaction.addToBackStack(null)
                transaction.commit()
            }

            // 다른 탭버튼을 눌러 선택된 탭 버튼이 해제될 때 이벤트
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            // 선택된 탭 버튼을 다시 선택할 때 이벤트
            override fun onTabReselected(tab: TabLayout.Tab?) {
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