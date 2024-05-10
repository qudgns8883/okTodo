package com.example.oktodo.todoList

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.R
import com.example.oktodo.util.adapter.TodoListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

//알람 및 BroadcastReceiver를 사용하여 다음 날이 되면 특정 작업을 수행하는 것으로 권한이 필요하지 않음
class TodoBackgroundActivity : AppCompatActivity() {

    private val ALARM_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 알람 설정
        setResetCheckBox()
    }

    private fun setResetCheckBox() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TodoBackgroundReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE) // Android 12에서 발생하는 오류가 해결)
        val currentTime = Calendar.getInstance() // 현재 시간

        val alarmTime = Calendar.getInstance()

        // 매일 정해진 시간에 알람 설정
        alarmTime.set(Calendar.HOUR_OF_DAY, 23)
        alarmTime.set(Calendar.MINUTE, 52)
        alarmTime.set(Calendar.SECOND, 0)

        // 알람 설정
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

        // 백그라운드 작업 완료 후 메인 액티비티로 돌아가기
        finish()
    }
}

class TodoBackgroundReceiver : BroadcastReceiver() {
    private lateinit var adapter: TodoListAdapter

    override fun onReceive(context: Context, intent: Intent) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                adapter.unCheckboxes() // 체크박스 비활성화 작업을 수행하는 메서드 호출
            }
        } catch (e: Exception) {
            // 예외 발생 시 로그 출력
            Log.e("test", "오류 발생: ${e.message}", e)
        }
    }
}