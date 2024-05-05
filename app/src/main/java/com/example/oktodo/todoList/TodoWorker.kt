package com.example.oktodo.todoList

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.Todo2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class TodoWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 데이터베이스 인스턴스 가져오기
        val db = AppDatabase.getInstance2(applicationContext)
        val todayWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toLong()

        // 비동기 작업으로 데이터베이스 값 가져오기 + 계산
        CoroutineScope(Dispatchers.IO).launch {
            val checkCnt = db.todoDao().getCheckedCount()
            val uncheckCnt = db.todoDao().getUncheckedCount()
            val totalCount = checkCnt + uncheckCnt
            val calculate = if (totalCount > 0) ((checkCnt.toDouble() / totalCount) * 100).toLong() else 0

            // 새로운 할 일 객체 생성 및 초기화
            val todo2Item = Todo2(
                checkedCnt = checkCnt, // 체크된 항목 수 초기화
                uncheckedCnt = uncheckCnt, // 체크되지 않은 항목 수 초기화
                day = todayWeek,
                tno2 = getStartOfDayInMillis(), // 생성 시간 설정
                calculate = calculate
            )

            // 데이터베이스에 값 추가
            db.todoDao().insert2(todo2Item)
        }

        return Result.success()
    }

    // 해당 일자의 시작 시간을 반환하는 함수
    private fun getStartOfDayInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}