package com.example.oktodo.util.menuClickListener

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.oktodo.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.navercorp.nid.NaverIdLoginSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthManager {
    fun performLogout(context: Context) {
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        CoroutineScope(Dispatchers.Main).launch {
            signOut(context)
            // SharedPreferences에서 사용자 정보 삭제
            prefs.edit().apply {
                putBoolean("IsLoggedIn", false)
                remove("LoginType")
                remove("Nickname")
                remove("ProfileImageUrl")
                apply()
            }
            // 로그아웃 메시지를 표시하고 메인 액티비티로 이동
            Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

     fun signOut(context: Context) {
        val googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
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
            Log.e("AuthManager", "Context is not an instance of Activity.")
        }
    }
}