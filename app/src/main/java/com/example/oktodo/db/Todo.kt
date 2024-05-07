package com.example.oktodo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalTime
import java.util.Calendar

@Entity(
    tableName = "todo",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["mno"],
            childColumns = ["mno"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Todo(
    @ColumnInfo(name = "mno")
    var mno: String,

    @ColumnInfo(name = "todo_content")
    var todoContent: String,

    @ColumnInfo(name = "importance")
    var importance: String,

    @ColumnInfo(name = "checked")
    var checked: Boolean = false,

    @ColumnInfo(name = "day")
    var day: String,

    @ColumnInfo(name = "date")
    var date: Long = Calendar.getInstance().timeInMillis,

    @ColumnInfo(name = "todoTime")
    var todoTime: Long,

    @ColumnInfo(name = "todo_reg_time")
    var todoRegTime: LocalTime,

    ) {
    @PrimaryKey(autoGenerate = true) // autoGenerate 옵션 : 기본키를 직접 지정하지 않아도 자동으로 증가
    var tno: Long = 0
}

@Entity(
    tableName = "todo2",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["mno"],
            childColumns = ["mno"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Todo2(

    @ColumnInfo(name = "tno2")
    var tno2: Long,

    @ColumnInfo(name = "checkedCnt")
    var checkedCnt: Long,

    @ColumnInfo(name = "uncheckedCnt")
    var uncheckedCnt: Long,

    @ColumnInfo(name = "calculate")
    var calculate: Long,

    @ColumnInfo(name = "day")
    var day: Long,

    @ColumnInfo(name = "date")
    var date: Long,

    @ColumnInfo(name = "mno")
    var mno: String
) {
    @PrimaryKey(autoGenerate = true)
    var tno: Long = 0
}