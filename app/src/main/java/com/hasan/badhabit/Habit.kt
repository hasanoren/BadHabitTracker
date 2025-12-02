package com.hasan.badhabit

data class Habit(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    
    val motivation: String = "",
    val difficulty: Int = 1, // 1: Kolay, 2: Orta, 3: Zor
    
    val startTime: Long = 0L,
    val currentLives: Int = 3,
    val maxLives: Int = 3,
    
    val relapseHistory: List<Long> = emptyList(),
    
    // YENİ: Son can kazanma veya son hata zamanı (Sayaç buradan başlar)
    val lastRegenerationTime: Long = 0L 
)
