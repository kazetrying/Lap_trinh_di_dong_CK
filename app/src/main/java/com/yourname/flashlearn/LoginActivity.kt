package com.yourname.flashlearn


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yourname.flashlearn.databinding.ActivityLoginBinding
import com.yourname.flashlearn.util.UserManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nếu đã đăng nhập → vào thẳng MainActivity
        if (userManager.isLoggedIn()) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { handleLogin() }
        binding.btnRegister.setOnClickListener { handleRegister() }
    }

    private fun handleLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!")
            return
        }

        if (userManager.login(username, password)) {
            goToMain()
        } else {
            showError("Tên đăng nhập hoặc mật khẩu không đúng!")
        }
    }

    private fun handleRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!")
            return
        }

        if (password.length < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự!")
            return
        }

        if (userManager.register(username, password)) {
            MaterialAlertDialogBuilder(this)
                .setTitle("✅ Đăng ký thành công!")
                .setMessage("Chào mừng $username đến với FlashLearn!")
                .setPositiveButton("Bắt đầu học!") { _, _ -> goToMain() }
                .show()
        } else {
            showError("Tên đăng nhập đã tồn tại!")
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}