package com.example.oktodo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// @Database 어노테이션은 안드로이드 Jetpack의 Room 라이브러리에서 사용되며, 데이터베이스를 정의하는 클래스에 이 어노테이션을 사용
// entities: TodoEntity 클래스를 데이터베이스의 엔티티로 포함시킴
// version = 2: 데이터베이스의 버전을 나타내는 부분
@Database(entities = [MemberEntity::class, Todo::class, Todo2::class, Forum::class], version = 2) // 조건 1
// RoomDatabase를 상속함으로써 AppDatabase 클래스는 Room 라이브러리의 데이터베이스와 관련된 기능들을 이용할 수 있게됨
@TypeConverters(TodoConverters::class)
abstract class AppDatabase : RoomDatabase() { // 조건 2

    // 추상 메서드인 getTodoDao()를 선언하고, 이 메서드가 TodoDao 인터페이스를 반환하도록 정의
//    abstract fun getTodoDao() : TodoDao // 조건 3
    abstract fun getMemberDao(): MemberDao
    abstract fun todoDao(): TodoDao
    abstract fun forumDao() : ForumDao


    // 클래스의 인스턴스를 만들지 않고도 해당 멤버에 접근할 수 있도록함 (싱글톤 패턴)
    companion object {
        val databaseName = "db_todo" // 데이터베이스 이름
        var appDatabase : AppDatabase? = null

        // Context를 매개변수로 받아들이고, AppDatabase 또는 null을 반환하는 메서드
        // 컨텍스트(Context)는 안드로이드 애플리케이션의 현재 상태와 환경 정보에 대한 접근을 제공하는 클래스
        fun getInstance(context : Context) : AppDatabase? {
            // appDatabase가 null인 경우에만 데이터베이스를 빌드하고, 그렇지 않은 경우에는 기존의 데이터베이스를 반환
            if (appDatabase == null) {
                // Room.databaseBuilder는 Room 데이터베이스를 생성하기 위한 빌더를 생성
                appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                    // 데이터베이스의 버전이 변경되었을 때, 데이터를 보존하지 않고 새로운 스키마로 데이터베이스를 재구성하는 옵션
                    .fallbackToDestructiveMigration().build()
            }
            return appDatabase
        }

        // TodoWorker 에서 사용
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance2(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "db_todo"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}