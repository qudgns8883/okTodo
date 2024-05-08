package com.example.oktodo.forum

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.oktodo.db.AppDatabase
import com.example.oktodo.db.Forum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.util.Date

class ForumMainViewModel(application: Application) : AndroidViewModel(application) {

    // Room 데이터베이스
    private val db: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "db_todo"
    ).build()

    private val _items = MutableStateFlow<List<Forum>>(emptyList())
    val items: StateFlow<List<Forum>> = _items
    var selectedForum: Forum? = null

    private val _updateComplete = MutableStateFlow(false)
    val updateComplete: StateFlow<Boolean> = _updateComplete

    // 프래그먼트 4에 대한 쿼리(전체 탭)
    fun queryForFragment1() {
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getAll().collect { forums ->
                _items.value = forums
            }
        }
    }

    // 프래그먼트 2에 대한 쿼리(교통 탭)
    fun queryForFragment2() {
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getAllT().collect { forums ->
                _items.value = forums
            }
        }
    }

    // 프래그먼트 3에 대한 쿼리(날씨 탭)
    fun queryForFragment3() {
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getAllW().collect { forums ->
                _items.value = forums
            }
        }
    }

    // 프래그먼트 4에 대한 쿼리(검색)
    fun queryForFragment4(searchText: String) {
        Log.d("test", "test::::::searchText ::: $searchText")
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao().getSearch(searchText).collect { forums ->
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

    fun addForum(
        mno: String,
        text: String,
        forumTime: Date,
        forumPlace1: String,
        forumPlace2: String,
        forumCategory: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.forumDao()
                .insert(Forum(mno, text, forumTime, forumPlace1, forumPlace2, forumCategory))
        }
    }

    fun updateForum(
        text: String,
        cno: Long,
        forumPlace1: String,
        forumPlace2: String,
        forumCategory: String
    ) {
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