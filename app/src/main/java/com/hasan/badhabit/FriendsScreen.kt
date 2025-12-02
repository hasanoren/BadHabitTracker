package com.hasan.badhabit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userViewModel: UserViewModel = viewModel()
) {
    val myProfile by userViewModel.myProfile.collectAsState()
    val friends by userViewModel.friendsList.collectAsState()
    val receivedRequests by userViewModel.receivedRequestsList.collectAsState()
    
    val isLoading by userViewModel.isLoading.collectAsState()
    
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<UserProfile?>(null) }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (myProfile != null && myProfile!!.username.isEmpty()) {
        CreateProfileScreen(userViewModel)
        return
    }

    if (selectedFriend != null) {
        FriendDetailScreen(
            friend = selectedFriend!!,
            userViewModel = userViewModel,
            onBack = { selectedFriend = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Arkadaşlar", fontWeight = FontWeight.Bold)
                        Text("@${myProfile?.username}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFriendDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Arkadaş Ekle")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (receivedRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Gelen İstekler (${receivedRequests.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(receivedRequests) { requestUser ->
                    RequestItem(
                        user = requestUser,
                        onAccept = { userViewModel.acceptFriendRequest(requestUser.uid) },
                        onDecline = { userViewModel.declineFriendRequest(requestUser.uid) }
                    )
                }
                item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }
            }

            if (friends.isEmpty()) {
                item {
                    if (receivedRequests.isEmpty()) {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Henüz kimseyi takip etmiyorsun.", color = Color.Gray)
                            Text("Sağ alttaki (+) butonuyla arkadaş bul.", color = Color.Gray)
                        }
                    } else {
                         Text("Arkadaş listesi boş.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            } else {
                item {
                     Text(
                        text = "Arkadaşlarım (${friends.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(friends) { friend ->
                    FriendItem(friend = friend, onClick = { selectedFriend = friend })
                }
            }
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            userViewModel = userViewModel,
            onDismiss = { showAddFriendDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friend: UserProfile,
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val habits by userViewModel.friendHabits.collectAsState()
    val isLoading by userViewModel.isFriendHabitsLoading.collectAsState()

    LaunchedEffect(friend.uid) {
        userViewModel.loadFriendHabits(friend.uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("@${friend.username}")
                        Text("Takip Ediliyor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (habits.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bu kullanıcının henüz görünür bir alışkanlığı yok.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp), 
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Kartlar arası boşluk
                ) {
                    items(habits) { habit ->
                        FriendHabitCard(habit = habit)
                    }
                }
            }
        }
    }
}

// --- YENİ: ARKADAŞ ALIŞKANLIK KARTI (Gelişmiş Tasarım) ---
@Composable
fun FriendHabitCard(habit: Habit) {
    val isAlive = habit.currentLives > 0
    
    // Hesaplamalar
    val currentXp = calculateXp(habit)
    val rankInfo = getRankFromXp(currentXp)
    
    // Zaman Hesaplaması (HomeScreen ile aynı)
    val lastResetTime = if (habit.relapseHistory.isNotEmpty()) habit.relapseHistory.last() else habit.startTime
    val currentTime = System.currentTimeMillis()
    val hoursPassed = TimeUnit.MILLISECONDS.toHours(currentTime - lastResetTime)
    
    // İlerleme Hesaplaması
    val isRegenerating = habit.currentLives < habit.maxLives
    val daysNeeded = when(habit.difficulty) { 1 -> 10L; 2 -> 7L; 3 -> 5L; else -> 7L }
    val timePassedSinceRegen = currentTime - (if (habit.lastRegenerationTime > 0) habit.lastRegenerationTime else lastResetTime)
    val totalTimeNeeded = TimeUnit.DAYS.toMillis(daysNeeded)
    val rawProgress = if (totalTimeNeeded > 0) (timePassedSinceRegen.toFloat() / totalTimeNeeded.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = rawProgress, animationSpec = tween(1000))

    // Renkler ve Metinler
    val progressRingColor = when {
        !isAlive -> Color.Gray
        isRegenerating -> Color(0xFF4CAF50)
        hoursPassed < 24 -> Color(0xFFE53935)
        hoursPassed < 24 * 30 -> Color(0xFF43A047)
        else -> Color(0xFFFFD700)
    }

    val (displayValue, displayUnit) = if (hoursPassed < 24) { Pair("$hoursPassed", "Saat") } else { Pair("${hoursPassed / 24}", "Gün") }

    // Arka Plan
    val cardBackgroundBrush = if (isAlive) {
        Brush.horizontalGradient(
            colors = listOf(MaterialTheme.colorScheme.surface, rankInfo.color.copy(alpha = 0.1f))
        )
    } else {
        SolidColor(Color(0xFFFFEBEE))
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.background(cardBackgroundBrush)) {
            Column(modifier = Modifier.padding(20.dp)) {
                
                // ÜST: İsim ve Kalpler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isAlive) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    // Arkadaşın can durumu
                    Row {
                        repeat(habit.maxLives) { index ->
                            val heartColor = if (index < habit.currentLives) Color(0xFFE91E63) else Color.LightGray.copy(alpha = 0.5f)
                            val icon = if (index < habit.currentLives) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                            Icon(imageVector = icon, contentDescription = null, tint = heartColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // ORTA: İlerleme ve Bilgiler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Halka
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(70.dp), color = MaterialTheme.colorScheme.surfaceVariant, strokeWidth = 6.dp)
                        CircularProgressIndicator(
                            progress = { if (isRegenerating) animatedProgress else 1f }, 
                            modifier = Modifier.size(70.dp), 
                            color = progressRingColor, 
                            strokeWidth = 6.dp, 
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = displayValue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = displayUnit, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Detaylar
                    Column(modifier = Modifier.weight(1f)) {
                        // Rütbe
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = rankInfo.color.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rankInfo.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rankInfo.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = rankInfo.color
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Durum Mesajı (Buton Yerine)
                        if (isAlive) {
                            Text(
                                text = "🔥 Mücadele Ediyor",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "💀 Mücadeleyi Kaybetti",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ... (Diğer bileşenler: CreateProfileScreen, AddFriendDialog, RequestItem, FriendItem aynı kalıyor) ...
// Kod tekrarını önlemek için aşağısı aynı.
@Composable
fun RequestItem(user: UserProfile, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("@${user.username}", style = MaterialTheme.typography.titleMedium)
                    Text("Arkadaşlık isteği", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Row {
                IconButton(onClick = onDecline) { Icon(Icons.Default.Close, contentDescription = "Reddet", tint = Color.Red) }
                IconButton(onClick = onAccept) { Icon(Icons.Default.Check, contentDescription = "Kabul Et", tint = Color(0xFF4CAF50)) }
            }
        }
    }
}

@Composable
fun CreateProfileScreen(viewModel: UserViewModel) {
    var username by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profilini Oluştur", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Arkadaşlarının seni bulabilmesi için benzersiz bir kullanıcı adı seç.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = username, onValueChange = { username = it.trim().lowercase() }, label = { Text("Kullanıcı Adı") }, prefix = { Text("@") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { if (username.isNotBlank()) viewModel.updateUsername(username) }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet ve Başla") }
    }
}

@Composable
fun AddFriendDialog(userViewModel: UserViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val searchResult by userViewModel.searchResult.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    val successMessage by userViewModel.successMessage.collectAsState()
    LaunchedEffect(successMessage) { if (successMessage != null) { onDismiss(); userViewModel.clearSuccess() } }
    AlertDialog(
        onDismissRequest = { onDismiss(); userViewModel.clearError() },
        title = { Text("Arkadaş Ekle") },
        text = {
            Column {
                Text("Arkadaşının kullanıcı adını gir:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.trim().lowercase() },
                    placeholder = { Text("kullaniciadi") },
                    trailingIcon = { IconButton(onClick = { userViewModel.searchUser(query) }) { Icon(Icons.Default.Search, contentDescription = "Ara") } }
                )
                if (errorMessage != null) Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                if (searchResult != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("@${searchResult!!.username}", fontWeight = FontWeight.Bold)
                            Button(onClick = { userViewModel.sendFriendRequest(searchResult!!.uid) }) { Text("İstek Gönder") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } }
    )
}

@Composable
fun FriendItem(friend: UserProfile, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text("@${friend.username}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
