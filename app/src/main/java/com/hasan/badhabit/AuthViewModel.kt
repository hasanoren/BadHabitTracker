import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hasan.badhabit.UserProfile
import com.hasan.badhabit.getFriendlyErrorMessage // Import edildi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _resetPasswordMessage = MutableStateFlow<String?>(null)
    val resetPasswordMessage: StateFlow<String?> = _resetPasswordMessage.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _isUserLoggedIn.value = firebaseAuth.currentUser != null
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.reload().addOnFailureListener { e ->
                Log.e("AuthViewModel", "Kullanıcı doğrulanamadı.", e)
                signOut()
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Lütfen tüm alanları doldurun."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let { createProfileIfNeeded(it) }
                } else {
                    // GÜNCELLENDİ: Türkçe mesaj
                    _errorMessage.value = getFriendlyErrorMessage(task.exception)
                }
                _isLoading.value = false
            }
    }

    fun register(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Lütfen tüm alanları doldurun."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let { createProfileIfNeeded(it) }
                } else {
                    // GÜNCELLENDİ: Türkçe mesaj
                    _errorMessage.value = getFriendlyErrorMessage(task.exception)
                }
                _isLoading.value = false
            }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let { createProfileIfNeeded(it) }
                } else {
                    _errorMessage.value = getFriendlyErrorMessage(task.exception)
                }
                _isLoading.value = false
            }
    }
    
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Lütfen e-posta adresinizi girin."
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null

        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->
                val methods = result.signInMethods ?: emptyList()
                
                if (methods.contains(GoogleAuthProvider.PROVIDER_ID)) {
                    _errorMessage.value = "Bu e-posta Google hesabı ile kayıtlı. Lütfen 'Google ile Devam Et' butonunu kullanın."
                    _isLoading.value = false
                } else {
                    sendResetEmailFinal(email)
                }
            }
            .addOnFailureListener { e ->
                Log.w("AuthViewModel", "Giriş yöntemleri alınamadı: ${e.message}")
                sendResetEmailFinal(email)
            }
    }

    private fun sendResetEmailFinal(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _resetPasswordMessage.value = "Sıfırlama bağlantısı gönderildi! Spam kutusunu kontrol etmeyi unutmayın."
                _errorMessage.value = null
            }
            .addOnFailureListener { e ->
                _errorMessage.value = getFriendlyErrorMessage(e)
            }
            .addOnCompleteListener {
                _isLoading.value = false
            }
    }
    
    fun clearResetMessage() { _resetPasswordMessage.value = null }
    fun clearError() { _errorMessage.value = null }

    fun signOut() {
        auth.signOut()
        _isUserLoggedIn.value = false
    }

    private fun createProfileIfNeeded(user: FirebaseUser) {
        val userDoc = db.collection("users").document(user.uid)

        userDoc.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val defaultUsername = user.email?.substringBefore("@") ?: "user${user.uid.take(5)}"
                
                val newProfile = UserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    username = defaultUsername,
                    friends = emptyList()
                )

                userDoc.set(newProfile)
                    .addOnFailureListener { e -> Log.e("AuthViewModel", "Profil oluşturma hatası", e) }
            }
        }
    }
}
