package com.example.oktodo.util.base

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.oktodo.R
import com.example.oktodo.myPage.MyPage

abstract class BaseActivity : AppCompatActivity(), MenuHandler {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return handleMenuClick(item.itemId) || super.onOptionsItemSelected(item)
    }
}