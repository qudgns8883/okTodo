package com.example.oktodo.AirAndWeather

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.oktodo.databinding.ActivityFragmentBinding

// FragmentActivity: 여러 프래그먼트를 관리하기 위한 액티비티
class FragmentActivity : AppCompatActivity() {
    // 프래그먼트 인스턴스 초기화
    var oneFragment: OneFragment? = null
    
    // View Binding을 사용하여 레이아웃 요소에 쉽게 접근
    lateinit var binding: ActivityFragmentBinding

    // 위치 권한 요청 결과를 처리하기 위한 ActivityResultLauncher 초기화
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    // 권한 요청 코드
    private val PERMISSIONS_REQUEST_CODE = 100

    // 요청할 권한 목록
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, // 정확한 위치 정보
        Manifest.permission.ACCESS_COARSE_LOCATION, // 대략적인 위치 정보
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View Binding 초기화
        binding = ActivityFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager2에 프래그먼트 어댑터 설정
        binding.viewpager.adapter = MyFragmentPagerAdapter(this)
        // 슬라이드 방향을 가로로 설정
        binding.viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // 모든 권한 요청 체크
        checkAllPermissions()

        // 위치 서비스 활성화 확인 후 권한 체크
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (isLocationServicesAvailable()) {
                // 위치 서비스 활성화된 경우 권한 체크
                isRunTimePermissionsGranted()
            } else {
                // 위치 서비스 비활성화된 경우 앱 종료 안내
                Toast.makeText(this, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    // 모든 권한 체크 메소드
    private fun checkAllPermissions() {
        if (!isLocationServicesAvailable()) {
            // 위치 서비스가 비활성화된 경우 설정 대화상자 표시
            showDialogForLocationServiceSetting()
        } else {
            // 위치 서비스가 활성화된 경우 권한 체크
            isRunTimePermissionsGranted()
        }
    }

    // 위치 서비스 사용 가능 여부 확인
    private fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // 런타임 권한 부여 여부 확인
    private fun isRunTimePermissionsGranted() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // 필요한 권한이 부여되지 않은 경우 사용자에게 권한 요청
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
            hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {
            // 모든 권한이 부여되었는지 확인하기 위해 "grantResults" 배열을 순회하면서 권한이 부여되었는지 확인
            val checkResult = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (checkResult) {
                // 모든 권한이 부여된 경우 UI 업데이트
                oneFragment?.updateUI()
            } else {
                // 권한이 거부된 경우 앱 종료 안내
                Toast.makeText(
                    this,
                    "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    // 위치 서비스 활성화 요청 대화상자 표시
    private fun showDialogForLocationServiceSetting() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialogInterface, i ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.cancel()
            Toast.makeText(this, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show()
            finish()
        })
        builder.create().show()
    }

    // MyFragmentPagerAdapter: ViewPager2에 사용될 프래그먼트 어댑터
    class MyFragmentPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        // 표시될 프래그먼트 목록
        private val fragments: List<Fragment> = listOf(OneFragment(), TwoFragment())

        // 프래그먼트의 총 개수 반환
        override fun getItemCount(): Int = fragments.size

        // 특정 위치에 해당하는 프래그먼트 반환
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}