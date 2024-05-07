package com.example.oktodo.myPage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.oktodo.Login.LoginActivity
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivityMypageBinding
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.MemberDao
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// 액티비티의 메인 클래스를 정의합니다.
class MyPage : AppCompatActivity() {
    // 레이아웃 바인딩, 데이터베이스 객체, DAO, 레이아웃 컨트롤러들 선언
    private lateinit var binding: ActivityMypageBinding
    lateinit var db: AppDatabase
    lateinit var memberDao: MemberDao
    private var isDrawerOpen = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // XML 레이아웃을 View와 바인딩합니다.
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val loginType = sharedPreferences.getString("LoginType", "")
        val userNickName = sharedPreferences.getString("Nickname", "")
        val userEmail = sharedPreferences.getString("Email", "")
        val userPassword = sharedPreferences.getString("Password", "")

        // TextView에 사용자 정보 표시
        binding.savedEmail.text = userEmail
        binding.savedNickname.text = userNickName

        // 사용자 인터페이스 설정
        setupUserInterface(loginType, userPassword!!)

        // Room 데이터베이스 인스턴스화 및 DAO 가져오기
        db = AppDatabase.getInstance(this)!!
        memberDao = db.getMemberDao()

        // 홈 버튼 클릭시 메인 엑티비티
        binding.homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 메뉴 아이콘 클릭 시 네비게이션 드로어의 가시성을 토글
        binding.menuIcon.setOnClickListener {
            isDrawerOpen = DrawerUtil.toggleDrawer(binding.navigationDrawer, isDrawerOpen)
        }

        // 메인 레이아웃에 터치 리스너를 설정
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

        // NavigationView 메뉴 텍스트 업데이트 코드 추가
        NavigationMenuClickListener(this).updateMenuText(navigationView)

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))

    }

    // 사용자 인터페이스 설정
    private fun setupUserInterface(loginType: String?, userPassword: String) {
        if (loginType == "Google" || loginType == "Naver" || loginType == "Kakao") {
            // 소셜 로그인 사용자의 경우
            binding.editBtn.alpha = 0.5f // 버튼 흐리게 표시
            binding.editBtn.setOnClickListener {
                Toast.makeText(this, "소셜 로그인 회원은 수정을 할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

            // 회원 탈퇴 버튼 비활성화 및 메시지 표시
            binding.deleteBtn.alpha = 0.5f
            binding.deleteBtn.setOnClickListener {
                Toast.makeText(this, "소셜 로그인 회원은 탈퇴를 할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 일반 로그인 사용자의 경우
            binding.savedPassword.text = userPassword // 비밀번호 필드 표시
            // 회원 정보 수정 가능
            binding.editBtn.isEnabled = true
            binding.editBtn.alpha = 1.0f
            binding.editBtn.setOnClickListener {
                // 회원 정보 수정 로직 구현
                verifyPasswordAndNavigate()
            }
            // 회원 탈퇴 가능
            binding.deleteBtn.isEnabled = true
            binding.deleteBtn.alpha = 1.0f
            binding.deleteBtn.setOnClickListener {
                // 회원 탈퇴 로직 구현
                verifyPasswordAndNavigateDelete()
            }
        }
    }

    // Activity가 다시 시작될 때마다 호출되는 onResume() 메소드 오버라이드
    override fun onResume() {
        super.onResume()
        loadUserProfileImage()
    }

    // 사용자 프로필 이미지를 로드하는 메소드
    private fun loadUserProfileImage() {
        lifecycleScope.launch {
            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val loginType = sharedPreferences.getString("LoginType", "")
            val imageUrl = sharedPreferences.getString("ProfileImageUrl", null)
            val imagePath = sharedPreferences.getString("UserImageFilePath", null)
            val imageView = binding.editImage

            when (loginType) {
                "Kakao", "Google", "Naver" -> {
                    Glide.with(this@MyPage).load(imageUrl).into(imageView)
                }
                "General" -> {
                    if (imagePath != null) {
                        // 저장된 이미지 경로에서 비트맵을 디코딩
                        val bitmap = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(imagePath) }
                        imageView.setImageBitmap(bitmap)
                    } else {
                        imageView.setImageResource(R.drawable.profileee) // 기본 이미지 설정
                    }
                }
                else -> imageView.setImageResource(R.drawable.profileee) // 기본 이미지 설정
            }
        }
    }

    // 회원 수정 비밀번호 확인 함수
    private fun verifyPasswordAndNavigate() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("비밀번호 확인")
        alertDialog.setMessage("수정을 계속하려면 비밀번호를 입력하세요.")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        alertDialog.setView(input)

        alertDialog.setPositiveButton("확인") { _, _ ->
            val password = input.text.toString()
            val savedPassword =
                getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    .getString("Password", "")
            if (password == savedPassword) {
                startActivity(Intent(this, MyPageEdit::class.java))
            } else {
                Toast.makeText(applicationContext, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        alertDialog.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
        alertDialog.show()
    }

    // 회원 탈퇴 비밀번호 확인 함수
    private fun verifyPasswordAndNavigateDelete() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("비밀번호 확인")
        alertDialog.setMessage("계속하려면 비밀번호를 입력하세요.")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        alertDialog.setView(input)

        alertDialog.setPositiveButton("확인") { _, _ ->
            val password = input.text.toString()
            val savedPassword = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                .getString("Password", "")
            if (password == savedPassword) {
                // 비밀번호 일치 시 회원 탈퇴 확인 대화상자를 띄움
                showDeleteConfirmDialog()
            } else {
                Toast.makeText(applicationContext, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        alertDialog.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
        alertDialog.show()
    }

    // 회원 탈퇴 확인 대화 상자를 보여주는 함수
    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("회원 탈퇴")
            setMessage("정말로 탈퇴하시겠습니까?")
            setPositiveButton("탈퇴") { _, _ ->
                deleteUserAccount()
            }
            setNegativeButton("취소", null)
        }.show()
    }

    // 사용자 계정을 삭제하는 함수
    private fun deleteUserAccount() {
        // SharedPreferences에서 사용자의 mno(회원 번호)를 가져옴
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val mnoString = sharedPreferences.getString("mno", null)
        val mno = mnoString?.toIntOrNull()

        // 비동기 처리를 위해 Coroutine 사용
        lifecycleScope.launch {
            try {
                val member = memberDao.getMemberById(mno!!)
                if (member != null) {
                    memberDao.deleteMember(member)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MyPage, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "해당 회원을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
















