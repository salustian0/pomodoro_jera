package com.salusoftware.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.salusoftware.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root;
        setContentView(view)

        this.binding.txtTime.text = "00:00"
    }
}