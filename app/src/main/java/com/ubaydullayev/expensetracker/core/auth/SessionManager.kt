package com.ubaydullayev.expensetracker.core.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Kirish, chiqish va akkauntni o'chirishning yagona joyi.
 *
 * Ma'lumotlar (Room) faqat qurilmada saqlanadi va bitta akkauntga tegishli:
 * - o'sha akkaunt qayta kirsa, ma'lumotlar joyida qoladi;
 * - boshqa akkaunt kirsa, oldingi egasining ma'lumotlari o'chiriladi.
 */
class SessionManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val auth = Firebase.auth
    private val prefs = UserPreferences.getInstance(appContext)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /** Splash uchun: foydalanuvchi kirgan va "Remember me" yoqilgan. */
    fun hasActiveSession(): Boolean = auth.currentUser != null && prefs.rememberSession

    /**
     * Har bir muvaffaqiyatli kirishdan keyin (email, ro'yxatdan o'tish, Google) chaqiriladi.
     * [newName] - ro'yxatdan o'tishda kiritilgan ism (Firebase keshidagi displayName
     * darhol yangilanmasligi mumkin).
     */
    suspend fun onSignedIn(user: FirebaseUser, rememberMe: Boolean = true, newName: String? = null) {
        val differentOwner = SessionRules.isDifferentOwner(prefs.ownerUid, user.uid)
        if (differentOwner) wipeLocalData()

        prefs.setOwnerUid(user.uid)
        prefs.setRememberSession(rememberMe)

        val name = SessionRules.resolveName(
            savedName = prefs.profile.value.name,
            sameOwner = !differentOwner,
            firebaseName = newName ?: user.displayName,
            email = user.email
        )
        prefs.updateProfile(name, user.email.orEmpty())
    }

    /** Chiqish. Ma'lumotlar qurilmada qoladi: o'sha akkaunt qayta kirsa, hammasi joyida. */
    suspend fun signOut() {
        auth.signOut()
        clearCredentialState()
    }

    /** Akkaunt saqlanadi, tranzaksiya, budjet, maqsad va to'lovlar o'chiriladi. */
    suspend fun resetAppData() = withContext(Dispatchers.IO) {
        AppDatabase.getInstance(appContext).clearAllTables()
    }

    /**
     * Firebase akkauntini va qurilmadagi barcha ma'lumotlarni o'chiradi.
     * Firebase o'chirishdan oldin yaqinda kirishni talab qiladi, shuning uchun
     * [credential] bilan qayta tasdiqlanadi (parol yoki Google).
     * Firebase'da o'chmasa, lokal ma'lumotlar ham tegmaydi.
     */
    suspend fun deleteAccount(credential: AuthCredential) {
        val user = auth.currentUser ?: error("Not signed in")
        user.reauthenticate(credential).await()
        user.delete().await()
        wipeLocalData()
        auth.signOut()
        clearCredentialState()
    }

    /** Akkaunt Google orqali kirganmi (aks holda email/parol). */
    fun isGoogleAccount(): Boolean =
        auth.currentUser?.providerData.orEmpty().any { it.providerId == GOOGLE_PROVIDER_ID }

    private suspend fun wipeLocalData() {
        withContext(Dispatchers.IO) {
            AppDatabase.getInstance(appContext).clearAllTables()
        }
        prefs.clearAll()
    }

    /** Google akkaunt tanlash oynasi keyingi safar yana chiqishi uchun. */
    private suspend fun clearCredentialState() {
        try {
            CredentialManager.create(appContext).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // Credential Manager yo'q yoki xato - chiqishga xalaqit bermaydi.
        }
    }

    companion object {
        private const val GOOGLE_PROVIDER_ID = "google.com"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context).also { INSTANCE = it }
            }
    }
}
