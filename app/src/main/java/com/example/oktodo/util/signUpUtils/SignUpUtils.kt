package com.example.oktodo.util.signUpUtils

import android.content.Intent
import android.graphics.Bitmap
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.oktodo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

// 회원가입 관련 유틸리티 클래스
object SignUpUtils {

    // URI에서 바이트 배열로 변환하는 함수
    suspend fun convertUriToByteArray(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                    ByteArrayOutputStream().use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.toByteArray()
                    }
                } ?: byteArrayOf().also {
                    Log.e("convertUriToByteArray", "Bitmap conversion failed")
                }
            } ?: byteArrayOf().also {
                Log.e("convertUriToByteArray", "InputStream is null")
            }
        } catch (e: FileNotFoundException) {
            Log.e("convertUriToByteArray", "File not found", e)
            byteArrayOf()
        }
    }

    // 기본 이미지를 바이트 배열로 변환하는 함수
    suspend fun getDefaultImageByteArray(context: Context): ByteArray = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.profileee)
            ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
        } catch (e: Exception) {
            Log.e("getDefaultImage", "Default image loading failed", e)
            byteArrayOf()
        }
    }

    // 비밀번호 유효성 검사 함수
    fun isValidPassword(password: String): Boolean = password.length >= 6

    // 이메일 유효성 검사 함수
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }

    // 입력값 유효성 검사 함수
    fun validateInputs(context: Context, email: String, password: String, nickName: String): Boolean {
        when {
            email.isEmpty() || password.isEmpty() || nickName.isEmpty() -> {
                showToast(context, "모든 정보를 입력해주세요.")
                return false
            }

            !isValidEmail(email) -> {
                showToast(context, "올바른 이메일 주소를 입력해주세요.")
                return false
            }

            !isValidPassword(password) -> {
                showToast(context, "비밀번호는 6자 이상이어야 합니다.")
                return false
            }
            else -> return true
        }
    }

    // Toast 메시지 표시 함수
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 이미지 파일을 저장하고, 파일 경로를 SharedPreferences에 저장하는 함수
    fun saveImageAndPathInPreferences(context: Context, imageByteArray: ByteArray, nickName: String) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${nickName}_$timeStamp.jpg"

        // 파일을 내부 저장소에 저장
        val file = File(context.filesDir, fileName)
        file.writeBytes(imageByteArray)

        // 파일 경로를 SharedPreferences에 저장
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE).edit().apply {
            putString("UserImageFilePath", file.absolutePath)
            apply()
        }
    }
}