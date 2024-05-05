package com.example.oktodo.todoList

import android.content.Context
import androidx.room.Transaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.Todo2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class TodoWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    // mno 가져오기
    private val prefs =
        applicationContext.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    private val isLoggedIn = prefs.getBoolean("IsLoggedIn", false)
    private val mno = if (isLoggedIn) prefs.getString("mno", "").toString() else "default_value"

    // 데이터베이스 인스턴스 가져오기
    private val db = AppDatabase.getInstance2(applicationContext)

    private var selectedTodo2: Todo2? = null

    override suspend fun doWork(): Result {
        val todayWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toLong()

        // 비동기 작업으로 데이터베이스 값 가져오기 + 계산
        val checkCnt = db.todoDao().getCheckedCount(mno)
        val uncheckCnt = db.todoDao().getUncheckedCount(mno)
        val totalCount = checkCnt + uncheckCnt
        val calculate =
            if (totalCount > 0) ((checkCnt.toDouble() / totalCount) * 100).toLong() else 0

        // 새로운 할 일 객체 생성 및 초기화
        val todo2 = Todo2(
            tno2 = getStartOfDayInMillis(), // 생성 시간 설정
            checkedCnt = checkCnt, // 체크된 항목 수 초기화
            uncheckedCnt = uncheckCnt, // 체크되지 않은 항목 수 초기화
            calculate = calculate,
            date = Calendar.getInstance().timeInMillis,
            day = todayWeek,
            mno = mno,
        )
        // 데이터베이스에 값 추가
        insertOrUpdate(todo2)

        // 데이터베이스 작업이 완료될 때까지 기다림
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

    private fun add(
        tno2: Long,
        checkedCnt: Long,
        uncheckedCnt: Long,
        calculate: Long,
        day: Long,
        date: Long,
        mno: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            db.todoDao()
                .insert2(Todo2(tno2, checkedCnt, uncheckedCnt, calculate, day, date, mno))
        }
    }

    private fun update(
        checkedCnt: Long,
        uncheckedCnt: Long,
        calculate: Long,
        day: Long,
        date: Long,
    ) {
        selectedTodo2?.let { todo ->
            todo.apply {
                this.checkedCnt = checkedCnt
                this.uncheckedCnt = uncheckedCnt
                this.calculate = calculate
                this.day = day
                this.date = date
            }

            CoroutineScope(Dispatchers.IO).launch {
                db.todoDao().update2(todo)
            }
            selectedTodo2 = null
        }
    }

    @Transaction
    suspend fun insertOrUpdate(todo2: Todo2) {
        val existingTodo2 = db.todoDao().findTodo2ByKeys(todo2.tno2, todo2.mno)
        if (existingTodo2 != null) {
            // 이미 존재하는 경우 업데이트
            selectedTodo2 = existingTodo2 // existingTodo2를 selectedTodo2에 할당
            update(todo2.checkedCnt, todo2.uncheckedCnt, todo2.calculate, todo2.day, todo2.date)
        } else {
            // 존재하지 않는 경우 삽입
            add(
                todo2.tno2,
                todo2.checkedCnt,
                todo2.uncheckedCnt,
                todo2.calculate,
                todo2.day,
                todo2.date,
                todo2.mno
            )
        }
    }
}