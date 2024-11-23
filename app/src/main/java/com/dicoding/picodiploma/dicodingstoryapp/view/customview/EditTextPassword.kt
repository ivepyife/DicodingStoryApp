package com.dicoding.picodiploma.dicodingstoryapp.view.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.google.android.material.textfield.TextInputEditText

class EditTextPassword @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextInputEditText(context, attrs) {
    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }

    private fun validatePassword(password: String) {
        when {
            password.isEmpty() -> {
                setError(context.getString(R.string.password_cannot_empty), null)
            }
            password.contains(" ") -> {
                setError(context.getString(R.string.password_cannot_contain_space), null)
            }
            password.length < 8 -> {
                setError(context.getString(R.string.password_min_8_char), null)
            }
            else -> {
                error = null
            }
        }
    }
}