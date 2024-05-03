package com.example.oktodo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(Converters::class)
data class MemberEntity(
    // autoGenerate = true는 이 기본 키가 자동으로 생성되도록 지정
    @PrimaryKey(autoGenerate = true)
    var mno: Int? = null,

    // 회원의 이메일
    @ColumnInfo(name = "email")
    var email: String,
 
    // 회원의 비밀번호
    @ColumnInfo(name = "password")
    var password: String,

    // 회원의 닉네임
    @ColumnInfo(name = "nick_name")
    var nickName: String,

    //ByteArray형식으로 이미지 저장
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray
//
//    @Relation(parentColumn = "mno", entityColumn = "mno")
//    val todos: List<TodoEntity>? = null,
//
//    @Relation(parentColumn = "mno", entityColumn = "mno")
//    val communication: List<Communication>? = null

)