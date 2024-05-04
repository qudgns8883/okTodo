package com.example.oktodo.util.menuClickListener

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.oktodo.AirAndWeather.FragmentActivity
import com.example.oktodo.R
import com.example.oktodo.forum.ForumMainActivity
import com.example.oktodo.metro.MetroActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.File

object CardViewClickListener {
    fun setupCardViewClickListeners(
        headerView: View,
        context: Context,
        lifecycleOwner: LifecycleOwner
    ) {
        lifecycleOwner.lifecycleScope.launch {
            val sharedPreferences =
                context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val loginType = sharedPreferences.getString("LoginType", "none")
            val userNickName = sharedPreferences.getString("Nickname", "로그인 해주세요.")
            val imageUrl = sharedPreferences.getString("ProfileImageUrl", null)
            val imagePath = sharedPreferences.getString("UserImageFilePath", null)

            // 사용자 이미지와 닉네임을 업데이트하는 로직
            val imageView = headerView.findViewById<CircleImageView>(R.id.myPageImage)
            val textView = headerView.findViewById<TextView>(R.id.nickName)
            textView.text = userNickName
            Log.e("ProfileImageUrl", "$imageView")

            when (loginType) {
                "Kakao", "Google", "Naver" -> {
                    Glide.with(context).load(imageUrl).into(imageView)
                }

                "General" -> {
                    if (imagePath != null) {
                        // 저장된 이미지 경로에서 비트맵을 디코딩
                        Glide.with(context).load(File(imagePath)).error(R.drawable.profileee)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.profileee) // 기본 이미지 설정
                    }
                }

                else -> imageView.setImageResource(R.drawable.profileee)
            }

            // 헤더 뷰에서 CardView 찾기
            val weather = headerView.findViewById<CardView>(R.id.weather_view)
            val todo = headerView.findViewById<CardView>(R.id.todo_view)
            val sub = headerView.findViewById<CardView>(R.id.sub_view)
            val comm = headerView.findViewById<CardView>(R.id.comm_view)

            // 각 CardView에 대한 클릭 리스너를 설정
            weather.setOnClickListener {
                context.startActivity(
                    Intent(
                        context,
                        FragmentActivity::class.java
                    )
                )
            }
            // todo.setOnClickListener { context.startActivity(Intent(context, TodoMainActivity::class.java)) }
            sub.setOnClickListener {
                context.startActivity(
                    Intent(
                        context,
                        MetroActivity::class.java
                    )
                )
            }
            comm.setOnClickListener {
                context.startActivity(
                    Intent(
                        context,
                        ForumMainActivity::class.java
                    )
                )
            }
        }
    }
}