package com.example.oktodo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface MemberDao {

    //모든회원 조회
    @Query("SELECT * FROM MemberEntity")
    suspend fun getAllMembers(): List<MemberEntity>

    // 삽입
    @Insert
    suspend fun insertMember(member: MemberEntity)

    // 수정
    @Update
    suspend fun updateMember(member: MemberEntity)

    // 삭제
    @Delete
    suspend fun deleteMember(member: MemberEntity)

    // 중복 이메일을 체크
    @Query("SELECT * FROM MemberEntity WHERE email = :email")
    suspend fun findMemberByEmail(email: String): MemberEntity?

    // 특정 회원 번호에 해당하는 MemberEntity를 반환
    @Query("SELECT * FROM MemberEntity WHERE mno = :mno")
    suspend fun getMemberById(mno: Int): MemberEntity

    @Query("SELECT image FROM MemberEntity WHERE mno = :userId")
    suspend fun getMemberImage(userId: Int): ByteArray? // 특정 ID에 해당하는 MemberEntity의 이미지를 byte 배열로 가져오는 함수

    // MemberEntity 타입으로 가져오면 일대다 형성으로 인해 TodoEntity, CommunityEntity 를 같이 가져올수있기 떄문에 Transaction을 사용
    // suspend 키워드는 해당 메서드가 일시 중단 함수임을 나타냄, 일시 중단 가능한 함수로써, 코루틴 내에서 호출될 수 있음을 의미
    // 따라서 이 메서드를 호출하면 MemberEntity 테이블의 모든 행이 포함된 리스트를 얻을 수 있음
    @Transaction
    @Query("SELECT * FROM MemberEntity")
    suspend fun getMembersWithTodosAndCommunities(): List<MemberEntity>
}