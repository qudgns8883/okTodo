package com.example.oktodo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo ORDER BY checked ASC, todoTime ASC")
    fun getAll(): Flow<List<Todo>>

    @Query("SELECT * FROM todo WHERE day = :day ORDER BY checked ASC, todoTime ASC")
    fun getTodoByDay(day: String): Flow<List<Todo>>

    // 체크, 미체크 cnt
    @Query("SELECT COUNT(*) FROM todo WHERE checked = true")
    suspend fun getCheckedCount(): Long
    @Query("SELECT COUNT(*) FROM todo WHERE checked = false")
    suspend fun getUncheckedCount(): Long

    @Query("SELECT * FROM todo2 WHERE tno2 >= :startDayOfWeek AND tno2 <= :endDayOfWeek")
    fun getThisWeekTodos(startDayOfWeek: Long, endDayOfWeek: Long): Flow<List<Todo2>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Todo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert2(todo2: Todo2)

    @Update
    suspend fun update(entity: Todo)

    @Delete
    suspend fun delete(entity: Todo)
}