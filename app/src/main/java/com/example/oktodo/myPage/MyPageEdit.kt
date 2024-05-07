package com.example.oktodo.myPage

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivityMypageEditBinding
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.MemberDao
import com.example.oktodo.db.MemberEntity
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.example.oktodo.util.signUpUtils.SignUpUtils
import com.example.oktodo.util.signUpUtils.SignUpUtils.convertUriToByteArray
import com.example.oktodo.util.signUpUtils.SignUpUtils.saveImageAndPathInPreferences
import com.example.oktodo.util.signUpUtils.SignUpUtils.validateInputs
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MyPageEdit : AppCompatActivity() {
    // 뷰 바인딩, 데이터베이스, DAO 초기화
    private lateinit var binding: ActivityMypageEditBinding
    lateinit var db: AppDatabase
    lateinit var memberDao: MemberDao
    private var imageUri: Uri? = null
    private var isDrawerOpen = false

    // SignUpUtils 인스턴스 생성
    private val signUpUtils = SignUpUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 레이아웃 설정
        binding = ActivityMypageEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences로부터 사용자 정보 가져오기
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val userNickName = sharedPreferences.getString("Nickname", "")
        val userEmail = sharedPreferences.getString("Email", "")
        val userPassword = sharedPreferences.getString("Password", "")

        // TextView에 밑줄이 있는 텍스트 설정
        binding.editTextEmail.setText(userEmail)
        binding.editTextPassword.setText(userPassword)
        binding.editTextNickName.setText(userNickName)

        // 프로필 이미지의 모서리를 둥글게 처리
        binding.editImage.clipToOutline = true

        // 데이터베이스 인스턴스 초기화
        db = AppDatabase.getInstance(this)!!
        memberDao = db.getMemberDao()

        // 뒤로가기 클릭 이벤트 설정
        binding.resetButton.setOnClickListener {
            finish()
        }

        // 홈 버튼 클릭시 메인 엑티비티
        binding.homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 회원정보 수정 버튼 클릭 이벤트 설정
        binding.updateButton.setOnClickListener {
            lifecycleScope.launch {
                update() // 사용자 정보 업데이트 함수
            }
        }

        // 프로필 이미지 선택 이벤트 설정
        binding.btnImageView.setOnClickListener {
            activityResult.launch("image/*")
        }

        // 메뉴 아이콘 클릭 시 네비게이션 드로어의 가시성을 토글
        binding.menuIcon.setOnClickListener {
            isDrawerOpen = DrawerUtil.toggleDrawer(binding.navigationDrawer, isDrawerOpen)
        }

        // 메인 레이아웃에 터치 리스너를 설정
        // 경고를 무시: 이 경우 performClick을 호출하지 않는 것이 의도된 동작
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

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, this, this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        binding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(this))
    }

    // Activity가 다시 시작될 때마다 호출되는 onResume() 메소드 오버라이드
    override fun onResume() {
        super.onResume()
        loadUpdatedUserProfileImage()
    }

    private fun loadUpdatedUserProfileImage() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        // "UserImageFilePath" 키로 저장된 이미지 경로를 먼저 시도하고, 없으면 "ProfileImageUrl" 키로 저장된 경로를 사용
        val updatedImagePath = sharedPreferences.getString("UserImageFilePath", null)
            ?: sharedPreferences.getString("ProfileImageUrl", null)

        updatedImagePath?.let {
            if (it.startsWith("/")) {
                // 파일 경로가 있으면 해당 경로에서 이미지를 로드하여 ImageView에 설정
                val bitmap = BitmapFactory.decodeFile(it)
                binding.editImage.setImageBitmap(bitmap)
            } else {
                // 파일 경로 대신 Base64 인코딩된 문자열이 있으면, 이를 바이트 배열로 변환 후 이미지로 디코드하여 ImageView에 설정
                val imageBytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.editImage.setImageBitmap(bitmap)
            }
        } ?: run {
            // 기본 이미지 설정
            binding.editImage.setImageResource(R.drawable.profileee)
        }
    }

    private suspend fun update() {
        val updateEmail = binding.editTextEmail.text.toString()
        val updatePassword = binding.editTextPassword.text.toString()
        val updateNickName = binding.editTextNickName.text.toString()

        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val userMno = sharedPreferences.getString("mno", null)?.toIntOrNull()

        // 입력 유효성 검사 실패 시 함수 종료
        if (!validateInputs(this, updateEmail, updatePassword, updateNickName)) return

        // 이미지 처리 및 바이트 배열 가져오기 (SignUpUtils 클래스 사용)
        val imageByteArray: ByteArray = imageUri?.let {
            signUpUtils.convertUriToByteArray(this, it)
        } ?: signUpUtils.getDefaultImageByteArray(this) // 기본 이미지 사용

        lifecycleScope.launch {
            // userMno가 null이 아닐 때만 조회
            val existingMember = userMno?.let { memberDao.getMemberById(it) }
            if (existingMember != null) {
                // 기존 사용자 정보를 새 정보로 업데이트합니다.
                val updateEntity = MemberEntity(userMno, updateEmail, updatePassword, updateNickName, imageByteArray)
                memberDao.updateMember(updateEntity)

                Toast.makeText(this@MyPageEdit, "회원정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()

                // 업데이트된 정보를 SharedPreferences에 저장
                val editor = sharedPreferences.edit()
                editor.putString("Nickname", updateEntity.nickName)
                editor.putString("Email", updateEntity.email)
                editor.putString("Password", updateEntity.password)
                editor.apply()

                if (imageByteArray.isNotEmpty()) {
                    saveImageAndPathInPreferences(this@MyPageEdit, imageByteArray, updateNickName) // 수정된 이미지 경로를 SharedPreferences에 저장
                }

                navigateToLogin()
            } else {
                Toast.makeText(this@MyPageEdit, "회원 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리에서 이미지 선택했을 때의 콜백 처리
    private val activityResult: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handleImageSelection(uri)
            imageUri = uri
        } else {
            Log.d("ImageSelection", "이미지 선택 실패: 사용자가 이미지를 선택하지 않았거나 기타 오류가 발생했습니다.")
        }
    }

    // 사용자가 선택한 이미지 처리
    private fun handleImageSelection(uri: Uri) {
        lifecycleScope.launch {
            val byteData = convertUriToByteArray(this@MyPageEdit , uri)
            val bitmap = BitmapFactory.decodeByteArray(byteData, 0, byteData.size)
            // ImageView 업데이트
            binding.editImage.setImageBitmap(bitmap)
            Log.d("ImageSelection", "Selected image URI: $uri")
        }
    }

    // 수정이 성공적으로 완료된 후 마이페이지 화면으로 이동
    private fun navigateToLogin() {
        val intent = Intent(this@MyPageEdit, MyPage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}









