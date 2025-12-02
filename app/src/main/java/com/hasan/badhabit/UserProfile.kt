package com.hasan.badhabit

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val avatarId: String = "default", // YENİ: Avatar Kimliği
    
    val friends: List<String> = emptyList(),
    val sentRequests: List<String> = emptyList(),
    val receivedRequests: List<String> = emptyList()
)
