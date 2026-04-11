package com.yourname.flashlearn.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun login(username: String, password: String): Boolean {
        // Lấy thông tin đã đăng ký
        val savedUser = prefs.getString("username", null)
        val savedPass = prefs.getString("password", null)

        return if (savedUser == username && savedPass == password) {
            prefs.edit().putBoolean("is_logged_in", true)
                .putString("current_user", username).apply()
            true
        } else false
    }

    fun register(username: String, password: String): Boolean {
        // Kiểm tra username đã tồn tại chưa
        val savedUser = prefs.getString("username", null)
        if (savedUser == username) return false

        prefs.edit()
            .putString("username", username)
            .putString("password", password)
            .putBoolean("is_logged_in", true)
            .putString("current_user", username)
            .apply()
        return true
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun getCurrentUser(): String = prefs.getString("current_user", "Người dùng") ?: "Người dùng"
}