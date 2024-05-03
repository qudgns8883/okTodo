package com.example.oktodo.Login

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // 여기서 YOUR_APP_KEY는 카카오 개발자 콘솔에서 발급받은 네이티브 앱 키를 사용합니다.
        KakaoSdk.init(this, "d16087d80c91cf7da093934da501b028")
    }
}