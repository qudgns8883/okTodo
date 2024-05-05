package com.example.oktodo.Login

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.ActivityLoginBinding
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.MemberDao
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    // 레이아웃 및 데이터베이스 관련 멤버 변수
    private lateinit var binding: ActivityLoginBinding
    lateinit var db: AppDatabase
    lateinit var memberDao: MemberDao

    private val tag = "LoginActivity"
    private val rcSignIn = 100

    // Google 로그인 관련 멤버 변수
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    // 이메일 로그인 콜백
    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Toast.makeText(this@LoginActivity, "카카오 로그인 실패!", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            Toast.makeText(this@LoginActivity, "카카오 로그인 성공!", Toast.LENGTH_SHORT).show()
            loggedInSuccess(token)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layout binding 초기화
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "keyhash : ${Utility.getKeyHash(this)}")
        db = AppDatabase.getInstance(this)!!
        memberDao = db.getMemberDao()

        // 파이어베이스 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance()

        // 회원가입 버튼 클릭 이벤트
        binding.buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 홈 버튼 클릭 이벤트
        binding.homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 일반 로그인 버튼 클릭 이벤트
        binding.loginButton.setOnClickListener {
            lifecycleScope.launch {
                login()
            }
        }

        // 카카오 로그인 버튼 클릭 이벤트
        binding.buttonKakaoLogin.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                kakaoLoginWithTalk()
            } else {
                kakaoLoginWithAccount()
            }
        }

        // 구글 로그인에 필요한 옵션을 설정
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_login_client_id))
            .requestEmail()
            .build()

        // GoogleSignInClient 인스턴스를 초기화
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // 구글 로그인 버튼 클릭 이벤트
        binding.buttonGoogleLogin.setOnClickListener {
            signIn() // 로그인 프로세스 시작
        }

        // 네이버 로그인 버튼 클릭 이벤트
        binding.buttonNaverLogin.setOnClickListener {
            startNaverLogin()
        }
    }

    // ================== 네이버 소셜 로그인 ================
    // 네이버 로그인 시작 함수
    private fun startNaverLogin() {
        // 프로필 콜백 객체 생성
        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                // 로그인 성공 메시지 출력
                Toast.makeText(this@LoginActivity, "네이버 로그인 성공!", Toast.LENGTH_SHORT).show()

                // 닉네임과 프로필 이미지 URL 추출
                val nickname = response.profile?.nickname ?: "Unknown"
                val profileImage = response.profile?.profileImage ?: ""
                val email = response.profile?.email ?: ""

                if (nickname != null && profileImage != null) {
                    // ChosenActivity로 이동하고 닉네임과 프로필 이미지 URL 전달
                    val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
                    prefs.putBoolean("IsLoggedIn", true)
                    prefs.putString("Nickname", nickname) // 닉네임 저장
                    prefs.putString("ProfileImageUrl", profileImage) // 프로필 이미지 URL 저장
                    prefs.putString("Email", email) // 이메일 정보 저장
                    prefs.putString("LoginType", "Naver") // 로그인 타입 저장
                    prefs.apply()

                    // ChosenActivity로 넘어가기
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                showToastError("네이버 로그인 실패: $message")
            }

            override fun onError(errorCode: Int, message: String) {
                showToastError("네이버 로그인 오류: $message")
            }
        }

        // OAuth 로그인 콜백 객체 생성
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 사용자 정보 요청
                NidOAuthLogin().callProfileApi(profileCallback)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                // OAuth 로그인 실패 시 메시지 출력
                showToastError("OAuth 로그인 실패: $message")
            }

            override fun onError(errorCode: Int, message: String) {
                // OAuth 로그인 오류 시 메시지 출력
                showToastError("OAuth 로그인 오류: $message")
            }
        }

        // 네이버 로그인 SDK를 이용해 로그인 시도
        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }

    // 에러 메시지 출력하는 함수
    private fun showToastError(message: String) {
        // 토스트 메시지를 이용해 에러 메시지 표시
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    // ============= 구글 소셜 로그인 ============
    // 로그인 인텐트를 시작하는 함수
    private fun signIn() {
        // GoogleSignInClient를 이용해 로그인 인텐트를 생성하고, startActivityForResult로 결과를 요청
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIn) // REQ_CODE_SIGN_IN은 요청 코드 상수
    }

    // 로그인 결과를 처리하는 콜백 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcSignIn) { // Google 로그인 요청 코드인지 확인
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task) // 로그인 결과를 처리
        }
    }

    // 사용자가 Google 로그인을 성공한 후, 그 결과를 처리하는 함수
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!) // Firebase 인증 프로세스로 넘어감
            }
        } catch (e: ApiException) {
            // 로그인 실패 시 로그
            Log.e(tag, "signInResult:failed code=" + e.statusCode)
        }
    }

    // Firebase를 통한 로그인 처리 함수
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인에 성공했을 경우 UI를 업데이트
                    Log.d(tag, "signInWithCredential:success")
                    Toast.makeText(this@LoginActivity, "구글 로그인 성공!", Toast.LENGTH_SHORT).show()
                    val user = mAuth.currentUser
                    googleUpdateUI(user) // UI 업데이트
                } else {
                    // 로그인 실패 시 사용자에게 알림
                    Log.w(tag, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@LoginActivity, "구글 로그인 실패!", Toast.LENGTH_SHORT).show()
                    googleUpdateUI(null) // 실패 UI 업데이트
                }
            }
    }

    // 구글 로그인 후, 사용자 정보에 따른 UI 업데이트 함수
    private fun googleUpdateUI(user: FirebaseUser?) {
        if (user != null) {
            // 구글 로그인 성공 시, 로그와 함께 사용자 정보를 저장
            Log.d(tag, "updateUI: Login success with ${user.displayName}")

            if (user.displayName != null && user.photoUrl != null) {
                // SharedPreferences를 사용해 사용자 정보를 저장
                val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
                prefs.putBoolean("IsLoggedIn", true)
                prefs.putString("Nickname", user.displayName) // 닉네임 저장
                prefs.putString("ProfileImageUrl", user.photoUrl.toString()) // 프로필 이미지 URL 저장
                prefs.putString("Email", user.email ?: "") // 이메일 저장, 이메일이 null일 경우 빈 문자열 저장
                prefs.putString("LoginType", "Google") // 로그인 타입 저장
                prefs.apply()

                // ChosenActivity로 넘어가기
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // 사용자 정보가 없는 경우 로그인 실패 처리
                Log.w(tag, "updateUI: No user info available after login.")
                Toast.makeText(this, "로그인에 실패하였습니다: 사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ================= 카카오 소셜 로그인 ===============
    // 카카오톡으로 로그인 시도
    private fun kakaoLoginWithTalk() {
        UserApiClient.instance.loginWithKakaoTalk(this, callback = mCallback)
    }

    // 카카오 계정으로 로그인 시도
    private fun kakaoLoginWithAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
    }

    // 로그인 성공 처리
    private fun loggedInSuccess(token: OAuthToken) {
        // 카카오톡 사용자 정보 가져오기만 하고 화면 전환은 fetchKakaoUserInfo 함수에서 처리
        fetchKakaoUserInfo()
    }

    // 카카오톡 사용자 정보 가져오기
    private fun fetchKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                // Shared Preferences에 로그인 상태, 닉네임, 프로필 이미지 URL 저장
                val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
                prefs.putBoolean("IsLoggedIn", true)
                prefs.putString("Nickname", user.kakaoAccount?.profile?.nickname) // 닉네임 저장
                prefs.putString(
                    "ProfileImageUrl",
                    user.kakaoAccount?.profile?.profileImageUrl
                ) // 프로필 이미지 URL 저장
                prefs.putString("Email", user.kakaoAccount?.email) // 이메일 정보 저장
                prefs.putString("LoginType", "Kakao") // 로그인 타입 저장
                prefs.apply()

                // 사용자 정보 요청이 성공한 후 ChosenActivity로 이동
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    // =========== 일반 로그인 =============
    // 로그인
    private suspend fun login() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val member = memberDao.findMemberByEmail(email)

        if (member != null && member.password == password) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()

                // 로그인 성공 시, 닉네임 SharedPreferences에 저장
                val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit()
                val encodedImage = Base64.encodeToString(member.image, Base64.DEFAULT)
                sharedPreferences.putBoolean("IsLoggedIn", true) // 로그인 상태 저장
                sharedPreferences.putString("Nickname", member.nickName)
                sharedPreferences.putString("Email", member.email)
                sharedPreferences.putString("Password", member.password)
                sharedPreferences.putString("ProfileImageUrl", encodedImage)
                sharedPreferences.putString("mno", member.mno.toString())
                sharedPreferences.putString("LoginType", "General") // 로그인 타입 저장
                sharedPreferences.apply()

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
