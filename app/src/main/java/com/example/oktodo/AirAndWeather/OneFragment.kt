package com.example.oktodo.AirAndWeather

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.oktodo.AirAndWeather.retrofit.AirQualityResponse
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.FragmentOneBinding
import com.example.oktodo.databinding.FragmentTwoBinding
import com.example.oktodo.util.drawerUtil.DrawerUtil
import com.example.oktodo.util.menuClickListener.CardViewClickListener
import com.example.oktodo.util.menuClickListener.NavigationMenuClickListener
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class OneFragment : Fragment() {
    // 뷰모델 및 바인딩 변수 선언
    private lateinit var viewModel: LocationViewModel
    private lateinit var airBinding: FragmentOneBinding
    private lateinit var weatherBinding: FragmentTwoBinding
    private var isDrawerOpen = false

    // 맵 액티비티 결과를 처리하는 콜백 등록
    private val startMapActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    viewModel.latitude = data.getDoubleExtra("latitude", 0.0)
                    viewModel.longitude = data.getDoubleExtra("longitude", 0.0)
                    updateUI()
                }
            }
        }

    // onCreateView 메서드: 프래그먼트의 뷰를 생성하고 초기화
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // 두 개의 Binding 객체를 초기화
        airBinding = FragmentOneBinding.inflate(inflater, container, false)
        weatherBinding = FragmentTwoBinding.inflate(inflater, container, false)

        // ViewModel을 초기화하고, 현재 액티비티와 연결
        viewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        // 주소 텍스트에 대한 LiveData를 관찰하고 UI를 업데이트
        viewModel.addressTextLiveData.observe(viewLifecycleOwner, Observer { addressText ->
            airBinding.tvLocationTitle.text = addressText
        })

        // 위치 소제목에 대한 LiveData를 관찰하고 UI를 업데이트
        viewModel.locationSubtitleLiveData.observe(
            viewLifecycleOwner,
            Observer { locationSubtitle ->
                airBinding.tvLocationSubtitle.text = locationSubtitle
            })

        // 공기질 데이터에 대한 LiveData를 관찰하고 UI를 업데이트
        viewModel.airQualityData.observe(viewLifecycleOwner, Observer { airQualityResponse ->
            airQualityResponse?.let {
                updateAirUI(it)
            }
        })

        // 날씨 데이터에 대한 LiveData를 관찰하고 UI를 업데이트합니다.
        viewModel.weatherQualityData.observe(viewLifecycleOwner, Observer { weatherQualityData ->
            weatherQualityData?.let {
                updateWeatherUI(it)
            }
        })

        // 추가적인 UI 업데이트를 수행
        updateUI()

        // airBinding의 root 뷰를 반환
        return airBinding.root
    }

    // onViewCreated 메서드: 뷰가 생성된 후에 호출되며, 여기서 추가적인 설정을 수행
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRefreshButton()
        setFab()

        airBinding.homeIcon.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }

        // 메뉴 아이콘 클릭 시 네비게이션 드로어의 가시성을 토글
        airBinding.menuIcon.setOnClickListener {
            isDrawerOpen = DrawerUtil.toggleDrawer(airBinding.navigationDrawer, isDrawerOpen)
        }

        // 메인 레이아웃에 터치 리스너를 설정
        // 경고를 무시: 이 경우 performClick을 호출하지 않는 것이 의도된 동작
        airBinding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isDrawerOpen) {
                if (!DrawerUtil.isPointInsideView(event.rawX, event.rawY, airBinding.navigationDrawer)) {
                    isDrawerOpen = DrawerUtil.closeDrawer(airBinding.navigationDrawer, isDrawerOpen)
                }
            }
            false
        }

        // NavigationView의 헤더 뷰를 얻음
        val navigationView = view.findViewById<NavigationView>(R.id.main_drawer_view)
        val headerView = navigationView.getHeaderView(0)

        // NavigationView 메뉴 텍스트 업데이트 코드 추가
        NavigationMenuClickListener(requireContext()).updateMenuText(navigationView)

        // 싱글톤 객체의 메소드를 호출하여 클릭 리스너를 설정
        CardViewClickListener.setupCardViewClickListeners(headerView, requireContext(), this)

        // View Binding을 사용하여 NavigationView에 리스너 설정
        airBinding.mainDrawerView.setNavigationItemSelectedListener(NavigationMenuClickListener(requireContext()))
    }

    // UI 업데이트 메소드
    fun updateUI() {
        context?.let {
            val locationProvider = LocationProvider(it)
            viewModel.updateUI(locationProvider, this::getCurrentAddress)
        }
    }

    // FAB(플로팅 액션 버튼)을 설정
    private fun setFab() {
        airBinding.searchIcon.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java).apply {
                putExtra("currentLat", viewModel.latitude)
                putExtra("currentLng", viewModel.longitude)
            }
            startMapActivityResult.launch(intent)
        }
    }

    // 현재 위치의 주소를 가져오는 함수
    private fun getCurrentAddress(latitude: Double, longitude: Double): Address? {
        val geoCoder = Geocoder(requireContext(), Locale.KOREA)
        try {
            // 지정된 위도, 경도로부터 주소를 조회합니다. 최대 결과 개수는 7개
            val addresses = geoCoder.getFromLocation(latitude, longitude, 7)
            if (addresses.isNullOrEmpty()) {
                Toast.makeText(context, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
                return null
            }
            // 첫 번째 주소를 반환
            return addresses[0]
        } catch (ioException: IOException) {
            Toast.makeText(context, "지오코더 서비스 사용불가합니다.", Toast.LENGTH_LONG).show()
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(context, "잘못된 위도, 경도 입니다.", Toast.LENGTH_LONG).show()
        }
        return null
    }

    // 공기질 데이터를 UI에 업데이트하는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAirUI(airQualityData: AirQualityResponse) {
        val pollutionData = airQualityData.data.current.pollution
        // AQI(공기질 지수)를 표시
        airBinding.tvCount.text = pollutionData.aqius.toString()
        // 측정 시간을 'Asia/Seoul' 시간대로 변환하여 표시
        val dateTime =
            ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        airBinding.tvCheckTime.text = dateTime.format(dateFormatter).toString()
        // AQI 값에 따라 UI를 업데이트
        when (pollutionData.aqius) {
            in 0..50 -> {
                airBinding.tvTitle.text = "좋음"
                airBinding.imgBg.setImageResource(R.drawable.bg_good)
            }

            in 51..150 -> {
                airBinding.tvTitle.text = "보통"
                airBinding.imgBg.setImageResource(R.drawable.bg_soso)
            }

            in 151..200 -> {
                airBinding.tvTitle.text = "나쁨"
                airBinding.imgBg.setImageResource(R.drawable.bg_bad)
            }

            else -> {
                airBinding.tvTitle.text = "매우 나쁨"
                airBinding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    // 날씨 데이터를 UI에 업데이트하는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateWeatherUI(weatherData: AirQualityResponse.Data.Current.Weather) {
        // 측정 시간을 'Asia/Seoul' 시간대로 변환하여 표시합니다.
        val zonedDateTime =
            ZonedDateTime.parse(weatherData.ts).withZoneSameInstant(ZoneId.of("Asia/Seoul"))
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd a hh:mm", Locale.KOREA)
        weatherBinding.tvCheckTime.text = zonedDateTime.format(dateFormatter).toString()
        // 온도, 습도, 풍속을 표시합니다.
        weatherBinding.temp.text = "${weatherData.tp}°C"
        weatherBinding.humidity.text = "${weatherData.hu}%"
        weatherBinding.wind.text = "${weatherData.ws} m/s"
        // 날씨 아이콘을 설정합니다.
        getWeatherDescription(weatherData.ic)?.let {
            weatherBinding.status.setImageResource(it)
        }
    }

    // 날씨 아이콘 코드에 따른 리소스 ID 반환 함수
    private fun getWeatherDescription(weatherIcon: String): Int? {
        return when (weatherIcon) {
            "01d" -> R.drawable.icon_01d  // 맑은 낮
            "01n" -> R.drawable.icon_01n  // 맑은 밤
            "02d" -> R.drawable.icon_02d  // 구름이 조금 있는 낮
            "02n" -> R.drawable.icon_02n  // 구름이 조금 있는 밤
            "03d", "03n" -> R.drawable.icon_03d  // 흐림
            "04d", "04n" -> R.drawable.icon_04d  // 구름 많음
            "09d", "09n" -> R.drawable.icon_09d  // 소나기
            "10d", "10n" -> R.drawable.icon_10d  // 비
            "11d", "11n" -> R.drawable.icon_11d  // 천둥번개
            "13d", "13n" -> R.drawable.icon_13d  // 눈
            "50d", "50n" -> R.drawable.icon_50d  // 안개
            else -> null  // 일치하는 아이콘 없음
        }
    }

    // 새로고침 버튼 설정 함수
    private fun setRefreshButton() {
        airBinding.btnRefresh.setOnClickListener {
            updateUI()  // UI 업데이트 메소드 호출
        }
    }
}