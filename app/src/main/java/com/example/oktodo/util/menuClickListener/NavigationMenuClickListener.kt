package com.example.oktodo.util.menuClickListener

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.oktodo.HelpActivity
import com.example.oktodo.Login.LoginActivity
import com.example.oktodo.MainActivity
import com.example.oktodo.NoticeActivity
import com.example.oktodo.R
import com.example.oktodo.myPage.MyPage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.navercorp.nid.NaverIdLoginSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NavigationMenuClickListener(private val context: Context) :
    NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mypage -> {
                if (isLoggedIn()) {
                    // 로그인 상태일 경우, 마이페이지로 이동
                    context.startActivity(Intent(context, MyPage::class.java))
                } else {
                    // 로그인 상태가 아닐 경우, 로그인 페이지로 이동하도록 설정
                    context.startActivity(Intent(context, LoginActivity::class.java))
                }
                return true
            }

            R.id.help -> {
                // 도움말 페이지로 이동
                context.startActivity(Intent(context, HelpActivity::class.java))
                return true
            }

            R.id.notice -> {
                // 공지사항 페이지로 이동
                context.startActivity(Intent(context, NoticeActivity::class.java))
                return true
            }

            R.id.logout -> {
                if (!isLoggedIn()) {
                    // 이미 로그아웃 상태인 경우, 로그인 액티비티로 이동
                    context.startActivity(Intent(context, LoginActivity::class.java))
                } else {
                    // 로그인 상태인 경우, 로그아웃 처리 후 로그인 액티비티로 이동
                    AuthManager.performLogout(context)
                    return true
                }
            }
        }
        return false
    }

    // 로그인 상태 확인 로직 구현
    private fun isLoggedIn(): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("IsLoggedIn", false)
    }

    // 로그아웃 처리를 수행하고 UI를 업데이트하는 함수
    private fun performLogout(prefs: SharedPreferences) {
        CoroutineScope(Dispatchers.Main).launch {
                // Google 로그아웃
                signOut()
            }
            // SharedPreferences에서 사용자 정보 삭제
            prefs.edit().apply {
                putBoolean("IsLoggedIn", false)
                remove("LoginType")
                remove("Nickname")
                remove("ProfileImageUrl")
                apply()
            }
            // UI 업데이트를 위한 함수 호출, 메인 액티비티로 이동
            updateUIAndNavigateToMain()
        }

    // 로그아웃 메시지 뛰운후 MainActivity로 이동
    private fun updateUIAndNavigateToMain() {
        // 로그아웃 알림 표시하고 메인 액티비티로 이동
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    // Google 로그아웃 처리
    private fun signOut() {
        val googleSignInClient =
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
        if (context is Activity) {
            googleSignInClient.signOut().addOnCompleteListener(context) {
                // Google 로그아웃 완료 후에는 FirebaseAuth 로그아웃 처리
                FirebaseAuth.getInstance().signOut()
                // Naver 로그아웃 처리
                CoroutineScope(Dispatchers.IO).launch {
                    NaverIdLoginSDK.logout()
                }
            }
        } else {
            // context가 Activity가 아닐 경우 처리
            Log.e("NavigationMenuClickListener", "Context is not an instance of Activity.")
        }
    }

    // 네비게이션 메뉴 아이템의 텍스트 업데이트
    fun updateMenuText(navigationView: NavigationView) {
        val menu = navigationView.menu
        val loginMenuItem = menu.findItem(R.id.logout)

        if (isLoggedIn()) {
            // 로그인 상태일 경우
            loginMenuItem.title = "로그아웃"
        } else {
            // 로그인 상태가 아닐 경우
            loginMenuItem.title = "로그인"
        }
    }
}