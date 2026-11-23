package com.ubaydullayev.expensetracker.core.auth

/**
 * Sof qoidalar (Android'ga bog'liq emas), shuning uchun unit test bilan tekshiriladi.
 * [SessionManager] kirish paytida shu qoidalarga tayanadi.
 */
object SessionRules {

    /** Eski versiyalardagi standart ism. Bunday ism "foydalanuvchi kiritmagan" deb hisoblanadi. */
    const val LEGACY_DEFAULT_NAME = "Your Name"

    /**
     * Qurilmadagi ma'lumotlar boshqa akkauntga tegishlimi?
     * [storedUid] null bo'lsa (eski o'rnatma yoki birinchi kirish) - egasi hali belgilanmagan,
     * ma'lumotlar o'chirilmaydi.
     */
    fun isDifferentOwner(storedUid: String?, newUid: String): Boolean =
        storedUid != null && storedUid != newUid

    /**
     * Profilda ko'rsatiladigan ism.
     * 1. O'sha akkaunt qayta kirgan va ismni o'zi kiritgan bo'lsa - o'shani saqlaymiz.
     * 2. Aks holda Firebase'dagi ism (ro'yxatdan o'tishda yoki Google'dan).
     * 3. Ism yo'q bo'lsa - emailning "@" dan oldingi qismi.
     */
    fun resolveName(
        savedName: String,
        sameOwner: Boolean,
        firebaseName: String?,
        email: String?
    ): String {
        val saved = savedName.trim()
        if (sameOwner && saved.isNotEmpty() && saved != LEGACY_DEFAULT_NAME) return saved

        val fromFirebase = firebaseName?.trim().orEmpty()
        if (fromFirebase.isNotEmpty()) return fromFirebase

        return emailLocalPart(email)
    }

    fun emailLocalPart(email: String?): String =
        email?.trim()?.substringBefore('@').orEmpty()
}
