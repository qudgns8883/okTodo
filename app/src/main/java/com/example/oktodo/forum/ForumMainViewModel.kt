package com.example.oktodo.forum

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.Forum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ForumMainViewModel(application: Application) : AndroidViewModel(application) {

    // Room 데이터베이스
    private val db: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "forum"
    ).build()

    private val _items = MutableStateFlow<List<Forum>>(emptyList())
    val items: StateFlow<List<Forum>> = _items
    var selectedForum: Forum? = null

    private val _updateComplete = MutableStateFlow(false)
    val updateComplete: StateFlow<Boolean> = _updateComplete

//    init {
//        // ViewModel에서 데이터베이스 작업을 비동기적으로 처리
//        viewModelScope.launch(Dispatchers.IO) {
//            // Flow 객체는 collect로 현재 값을 가져올 수 있음
//            // getAll() 메서드는 Folw<List<<>> 타입을 반환함
//            db.forumDao().getAll().collect { forums ->
//                // StateFlow 객체는 value 프로퍼티로 현재 상태값을 읽거나 쓸 수 있음
//                _items.value = forums
//            }
//        }
//    }

    // 프래그먼트 1에 대한 쿼리
    fun queryForFragment1() {
        Log.d("test", "queryForFragment1() 호출됨")
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getAll().collect { forums ->
                _items.value = forums
            }
        }
    }

    // 프래그먼트 2에 대한 쿼리
    fun queryForFragment2() {
        Log.d("test", "queryForFragment2() 호출됨")
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getAllT().collect { forums ->
                _items.value = forums
            }
        }
    }

    // 프래그먼트 3에 대한 쿼리
    fun queryForFragment3() {
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getAllW().collect { forums ->
                _items.value = forums
            }
        }
    }

    fun loadForumData() {
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getAll().collect { newForums ->
                _items.value = newForums
            }
        }
    }

    fun addForum(text: String, forumTime: Date, forumPlace1: String, forumPlace2: String, forumCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().insert(Forum(text, forumTime, forumPlace1, forumPlace2, forumCategory))
        }
    }

    fun updateForum(text: String, cno: Long, forumPlace1: String, forumPlace2: String, forumCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 데이터베이스에서 엔티티를 찾음
            val forumToUpdate = db.forumDao().getForumById(cno)
            // 엔티티가 존재하는 경우에만 업데이트를 시도함
            forumToUpdate?.let { forum ->
                // 엔티티 수정
                forum.forumContent = text
                forum.forumCategory = forumCategory
                forum.forumPlace1 = forumPlace1
                forum.forumPlace2 = forumPlace2
                // 데이터베이스에 업데이트
                db.forumDao().update(forum)
//                // 선택된 포럼 초기화
//                selectedForum = null
                _updateComplete.value = true
            }
        }
    }

    fun deleteForum(cno: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val forumToDelete = db.forumDao().getForumById(cno)
            forumToDelete?.let { forum ->
                db.forumDao().delete(forum)
                selectedForum = null
            }
        }
    }
}