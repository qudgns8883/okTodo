package com.example.oktodo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Forum(
    //    @ColumnInfo(name = "mno")
    //    var mno: String,

    @ColumnInfo(name = "forum_content")
    var forumContent: String,

    @ColumnInfo(name = "forum_time")
    var forumTime: Date,

    @ColumnInfo(name = "forum_place1")
    var forumPlace1: String,

    @ColumnInfo(name = "forum_place2")
    var forumPlace2: String,

    @ColumnInfo(name = "forum_category")
    var forumCategory: String
) {
    @PrimaryKey(autoGenerate = true)
    var cno: Long = 0
}