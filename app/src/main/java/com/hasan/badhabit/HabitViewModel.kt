package com.hasan.badhabit

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class HabitViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var habitsListenerRegistration: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                startListeningToHabits(user.uid)
            } else {
                stopListening()
            }
        }
    }

    private fun startListeningToHabits(userId: String) {
        if (habitsListenerRegistration != null) return

        _isLoading.value = true

        habitsListenerRegistration = db.collection("habits")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HabitViewModel", "Listen failed.", e)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val habitList = snapshot.toObjects(Habit::class.java)
                    // Başlangıç zamanına göre sırala (en yeni en üstte olsun istersen descending yap)
                    _habits.value = habitList.sortedByDescending { it.startTime }
                    
                    // Otomatik Can Yenileme Kontrolü
                    checkAndRegenerateLives(habitList)
                }
                _isLoading.value = false
            }
    }

    private fun stopListening() {
        habitsListenerRegistration?.remove()
        habitsListenerRegistration = null
        _habits.value = emptyList()
        _isLoading.value = false
    }

    fun addHabit(name: String, motivation: String, difficulty: Int) {
        val userId = auth.currentUser?.uid ?: return
        val newHabit = Habit(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name,
            motivation = motivation,
            difficulty = difficulty,
            startTime = System.currentTimeMillis()
        )

        db.collection("habits").document(newHabit.id).set(newHabit)
            .addOnFailureListener { e -> Log.e("HabitViewModel", "Error adding habit", e) }
    }

    fun deleteHabit(habitId: String) {
        db.collection("habits").document(habitId).delete()
            .addOnFailureListener { e -> Log.e("HabitViewModel", "Error deleting habit", e) }
    }

    fun relapseHabit(habit: Habit) {
        if (habit.currentLives > 0) {
            val updates = hashMapOf<String, Any>(
                "currentLives" to habit.currentLives - 1,
                "lastRegenerationTime" to System.currentTimeMillis(), // Sayaç sıfırlanır
                "relapseHistory" to (habit.relapseHistory + System.currentTimeMillis())
            )
            
            db.collection("habits").document(habit.id).update(updates)
        }
    }
    
    private fun checkAndRegenerateLives(habits: List<Habit>) {
        val currentTime = System.currentTimeMillis()
        
        for (habit in habits) {
            if (habit.currentLives < habit.maxLives && habit.currentLives > 0) {
                // Zorluk seviyesine göre bekleme süresi (Gün)
                val daysNeeded = when(habit.difficulty) {
                    1 -> 10L // Kolay: 10 gün
                    2 -> 7L  // Orta: 7 gün
                    3 -> 5L  // Zor: 5 gün
                    else -> 7L
                }
                
                val timeNeededMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(daysNeeded)
                val lastTime = if (habit.lastRegenerationTime > 0) habit.lastRegenerationTime else habit.startTime
                val timePassed = currentTime - lastTime
                
                if (timePassed >= timeNeededMillis) {
                    // Süre dolmuş, 1 can ver ve sayacı sıfırla
                    val updates = hashMapOf<String, Any>(
                        "currentLives" to habit.currentLives + 1,
                        "lastRegenerationTime" to System.currentTimeMillis()
                    )
                    db.collection("habits").document(habit.id).update(updates)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
