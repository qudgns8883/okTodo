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
    @Query("SELECT * FROM todo WHERE mno = :mno ORDER BY checked ASC, todoTime ASC")
    fun getAll(mno: String): Flow<List<Todo>>

    @Query("SELECT * FROM todo WHERE day = :day ORDER BY checked ASC, todoTime ASC")
    fun getTodoByDay(day: String): Flow<List<Todo>>

    // 체크, 미체크 cnt
    @Query("SELECT COUNT(*) FROM todo WHERE checked = true AND mno = :mno AND day LIKE '%' || :day || '%'")
    suspend fun getCheckedCount(mno: String, day: String): Long
    @Query("SELECT COUNT(*) FROM todo WHERE checked = false AND mno = :mno AND day LIKE '%' || :day || '%'")
    suspend fun getUncheckedCount(mno: String, day: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Todo)

    @Update
    suspend fun update(entity: Todo)

    @Delete
    suspend fun delete(entity: Todo)

    @Query("SELECT * FROM todo2 WHERE tno2 >= :startDayOfWeek AND tno2 <= :endDayOfWeek AND mno = :mno ORDER BY date DESC")
    fun getThisWeekTodos(startDayOfWeek: Long, endDayOfWeek: Long, mno: String): Flow<List<Todo2>>

    @Query("SELECT * FROM todo2 WHERE tno2 = :tno2 AND mno = :mno")
    suspend fun findTodo2ByKeys(tno2: Long, mno: String): Todo2?

    @Insert
    suspend fun insert2(todo2: Todo2)

    @Update
    suspend fun update2(entity: Todo2)


}