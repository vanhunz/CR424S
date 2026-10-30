package com.example.nguyencongnhathuy_29214654999_cr424s

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nguyencongnhathuy_29214654999_cr424s.ui.navigation.WalletNavGraph
import com.example.nguyencongnhathuy_29214654999_cr424s.ui.theme.NguyenCongNhatHuy_29214654999_CR424STheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NguyenCongNhatHuy_29214654999_CR424STheme(darkTheme = true, dynamicColor = false) {
                WalletNavGraph()
            }
        }
    }
}