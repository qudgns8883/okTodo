package com.example.oktodo.AirAndWeather.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit 라이브러리를 사용하여 RESTful API 호출을 위한 인터페이스 정의.
 * 이 인터페이스를 통해 서버의 특정 엔드포인트(endpoint)에 대한 HTTP 요청을 구성할 수 있음
 */
interface AirQualityService {
    /**
     * 가장 가까운 도시의 대기질 정보를 가져오는 GET 요청을 정의합
     *
     * @param lat 위도 값으로, 요청할 위치의 위도
     * @param lon 경도 값으로, 요청할 위치의 경도
     * @param key API 키 값으로, 요청을 인증하기 위해 필요한 사용자의 API 키
     * @return Call<AirQualityResponse> 객체로, 요청의 응답을 비동기적으로 처리
     */
    @GET("nearest_city")
    fun getAirQualityData(
        @Query("lat") lat: String, // 위도를 의미
        @Query("lon") lon: String, // 경도를 의미
        @Query("key") key: String  // 사용자의 API 키
    ): Call<AirQualityResponse> // Retrofit의 Call 객체를 반환하여, 요청에 대한 응답을 처리
}