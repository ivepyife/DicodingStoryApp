package com.dicoding.picodiploma.dicodingstoryapp.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.databinding.ActivitySignupBinding
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.AuthViewModel
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.ViewModelFactory
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity() {
    private val viewModel by viewModels<AuthViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            resetErrorState()

            when {
                name.isEmpty() -> {
                    showFieldError(binding.nameEditTextLayout, getString(R.string.name_cannot_empty))
                    showError(getString(R.string.name_not_valid), getString(R.string.name_cannot_empty))
                }
                !isEmailValid(email) -> {
                    showFieldError(binding.emailEditTextLayout, getString(R.string.unvalid_email_format))
                    showError(getString(R.string.unvalid_email), getString(R.string.reminder_valid_email))
                }
                !isPasswordValid(password) -> {
                    showFieldError(binding.passwordEditTextLayout, getString(R.string.password_min_8_char))
                    showError(getString(R.string.unvalid_password), getString(R.string.reminder_valid_password))
                }
                else -> {
                    showLoading(true)
                    viewModel.register(name, email, password).observe(this) { result ->
                        when (result) {
                            is Result.Success -> {
                                showLoading(false)
                                AlertDialog.Builder(this).apply {
                                    setTitle("Yeah!")
                                    setMessage(getString(R.string.account_cretaed_message, email))
                                    setPositiveButton(getString(R.string.enter)) { _, _ ->
                                        finish()
                                    }
                                    create()
                                    show()
                                }
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showError(getString(R.string.registration_failed), result.error)
                            }
                            is Result.Loading -> {
                                showLoading(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.signupButton.isEnabled = !isLoading
    }

    private fun showFieldError(layout: TextInputLayout, error: String) {
        layout.error = error
        layout.setErrorTextColor(ColorStateList.valueOf(Color.RED))
        layout.boxStrokeColor = Color.RED
    }

    private fun resetErrorState() {
        listOf(
            binding.nameEditTextLayout,
            binding.emailEditTextLayout,
            binding.passwordEditTextLayout
        ).forEach { field ->
            field.error = null
            field.boxStrokeColor = Color.GRAY
        }
    }


    private fun showError(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 && !password.contains(" ")
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val nameTextView =
            ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100)
        val nameEditTextLayout =
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)


        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                signup
            )
            startDelay = 100
        }.start()
    }
}