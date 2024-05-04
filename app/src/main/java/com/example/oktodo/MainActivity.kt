package com.example.oktodo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.AirAndWeather.FragmentActivity
import com.example.oktodo.Login.LoginActivity
import com.example.oktodo.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.navercorp.nid.NaverIdLoginSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.oktodo.forum.ForumMainActivity
import com.example.oktodo.metro.MetroActivity
import com.example.oktodo.myPage.MyPage

class MainActivity : AppCompatActivity() {

    // 뷰 바인딩 변수 초기화
    lateinit var binding: ActivityMainBinding

    // 코루틴을 Android의 메인 스레드(즉, UI 스레드)에서 실행하도록 지정
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scope.launch {
            // 네이버 로그인 기능을 사용하기 전 SDK 초기화
            NaverIdLoginSDK.initialize(
                this@MainActivity,
                "uiQucVW4O9r9bhg80XvD",
                "FyxVPv_DH_",
                "부산it 네이버로그인"
            )
        }
        // 코루틴 내에서 `loginUpdateUI` 함수를 비동기적으로 실행
        scope.launch {
            loginUpdateUI()
        }

        // 날씨, 미세먼지
        binding.weatherView.setOnClickListener {
            scope.launch {
                handleWeatherViewClick()
            }
        }

        // 지하철
        var metro = findViewById<CardView>(R.id.sub_view)

        metro.setOnClickListener {
            intent = Intent(this, MetroActivity::class.java)
            startActivity(intent)
        }

        // forum
        val forumView = findViewById<View>(R.id.comm_view)
        forumView.setOnClickListener {
            val intent = Intent(this, ForumMainActivity::class.java)
            startActivity(intent)
        }
    }

    // 로그인 상태를 확인하고 UI를 업데이트하는 함수
    private suspend fun loginUpdateUI() {
        withContext(Dispatchers.Main) {
            val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            // 로그인 상태와 사용자 정보(닉네임, 프로필 이미지 URL)를 불러옴
            val isLoggedIn = prefs.getBoolean("IsLoggedIn", false)
            // 로그인 상태에 따라 닉네임 설정, 기본값은 "LOGIN"
            val nickname = if (isLoggedIn) prefs.getString("Nickname", "") else "LOGIN"

//            val profileImageUrl = prefs.getString("ProfileImageUrl", "")
//            Log.d("LoginActivity", "ProfileImageUrl Path: $profileImageUrl") // 로그 찍는 부분
//
//            val imagePath = prefs.getString("UserImageFilePath", null)
//            Log.d("LoginActivity", "ImageImageImageImage Path: $imagePath") // 로그 찍는 부분

            // 로그를 사용하여 profileImageUrl 확인
            // 사용자 닉네임으로 UI 업데이트
            binding.textLogin.text = nickname

            // 로그인 상태가 아닐 때만 로그인 페이지로 이동하도록 설정
            if (!isLoggedIn) {
                binding.placeBlock1.setOnClickListener {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            } else {
                // 로그인 상태일 경우, 클릭 리스너에 아무런 동작도 수행하지 않음
                binding.placeBlock1.setOnClickListener {
                    // 로그인 상태일 때는 아무 동작도 하지 않음
                }
            }
        }
    }

    // 날씨 보기 클릭 이벤트 처리
    private suspend fun handleWeatherViewClick() {
        withContext(Dispatchers.Main) {
            val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            // 로그인 상태 확인
            val isLoggedIn = prefs.getBoolean("IsLoggedIn", false)
            // 추가: 로그인 타입 확인
            val loginType = prefs.getString("LoginType", null)

            // 기존 로그인 상태 확인 조건에 더하여 로그인 타입이 구글, 카카오, 네이버 중 하나라도 해당되는지 확인
            val isAuthorizedUser =
                isLoggedIn && (loginType == "Google" || loginType == "Kakao" || loginType == "Naver" || loginType == "General")

            if (isAuthorizedUser) {
                // 로그인되어 있고, 구글/카카오/네이버 중 하나로 로그인되어 있을 경우 FragmentActivity로 이동
                val intent = Intent(this@MainActivity, FragmentActivity::class.java)
                startActivity(intent)
            } else {
                // 로그인 되어 있지 않거나 위 조건을 만족하지 않을 경우, 로그인 요청 메세지 표시
                Toast.makeText(this@MainActivity, "로그인 후 이용 가능합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 액티비티 종료 시 호출, Coroutine 작업 취소
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Coroutine 작업을 취소하여 리소스 방출
    }
}