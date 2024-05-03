package com.example.oktodo.Login

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oktodo.MainActivity
import com.example.oktodo.databinding.ActivitySignupBinding
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.MemberDao
import com.example.oktodo.db.MemberEntity
import com.example.oktodo.util.signUpUtils.SignUpUtils
import com.example.oktodo.util.signUpUtils.SignUpUtils.convertUriToByteArray
import com.example.oktodo.util.signUpUtils.SignUpUtils.saveImageAndPathInPreferences
import com.example.oktodo.util.signUpUtils.SignUpUtils.validateInputs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    lateinit var db: AppDatabase
    lateinit var memberDao: MemberDao
    private var imageUri: Uri? = null

    // SignUpUtils 인스턴스 생성
    private val signUpUtils = SignUpUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.userImageView.clipToOutline = true
        db = AppDatabase.getInstance(this)!!
        memberDao = db.getMemberDao()

        // 홈 버튼 클릭시 메인 엑티비티
        binding.homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 회원가입 화면에서의 사용자 상호작용을 설정
        binding.signupButton.setOnClickListener {
            lifecycleScope.launch {
                signUp()
            }
        }

        // 프로필 이미지 선택 이벤트 설정
        binding.btnImageView.setOnClickListener {
            activityResult.launch("image/*")
        }
    }

    // 회원가입 로직을 처리하는 함수
    private suspend fun signUp() {
        // 입력 필드에서 값 추출
        val email = binding.profileEmail.text.toString()
        val password = binding.profilePassword.text.toString()
        val nickName = binding.profileNickname.text.toString()

        // 입력 유효성 검사 실패 시 함수 종료
        if (!validateInputs(this, email, password, nickName)) return

        // 이미지 처리 및 바이트 배열 가져오기 (SignUpUtils 클래스 사용)
        val imageByteArray: ByteArray = imageUri?.let {
            signUpUtils.convertUriToByteArray(this, it)
        } ?: signUpUtils.getDefaultImageByteArray(this) // 기본 이미지 사용

        lifecycleScope.launch {
            // 새로운 회원 정보를 데이터베이스에 등록
            val existingMember = memberDao.findMemberByEmail(email)
            if (existingMember == null) {
                val newMember = MemberEntity(null, email, password, nickName, imageByteArray)
                memberDao.insertMember(newMember)
                showToast("회원가입이 완료되었습니다.")
                saveImageAndPathInPreferences(this@SignUpActivity, imageByteArray, nickName)
                navigateToLogin()
            } else {
                showToast("이미 가입된 계정입니다.")
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
            val byteData = convertUriToByteArray(this@SignUpActivity, uri)
            val bitmap = BitmapFactory.decodeByteArray(byteData, 0, byteData.size)
            // ImageView 업데이트
            binding.userImageView.setImageBitmap(bitmap)
            Log.d("ImageSelection", "Selected image URI: $uri")
        }
    }

    // Toast 메시지를 사용하여 사용자에게 짧은 메시지를 표시
    private fun showToast(message: String) {
        runOnUiThread {
            if (!isFinishing) {
                Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 회원가입이 성공적으로 완료된 후 로그인 화면으로 이동
    private fun navigateToLogin() {
        val intent = Intent(this@SignUpActivity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
