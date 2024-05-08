package com.example.oktodo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Query("SELECT * FROM forum ORDER BY cno DESC")
    fun getAll(): Flow<List<Forum>>

    @Query("SELECT * FROM forum WHERE forum_category = '교통' ORDER BY cno DESC")
    fun getAllT(): Flow<List<Forum>>

    @Query("SELECT * FROM forum WHERE forum_category = '날씨' ORDER BY cno DESC")
    fun getAllW(): Flow<List<Forum>>

    @Query("SELECT * FROM forum WHERE (forum_content LIKE '%' || :searchText || '%') OR (forum_place1 LIKE '%' || :searchText || '%') OR (forum_place2 LIKE '%' || :searchText || '%')")
    fun getSearch(searchText: String): Flow<List<Forum>>

    @Query("SELECT * FROM forum WHERE cno = :cno")
    suspend fun getForumById(cno: Long): Forum?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Forum)

    @Update
    suspend fun update(entity: Forum)

    @Delete
    suspend fun delete(entity: Forum)
}