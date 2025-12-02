package com.hasan.badhabit

import AuthViewModel
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings // Güneş/Ay ikonu yerine kullanılabilir veya custom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onUpdateNotificationTime: (Int, Int) -> Unit = { _, _ -> },
    // YENİ: Tema Kontrol Parametreleri
    isDarkTheme: Boolean = false, 
    onThemeSwitch: () -> Unit = {}
) {
    val myProfile by userViewModel.myProfile.collectAsState()
    val friends by userViewModel.friendsList.collectAsState()
    val successMessage by userViewModel.successMessage.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bad_habit_prefs", Context.MODE_PRIVATE) }

    var notificationHour by remember { mutableIntStateOf(prefs.getInt("notif_hour", 21)) }
    var notificationMinute by remember { mutableIntStateOf(prefs.getInt("notif_minute", 0)) }
    
    var currentMessage by remember { mutableStateOf(prefs.getString("notif_message", NotificationMessages.DEFAULT_MESSAGE) ?: NotificationMessages.DEFAULT_MESSAGE) }
    var showMessageDialog by remember { mutableStateOf(false) }

    var isEditingUsername by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    
    var showAvatarSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            notificationHour = hour
            notificationMinute = minute
            onUpdateNotificationTime(hour, minute)
            Toast.makeText(context, "Hatırlatıcı saati güncellendi", Toast.LENGTH_SHORT).show()
        },
        notificationHour,
        notificationMinute,
        true
    )

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            userViewModel.clearSuccess()
            isEditingUsername = false
            showAvatarSheet = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilim", fontWeight = FontWeight.Bold) },
                // YENİ: Tema Değiştirme Butonu
                actions = {
                    IconButton(onClick = onThemeSwitch) {
                        // Eğer simgeler yoksa R.drawable kullanabilirsin ama şimdilik text/mevcut ikon ile çözelim
                        // Gece ise Güneş, Gündüz ise Ay göster
                        val iconRes = if (isDarkTheme) R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_foreground // Geçici, aşağıda icon kullanacağım
                        // Material Icon kullanımı:
                        Icon(
                            // Not: Tam Güneş/Ay ikonları Material kütüphanesinin extended kısmında olabilir.
                            // Burada basitçe Settings ikonu veya varsa uygun bir ikon kullanıyorum.
                            // Sizin projenizde R.drawable ekleyebiliriz ama şimdilik basit tutalım.
                            imageVector = Icons.Default.Settings, // Temsili ikon
                            contentDescription = "Tema Değiştir",
                            tint = if (isDarkTheme) Color.Yellow else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // 1. PROFİL KARTI
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { showAvatarSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarResId = AvatarHelper.getIconById(myProfile?.avatarId ?: "default")
                                Image(
                                    painter = painterResource(id = avatarResId),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp).clickable { showAvatarSheet = true }.offset(x = (-2).dp, y = (-2).dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEditingUsername) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newUsername,
                                    onValueChange = { newUsername = it.trim().lowercase() },
                                    label = { Text("Kullanıcı Adı") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { if (newUsername.isNotBlank()) userViewModel.updateUsername(newUsername) }) { Text("Kaydet") }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${myProfile?.username ?: "..."}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { newUsername = myProfile?.username ?: ""; isEditingUsername = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(text = myProfile?.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }

                // 2. AYARLAR BÖLÜMÜ
                Text(
                    text = "Ayarlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Default.Notifications,
                            title = "Hatırlatma Saati",
                            subtitle = String.format("%02d:%02d", notificationHour, notificationMinute),
                            onClick = { timePickerDialog.show() }
                        )
                        
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surface)

                        SettingItem(
                            icon = Icons.Default.Email,
                            title = "Motivasyon Mesajı",
                            subtitle = currentMessage,
                            onClick = { showMessageDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. ARKADAŞLAR LİSTESİ
                Text(
                    text = "Arkadaşlar (${friends.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                if (friends.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Henüz arkadaşın yok.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(friends) { friend ->
                            FriendRowItem(friend = friend, onRemove = { userViewModel.removeFriend(friend.uid) })
                        }
                    }
                }

                Button(
                    onClick = { authViewModel.signOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Çıkış Yap", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // ... (Avatar ve Mesaj Dialogları aynı) ...
        if (showAvatarSheet) {
            ModalBottomSheet(onDismissRequest = { showAvatarSheet = false }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bir Avatar Seç", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        items(AvatarHelper.avatars) { avatar ->
                            val isSelected = myProfile?.avatarId == avatar.id
                            Box(modifier = Modifier.size(80.dp).clip(CircleShape).border(width = if (isSelected) 3.dp else 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, shape = CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent).clickable { userViewModel.updateAvatar(avatar.id) }, contentAlignment = Alignment.Center) {
                                Image(painter = painterResource(id = avatar.resId), contentDescription = null, modifier = Modifier.size(60.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
        
        if (showMessageDialog) {
            AlertDialog(
                onDismissRequest = { showMessageDialog = false },
                title = { Text("Motivasyon Mesajını Seç") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        NotificationMessages.messages.forEach { message ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { prefs.edit().putString("notif_message", message).apply(); currentMessage = message; showMessageDialog = false }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (message == currentMessage), onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showMessageDialog = false }) { Text("İptal") } }
            )
        }
    }
}

// ... Yardımcı bileşenler (SettingItem, FriendRowItem) aynı ...
@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun FriendRowItem(friend: UserProfile, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val friendAvatarRes = AvatarHelper.getIconById(friend.avatarId)
            Image(painter = painterResource(id = friendAvatarRes), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp))
            Text("@${friend.username}", fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)) }
    }
}
