package com.mob.proyectoandroid.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.airbnb.lottie.LottieAnimationView
import com.mob.proyectoandroid.R
import com.mob.proyectoandroid.ui.home.HomeActivity
import com.mob.proyectoandroid.ui.login.LoginActivity
import com.mob.proyectoandroid.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1️⃣ Aplica el tema guardado antes de inflar el layout
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val themeName = prefs.getString("app_theme", "SYSTEM")

        val mode = when (themeName) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)

        // 2️⃣ Cargar la vista
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 3️⃣ Mantener animación
        lottieAnimationView = findViewById(R.id.lottieSplash)
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            checkSession()
        }
    }


    private fun checkSession() {
        val token = preferenceManager.getAccessToken()
        val nextIntent = if (!token.isNullOrEmpty()) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(nextIntent)
        finish()
    }
}
