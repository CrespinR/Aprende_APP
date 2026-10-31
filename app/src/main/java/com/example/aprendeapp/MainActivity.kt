package com.example.aprendeapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.view.LoginActivity
import com.example.aprendeapp.view.MainActivity as DashboardActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authController = AuthController.getInstance()

        if (authController.estaSesionIniciada()) {
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
