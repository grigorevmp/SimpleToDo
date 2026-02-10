package com.grigorevmp.simpletodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.grigorevmp.simpletodo.platform.AndroidContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidContextHolder.appContext = applicationContext

        setContent {
            App()
        }
    }
}
