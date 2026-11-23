package com.ubaydullayev.expensetracker.data.local.prefs

import android.content.Context
import androidx.core.content.edit
import com.ubaydullayev.expensetracker.domain.model.AppCurrency
import com.ubaydullayev.expensetracker.domain.model.UserProfile
import com.ubaydullayev.expensetracker.utils.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(readProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _currencyCode = MutableStateFlow(prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY)
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _hideBalanceOnStart = MutableStateFlow(prefs.getBoolean(KEY_HIDE_BALANCE, false))
    val hideBalanceOnStart: StateFlow<Boolean> = _hideBalanceOnStart.asStateFlow()

    /** Qurilmadagi ma'lumotlar egasining Firebase UID'i (null - hali belgilanmagan). */
    val ownerUid: String?
        get() = prefs.getString(KEY_OWNER_UID, null)

    /** "Remember me": false bo'lsa, keyingi ishga tushishda Login so'raladi. */
    val rememberSession: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER, true)

    init {
        CurrencyFormatter.current = AppCurrency.fromCode(_currencyCode.value)
    }

    fun updateProfile(name: String, email: String) {
        prefs.edit { putString(KEY_NAME, name).putString(KEY_EMAIL, email) }
        _profile.value = UserProfile(name, email)
    }

    fun setCurrencyCode(code: String) {
        prefs.edit { putString(KEY_CURRENCY, code) }
        CurrencyFormatter.current = AppCurrency.fromCode(code)
        _currencyCode.value = code
    }

    fun setHideBalanceOnStart(hide: Boolean) {
        prefs.edit { putBoolean(KEY_HIDE_BALANCE, hide) }
        _hideBalanceOnStart.value = hide
    }

    fun setOwnerUid(uid: String) = prefs.edit { putString(KEY_OWNER_UID, uid) }

    fun setRememberSession(remember: Boolean) = prefs.edit { putBoolean(KEY_REMEMBER, remember) }

    /** Akkaunt o'zgarganda yoki o'chirilganda: barcha sozlamalar standart holatga qaytadi. */
    fun clearAll() {
        prefs.edit(commit = true) { clear() }
        _profile.value = readProfile()
        _hideBalanceOnStart.value = false
        _currencyCode.value = DEFAULT_CURRENCY
        CurrencyFormatter.current = AppCurrency.fromCode(DEFAULT_CURRENCY)
    }

    private fun readProfile() = UserProfile(
        name = prefs.getString(KEY_NAME, "") ?: "",
        email = prefs.getString(KEY_EMAIL, "") ?: ""
    )

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_HIDE_BALANCE = "hide_balance_on_start"
        private const val KEY_OWNER_UID = "owner_uid"
        private const val KEY_REMEMBER = "remember_session"
        private const val DEFAULT_CURRENCY = "USD"

        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
    }
}
