package com.example.oktodo.AirAndWeather.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit을 사용하여 API 호출을 위한 기본 설정을 담고 있는 클래스
 * 동반 객체(companion object)를 활용하여 Retrofit 인스턴스를 싱글톤으로 관리
 */
class RetrofitConnection {
    companion object {
        // API의 기본 URL을 상수로 정의
        private const val BASE_URL = "https://api.airvisual.com/v2/"

        // Retrofit 인스턴스를 저장하기 위한 변수, 초기값은 null
        private var INSTANCE: Retrofit? = null

        /**
         * Retrofit 인스턴스를 반환하는 메서드, 인스턴스가 이미 생성되어 있으면 기존 인스턴스를 반환하고,
         * 그렇지 않은 경우 새로운 인스턴스를 생성하여 반환
         *
         * @return Retrofit의 인스턴스
         */
        fun getInstance(): Retrofit {
            // INSTANCE가 null인 경우 새로운 Retrofit 인스턴스를 생성
            if (INSTANCE == null) {
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL) // 기본 URL 설정
                    .addConverterFactory(GsonConverterFactory.create()) // JSON 응답을 자바 객체로 변환하기 위한 GsonConverterFactory 추가
                    .build()
            }
            // 생성된 인스턴스를 반환, !! 연산자는 INSTANCE가 절대 null이 아님을 단언
            return INSTANCE!!
        }
    }
}