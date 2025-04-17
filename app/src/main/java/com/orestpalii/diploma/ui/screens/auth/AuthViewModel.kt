package com.orestpalii.diploma.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.orestpalii.diploma.data.service.UserProfileService
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var errorMessage             by mutableStateOf<String?>(null)
        private set
    var emailVerificationSent   by mutableStateOf(false)
        private set
    var isEmailVerified         by mutableStateOf(false)
        private set
    var hasProfile              by mutableStateOf(false)
        private set

    init {
        checkSession()
    }

    fun clearErrors() {
        errorMessage = null
    }

    /** Перевіряємо, можливо, уже логінені й лист вже верифіковано */
    fun checkSession() {
        auth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (auth.currentUser?.isEmailVerified == true) {
                isEmailVerified = true
                checkProfile()
            } else {
                emailVerificationSent = true
            }
        }
    }

    /** Зареєструвати й надіслати лист */
    fun register(email: String, pwd: String) {
        errorMessage = null
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    res.result?.user
                        ?.sendEmailVerification()
                        ?.addOnCompleteListener { mres ->
                            emailVerificationSent = mres.isSuccessful
                            if (!mres.isSuccessful) errorMessage = mres.exception?.localizedMessage
                        }
                } else {
                    errorMessage = res.exception?.localizedMessage
                }
            }
    }

    /** Зайти й перевірити лист */
    fun login(email: String, pwd: String) {
        errorMessage = null
        auth.signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    checkEmailVerification()
                } else {
                    errorMessage = res.exception?.localizedMessage
                }
            }
    }

    /** Кнопка «Я підтвердив пошту» */
    fun checkEmailVerification() {
        auth.currentUser
            ?.reload()
            ?.addOnCompleteListener {
                if (auth.currentUser?.isEmailVerified == true) {
                    isEmailVerified = true
                    checkProfile()
                } else {
                    errorMessage = "Будь ласка, підтвердьте пошту перед продовженням."
                }
            }
    }

    /** Перевірка наявності профілю в бекенді */
    private fun checkProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            hasProfile = UserProfileService.checkIfUserProfileExists(uid)
        }
    }

    /** Ставимо прапорець, що профіль створено (онбординг завершено) */
    fun markProfileCreated() {
        hasProfile = true
    }

    /** Скидаємо стан email‑sent, повертаємося до AuthScreen */
    fun resetEmailSent() {
        emailVerificationSent = false
        errorMessage = null
    }

    fun logout() {
        auth.signOut()
        emailVerificationSent = false
        isEmailVerified       = false
        hasProfile            = false
    }
}
