package com.example.oktodo.todoList

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.oktodo.R
import android.content.SharedPreferences
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.Todo
import com.example.oktodo.db.Todo2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.util.Calendar

//AndroidViewModel은 액티비티와 수명을 같이함
class TodoMainViewModel(application: Application) : AndroidViewModel(application) {
    // mno 가져오기
    private val prefs = application.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    private val isLoggedIn = prefs.getBoolean("IsLoggedIn", false)
    val mno = if (isLoggedIn) prefs.getString("mno", "").toString() else "default_value"

    // Room 데이터베이스
    private val db: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "db_todo"
    ).build()

    private val _items = MutableStateFlow<List<Todo>>(emptyList())
    val items: StateFlow<List<Todo>> = _items
    private val _items2 = MutableStateFlow<List<Todo2>>(emptyList())
    val items2: StateFlow<List<Todo2>> = _items2

    var selectedTodo: Todo? = null

    // 이모티콘 관련
    val calendar = Calendar.getInstance()
    val todayInMillis = calendar.timeInMillis
    val startDayOfWeek = calendar.apply {
        set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek) // 주의 첫 번째 요일
        add(Calendar.DAY_OF_MONTH, -6)
    }.timeInMillis
    val endDayOfWeek = todayInMillis

    init {
        // 해당 주의 Todo2를 가져옴
        viewModelScope.launch {
            db.todoDao().getThisWeekTodos(startDayOfWeek, endDayOfWeek, mno).collect { todos ->
                _items2.value = todos
            }
        }

        // ViewModel에서 데이터베이스 작업을 비동기적으로 처리
        viewModelScope.launch(Dispatchers.IO) {
            // Flow 객체는 collect로 현재 값을 가져올 수 있음
            // getAll() 메서드는 Folw<List<<>> 타입을 반환함
            db.todoDao().getAll(mno).collect { todos ->
                // StateFlow 객체는 value 프로퍼티로 현재 상태값을 읽거나 쓸 수 있음
                _items.value = todos
            }
        }
    }

    // Todo데이터를 가져와서 _items2 LiveData에 설정하는 작업
    fun loadThisWeekTodos(startDayOfWeek: Long, endDayOfWeek: Long) {
        viewModelScope.launch {
            db.todoDao().getThisWeekTodos(startDayOfWeek, endDayOfWeek, mno).collect { todos ->
                _items2.value = todos
            }
        }
    }

    fun addTodo(
        mno: String,
        text: String,
        importance: String,
        checked: Boolean,
        day: String,
        date: Long,
        todoTime: Long,
        todoRegTime: LocalTime
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.todoDao()
                .insert(Todo(mno, text, importance, checked, day, date, todoTime, todoRegTime))
        }
    }

    fun updateTodo(
        text: String,
        timeInMillis: Long,
        day: String,
        importance: String,
        todoTime: Long
    ) {
        selectedTodo?.let { todo ->
            todo.apply {
                this.todoContent = text
                this.day = day
                this.date = date
                this.importance = importance
                this.todoTime = todoTime
            }

            viewModelScope.launch(Dispatchers.IO) {
                db.todoDao().update(todo)
            }
            selectedTodo = null
        }
    }

    fun updateDoneChecked(todo: Todo, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            todo.checked = isChecked
            db.todoDao().update(todo)
        }
    }

    fun deleteTodo(tno: Long) {
        _items.value
            .find { todo -> todo.tno == tno }
            ?.let { todo ->
                viewModelScope.launch(Dispatchers.IO) {
                    db.todoDao().delete(todo)
                }
                selectedTodo = null
            }
    }
}