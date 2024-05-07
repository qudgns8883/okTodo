package com.example.oktodo.metro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import com.example.oktodo.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.nio.Buffer

class BusanBusActivity : AppCompatActivity() {

    private lateinit var busEditText: EditText
    private lateinit var userInput: String
    private lateinit var busBtn: Button




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busan_bus)

        busEditText = findViewById(R.id.busEditText)
        busBtn = findViewById(R.id.button_bus_search)

        busBtn.setOnClickListener {
            userInput = busEditText.text.toString()
            intent = Intent(this,BusanBusChosenActivity::class.java).apply {
                putExtra("userInput", userInput)
            }
            startActivity(intent)
        }

        busEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // EditText에서 엔터(완료) 키가 눌러졌을 때 키보드 숨기기
                v.hideKeyboard()
                true
            } else false
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService( Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}