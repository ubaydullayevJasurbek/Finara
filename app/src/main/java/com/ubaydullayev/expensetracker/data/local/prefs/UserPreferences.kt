package com.ubaydullayev.expensetracker.data.local.prefs

import android.content.Context
import com.ubaydullayev.expensetracker.domain.model.AppCurrency
import com.ubaydullayev.expensetracker.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit
import com.ubaydullayev.expensetracker.utils.CurrencyFormatter

class UserPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(readProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIF, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _currencyCode = MutableStateFlow(prefs.getString(KEY_CURRENCY, "USD") ?: "USD")
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    init {
        CurrencyFormatter.current = AppCurrency.fromCode(_currencyCode.value)
    }

    fun updateProfile(name: String, email: String) {
        prefs.edit { putString(KEY_NAME, name).putString(KEY_EMAIL, email) }
        _profile.value = UserProfile(name, email)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIF, enabled) }
        _notificationsEnabled.value = enabled
    }

    fun setCurrencyCode(code: String) {
        prefs.edit { putString(KEY_CURRENCY, code) }
        CurrencyFormatter.current = AppCurrency.fromCode(code)
        _currencyCode.value = code
    }

    private fun readProfile() = UserProfile(
        name = prefs.getString(KEY_NAME, "Your Name") ?: "Your Name",
        email = prefs.getString(KEY_EMAIL, "") ?: ""
    )

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOTIF = "notifications"
        private const val KEY_CURRENCY = "currency"

        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
    }
}