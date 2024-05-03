package com.example.oktodo.AirAndWeather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import java.lang.Exception

// GPS 나 네트워크의 위치를 사용하여 위도와 경도를 가져옴
class LocationProvider(val context : Context) {
    // 현재 위치 정보를 저장하는 변수로, 초기값은 null
    private var location : Location? = null
    // 위치 관리자를 나타내는 변수로, 초기값은 null
    private var locationManager : LocationManager? = null

    // 클래스가 초기화될 때 호출
    init {
        getLocation()
    }

    private fun getLocation() : Location? {
        try {
            // 위치 관리자를 가져옴
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // GPS 및 네트워크 위치 정보를 저장할 변수를 선언
            var gpsLocation : Location? = null
            var networkLocation : Location? = null

            // GPS or Network 가 활성화 되었는지 확인
            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            // GPS가 활성화되어 있지 않고 네트워크 위치가 활성화되어 있지 않은 경우, null을 반환
            if (!isGPSEnabled && !isNetworkEnabled) {
                return null
            } else {
                // 위치 권한이 부여되지 않은 경우, null을 반환
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }

                // 네트워크 위치가 활성화되어 있는 경우
                if (isNetworkEnabled) {
                    // LocationManager.NETWORK_PROVIDER를 통해 마지막으로 알려진 위치를 가져옴
                    networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                // GPS가 활성화되어 있는 경우
                if (isGPSEnabled) {
                    // LocationManager.GPS_PROVIDER를 통해 마지막으로 알려진 위치를 가져옴
                    gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }

                // GPS 위치와 네트워크 위치가 모두 null이 아닌 경우
                if (gpsLocation != null && networkLocation != null) {
                    // 두 위치의 정확도를 비교하여 보다 정확한 위치를 선택
                    // 정확도가 더 높은 위치를 선택하여 location 변수에 할당
                    if (gpsLocation.accuracy > networkLocation.accuracy) {
                        location = gpsLocation
                    } else {
                        // location = networkLocation
                        location = gpsLocation
                    }
                    // 만약 두 위치 중 하나라도 null이 아닌 경우, 해당 위치를 location 변수에 할당
                } else {
                    if (gpsLocation != null) {
                        location = gpsLocation
                    }
                    if (networkLocation != null) {
                        location = networkLocation
                    }
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
        // 마지막으로 결정된 위치를 반환
        return location
    }

    // 현재 위치의 위도를 반환
    fun getLocationLatitude() : Double? {
        // location이 null이 아니라면 latitude를 반환하고, 그렇지 않다면 0.0을 반환
        return location?.latitude
    }

    // 현재 위치의 경도를 반환
    fun getLocationLongitude() : Double? {
        // location이 null이 아니라면 longitude를 반환하고, 그렇지 않다면 0.0을 반환
        return location?.longitude
    }
}