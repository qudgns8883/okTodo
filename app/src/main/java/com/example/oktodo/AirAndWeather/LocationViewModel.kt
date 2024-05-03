package com.example.oktodo.AirAndWeather

import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.oktodo.AirAndWeather.retrofit.AirQualityResponse
import com.example.oktodo.AirAndWeather.retrofit.AirQualityService
import com.example.oktodo.AirAndWeather.retrofit.RetrofitConnection
import retrofit2.Call

// 위치 정보를 저장하고 공유하기 위한 ViewModel
class LocationViewModel : ViewModel() {

    // 오류 메시지 LiveData
    val errorMessage = MutableLiveData<String>()

    // 주소 텍스트 LiveData
    val addressTextLiveData = MutableLiveData<String>()

    // 위치에 대한 부가 설명 LiveData
    val locationSubtitleLiveData = MutableLiveData<String>()

    // 공기질 데이터 LiveData
    private val _airQualityData = MutableLiveData<AirQualityResponse>()
    val airQualityData: LiveData<AirQualityResponse> get() = _airQualityData

    // 날씨 데이터 LiveData
    private val _weatherQualityData = MutableLiveData<AirQualityResponse.Data.Current.Weather>()
    val weatherQualityData: LiveData<AirQualityResponse.Data.Current.Weather> get() = _weatherQualityData

    // 위도와 경도
    var latitude: Double? = null
    var longitude: Double? = null

    // 공기질 데이터 업데이트
    fun updateAirQuality(airQualityResponse: AirQualityResponse) {
        _airQualityData.value = airQualityResponse
    }

    // 날씨 데이터 업데이트
    fun updateWeatherQuality(weatherQualityResponse: AirQualityResponse.Data.Current.Weather) {
        _weatherQualityData.value = weatherQualityResponse
    }

    // 사용자의 현재 위치 기반으로 주소 정보와 공기 질 데이터를 가져와 UI를 업데이트하는 기능
    fun updateUI(
        locationProvider: LocationProvider, // 위치 정보 제공자
        getCurrentAddress: (Double, Double) -> Address?, // 위도와 경도를 통해 주소 정보를 가져오는 함수
    ) {
        // 현재 위치의 위도와 경도를 초기화
        if (latitude == null || longitude == null) {
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }

        // 위도와 경도가 null이 아니며, 0.0도 아닌 경우
        if (latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0) {
            val address = getCurrentAddress(latitude!!, longitude!!)
            address?.let {
                // 주소 정보를 바탕으로 텍스트 데이터 지정
                val defaultText = "알 수 없는 도로"
                val addressText = it.thoroughfare ?: it.featureName ?: it.subLocality ?: it.locality ?: defaultText
                addressTextLiveData.value = addressText

                // 부가적인 위치 정보를 UI에 업데이트
                val locationSubtitle = "${it.countryName} ${it.adminArea}"
                locationSubtitleLiveData.value = locationSubtitle
            } ?: run {
                // 주소 정보를 찾을 수 없는 경우의 처리
                addressTextLiveData.value = "위치 정보를 가져올 수 없습니다."
            }

            // 공기 질 데이터를 가져와서 UI 업데이트
            getAirQualityDataAndUpdateUI(latitude!!, longitude!!)
        } else {
            errorMessage.value = "데이터를 가져오는 데 실패했습니다."
        }
    }

    // 공기 질 데이터를 가져와서 UI를 업데이트하는 함수
    fun getAirQualityDataAndUpdateUI(latitude: Double, longitude: Double) {
        val retrofitAPI = RetrofitConnection.getInstance().create(AirQualityService::class.java)
        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "2d32be22-a9fc-41b7-8d13-85c79fc1272b" // API 키 입력
        ).enqueue(object : retrofit2.Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: retrofit2.Response<AirQualityResponse>
            ) {
                if (response.isSuccessful) {
                    errorMessage.value = "최신 데이터 업데이트 완료!"
                    // 응답 성공 시, 공기 질 및 날씨 정보를 UI에 업데이트
                    response.body()?.let {
                        updateAirQuality(it)
                        updateWeatherQuality(it.data.current.weather) // 날씨 정보 업데이트
                    }
                } else {
                    errorMessage.value = "데이터를 가져오는 데 실패했습니다."
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
                errorMessage.value = "데이터를 가져오는 데 실패했습니다."
            }
        })
    }
}

