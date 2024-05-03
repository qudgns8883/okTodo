package com.example.oktodo.util.drawerUtil

import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout

object DrawerUtil {
    // 네비게이션 드로어의 가시성을 토글하는 메서드
    fun toggleDrawer(drawerLayout: FrameLayout, isDrawerOpen: Boolean): Boolean {
        drawerLayout.visibility = if (isDrawerOpen) View.GONE else View.VISIBLE
        return !isDrawerOpen
    }

    // 특정 View 내부의 점을 확인하는 메서드
    fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val rect = Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
        return rect.contains(x.toInt(), y.toInt())
    }

    // 드로어를 닫는 메서드
    fun closeDrawer(drawerLayout: FrameLayout, isDrawerOpen: Boolean): Boolean {
        if (isDrawerOpen) {
            drawerLayout.visibility = View.GONE
            return false
        }
        return isDrawerOpen
    }
}