package com.hasan.badhabit

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _myProfile = MutableStateFlow<UserProfile?>(null)
    val myProfile: StateFlow<UserProfile?> = _myProfile.asStateFlow()

    private val _friendsList = MutableStateFlow<List<UserProfile>>(emptyList())
    val friendsList: StateFlow<List<UserProfile>> = _friendsList.asStateFlow()
    
    private val _receivedRequestsList = MutableStateFlow<List<UserProfile>>(emptyList())
    val receivedRequestsList: StateFlow<List<UserProfile>> = _receivedRequestsList.asStateFlow()

    private val _friendHabits = MutableStateFlow<List<Habit>>(emptyList())
    val friendHabits: StateFlow<List<Habit>> = _friendHabits.asStateFlow()

    private val _searchResult = MutableStateFlow<UserProfile?>(null)
    val searchResult: StateFlow<UserProfile?> = _searchResult.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFriendHabitsLoading = MutableStateFlow(false)
    val isFriendHabitsLoading: StateFlow<Boolean> = _isFriendHabitsLoading.asStateFlow()

    private var profileListenerRegistration: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                startListeningToProfile(user.uid)
            } else {
                stopListening()
            }
        }
    }

    private fun startListeningToProfile(uid: String) {
        if (profileListenerRegistration != null) return
        
        _isLoading.value = true 

        profileListenerRegistration = db.collection("users").document(uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("UserViewModel", "Profil dinleme hatası", e)
                _isLoading.value = false
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                _myProfile.value = profile
                
                if (profile != null) {
                    val friendsToFetch = profile.friends
                    val requestsToFetch = profile.receivedRequests
                    
                    if (friendsToFetch.isEmpty() && requestsToFetch.isEmpty()) {
                        _friendsList.value = emptyList()
                        _receivedRequestsList.value = emptyList()
                        _isLoading.value = false
                    } else {
                        fetchUsersDetails(friendsToFetch) { friends -> 
                            _friendsList.value = friends
                            
                            if (requestsToFetch.isEmpty()) {
                                _receivedRequestsList.value = emptyList()
                                _isLoading.value = false
                            } else {
                                fetchUsersDetails(requestsToFetch) { requests ->
                                    _receivedRequestsList.value = requests
                                    _isLoading.value = false
                                }
                            }
                        }
                    }
                }
            } else {
                _myProfile.value = null
                _isLoading.value = false
            }
        }
    }

    private fun stopListening() {
        profileListenerRegistration?.remove()
        profileListenerRegistration = null
        
        _myProfile.value = null
        _friendsList.value = emptyList()
        _receivedRequestsList.value = emptyList()
        _friendHabits.value = emptyList()
        _searchResult.value = null
        _isLoading.value = false
    }

    fun updateUsername(newUsername: String) {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf("username" to newUsername, "uid" to uid, "email" to (auth.currentUser?.email ?: ""))
        db.collection("users").document(uid).set(data, SetOptions.merge())
            .addOnSuccessListener { _successMessage.value = "Kullanıcı adı değiştirildi." }
            .addOnFailureListener { e -> _errorMessage.value = "Güncelleme hatası: ${e.message}" }
    }
    
    fun updateAvatar(avatarId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("avatarId", avatarId)
            .addOnSuccessListener { _successMessage.value = "Profil resmi güncellendi." }
            .addOnFailureListener { _errorMessage.value = "Hata: ${it.message}" }
    }

    fun searchUser(username: String) {
        _searchResult.value = null
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0].toObject(UserProfile::class.java)
                    if (user?.uid != auth.currentUser?.uid) {
                        _searchResult.value = user
                    } else {
                        _errorMessage.value = "Kendini ekleyemezsin :)"
                    }
                } else {
                    _errorMessage.value = "Kullanıcı bulunamadı."
                }
            }
    }

    fun sendFriendRequest(targetUid: String) {
        val myUid = auth.currentUser?.uid ?: return
        val myProfile = _myProfile.value ?: return

        if (myProfile.friends.contains(targetUid)) { _errorMessage.value = "Zaten arkadaşsınız."; return }
        if (myProfile.sentRequests.contains(targetUid)) { _errorMessage.value = "Zaten istek gönderilmiş."; return }

        val batch = db.batch()
        val myRef = db.collection("users").document(myUid)
        val targetRef = db.collection("users").document(targetUid)

        batch.update(myRef, "sentRequests", FieldValue.arrayUnion(targetUid))
        batch.update(targetRef, "receivedRequests", FieldValue.arrayUnion(myUid))

        batch.commit()
            .addOnSuccessListener { _searchResult.value = null; _successMessage.value = "Arkadaşlık isteği gönderildi!" }
            .addOnFailureListener { _errorMessage.value = "İstek gönderilemedi: ${it.message}" }
    }

    fun acceptFriendRequest(senderUid: String) {
        val myUid = auth.currentUser?.uid ?: return
        val batch = db.batch()
        val myRef = db.collection("users").document(myUid)
        val senderRef = db.collection("users").document(senderUid)

        batch.update(myRef, "receivedRequests", FieldValue.arrayRemove(senderUid))
        batch.update(myRef, "friends", FieldValue.arrayUnion(senderUid))
        batch.update(senderRef, "sentRequests", FieldValue.arrayRemove(myUid))
        batch.update(senderRef, "friends", FieldValue.arrayUnion(myUid))

        batch.commit()
            .addOnSuccessListener { _successMessage.value = "Arkadaşlık isteği kabul edildi!" }
            .addOnFailureListener { _errorMessage.value = "Hata oluştu: ${it.message}" }
    }

    fun declineFriendRequest(senderUid: String) {
        val myUid = auth.currentUser?.uid ?: return
        val batch = db.batch()
        val myRef = db.collection("users").document(myUid)
        val senderRef = db.collection("users").document(senderUid)

        batch.update(myRef, "receivedRequests", FieldValue.arrayRemove(senderUid))
        batch.update(senderRef, "sentRequests", FieldValue.arrayRemove(myUid))

        batch.commit().addOnSuccessListener { _successMessage.value = "İstek reddedildi." }
    }

    fun removeFriend(friendUid: String) {
        val myUid = auth.currentUser?.uid ?: return
        val batch = db.batch()
        val myRef = db.collection("users").document(myUid)
        val friendRef = db.collection("users").document(friendUid)

        batch.update(myRef, "friends", FieldValue.arrayRemove(friendUid))
        batch.update(friendRef, "friends", FieldValue.arrayRemove(myUid))

        batch.commit().addOnSuccessListener { _successMessage.value = "Arkadaş silindi." }
    }

    private fun fetchUsersDetails(uids: List<String>, onSuccess: (List<UserProfile>) -> Unit) {
        if (uids.isEmpty()) { onSuccess(emptyList()); return }
        db.collection("users").whereIn("uid", uids.take(10)).get()
            .addOnSuccessListener { documents -> onSuccess(documents.toObjects(UserProfile::class.java)) }
            .addOnFailureListener { onSuccess(emptyList()) }
    }

    fun loadFriendHabits(friendUid: String) {
        _friendHabits.value = emptyList() 
        _isFriendHabitsLoading.value = true
        
        db.collection("habits")
            .whereEqualTo("userId", friendUid)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(Habit::class.java)
                _friendHabits.value = list.sortedByDescending { it.startTime }
                _isFriendHabitsLoading.value = false 
            }
            .addOnFailureListener {
                _isFriendHabitsLoading.value = false
            }
    }
    
    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _successMessage.value = null }
    
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
