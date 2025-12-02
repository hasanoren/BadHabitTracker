package com.hasan.badhabit

import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.util.concurrent.TimeUnit

data class RankInfo(
    val name: String, 
    val icon: String, 
    val color: Color, 
    val nextRankXp: Long? = null,
    val level: Int
)

fun getRankFromXp(xp: Long): RankInfo {
    return when {
        xp < 500 -> RankInfo("Acemi", "🥚", Color.Gray, 500, 0)
        xp < 1500 -> RankInfo("Kararlı", "🌱", Color(0xFF4CAF50), 1500, 1)
        xp < 4000 -> RankInfo("Savaşçı", "⚔️", Color(0xFF2196F3), 4000, 2)
        xp < 10000 -> RankInfo("Usta", "🛡️", Color(0xFF9C27B0), 10000, 3)
        else -> RankInfo("Efsane", "👑", Color(0xFFFFC107), null, 4)
    }
}

fun calculateXp(habit: Habit): Long {
    val lastResetTime = if (habit.relapseHistory.isNotEmpty()) habit.relapseHistory.last() else habit.startTime
    val currentTime = System.currentTimeMillis()
    val hoursPassed = TimeUnit.MILLISECONDS.toHours(currentTime - lastResetTime)
    val multiplier = 10 
    return hoursPassed * multiplier
}

// GÜNCELLENDİ: Daha Kapsamlı Hata Mesajları
fun getFriendlyErrorMessage(exception: Exception?): String {
    if (exception == null) return "Bilinmeyen bir hata oluştu."
    
    // 1. Exception Tipine Göre Kontrol (Daha Güvenilir)
    return when (exception) {
        is FirebaseAuthInvalidCredentialsException -> "Hatalı şifre veya e-posta adresi."
        is FirebaseAuthInvalidUserException -> "Bu hesaba ait kullanıcı bulunamadı."
        is FirebaseAuthUserCollisionException -> "Bu e-posta adresi zaten kullanımda."
        is FirebaseAuthWeakPasswordException -> "Şifre çok zayıf. En az 6 karakter olmalı."
        else -> {
            // 2. Mesaj İçeriğine Göre Kontrol (Yedek Plan)
            val message = exception.message?.lowercase() ?: ""
            when {
                message.contains("email") && message.contains("badly formatted") -> "Geçersiz e-posta formatı."
                message.contains("network error") || message.contains("network_error") -> "İnternet bağlantınızı kontrol edin."
                message.contains("blocked") || message.contains("too many requests") -> "Çok fazla deneme yaptınız. Lütfen bekleyin."
                message.contains("internal error") -> "Sunucu hatası. Lütfen tekrar deneyin."
                else -> "Hata: ${exception.localizedMessage}" // Bilinmeyen hataları olduğu gibi göster
            }
        }
    }
}
