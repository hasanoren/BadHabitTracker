package com.hasan.badhabit

import android.content.Context
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    habitViewModel: HabitViewModel
) {
    val habits by habitViewModel.habits.collectAsState()
    val isLoading by habitViewModel.isLoading.collectAsState()
    
    var habitToRelapse by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var showRankInfo by remember { mutableStateOf(false) }
    var showFailAnim by remember { mutableStateOf(false) }
    var showLevelUpAnim by remember { mutableStateOf(false) }
    var newRankName by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("bad_habit_prefs", Context.MODE_PRIVATE) }

    LaunchedEffect(habits) {
        if (habits.isNotEmpty()) {
            val maxHabit = habits.maxByOrNull { getRankFromXp(calculateXp(it)).level }
            if (maxHabit != null) {
                val currentRankInfo = getRankFromXp(calculateXp(maxHabit))
                val currentLevel = currentRankInfo.level
                val savedLevel = sharedPrefs.getInt("last_seen_level", -1)
                
                if (currentLevel > savedLevel) {
                    newRankName = currentRankInfo.name
                    showLevelUpAnim = true
                    sharedPrefs.edit().putInt("last_seen_level", currentLevel).apply()
                } else if (currentLevel < savedLevel) {
                    sharedPrefs.edit().putInt("last_seen_level", currentLevel).apply()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kötü Alışkanlık Takibi", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = { showRankInfo = true }) { Icon(Icons.Default.Info, contentDescription = "Rütbe Bilgisi") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_habit") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) { Icon(Icons.Default.Add, contentDescription = "Ekle") }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 4.dp) }
            } else {
                if (habits.isEmpty()) {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.yoga))
                        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                        LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(250.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Henüz bir savaşın yok.", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Yeni bir sayfa açmak için (+) butonuna bas!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(items = habits, key = { it.id }) { habit ->
                            val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { habitToDelete = habit; return@rememberSwipeToDismissBoxState false }; false })
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = { Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(Color(0xFFE53935)).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White, modifier = Modifier.size(32.dp)) } },
                                content = { ModernHabitCard(habit = habit, onRelapseClick = { habitToRelapse = habit }, onClick = { navController.navigate("habit_detail/${habit.id}") }) }
                            )
                        }
                    }
                }
            }
            
            if (showRankInfo) RankInfoDialog(onDismiss = { showRankInfo = false })
            
            if (showLevelUpAnim) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable { showLevelUpAnim = false }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti)); val progress by animateLottieCompositionAsState(composition, iterations = 1); LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.fillMaxSize()); Text("TEBRİKLER!", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = Color.White); Text("Yeni Rütbe: $newRankName", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp)); Text("Ekrana dokunarak devam et", color = Color.White.copy(alpha = 0.7f)); if (progress >= 0.99f) LaunchedEffect(Unit) { showLevelUpAnim = false } } } }
            if (showFailAnim) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) { val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart_break)); val progress by animateLottieCompositionAsState(composition, iterations = 1, isPlaying = true); LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(300.dp)); if (progress >= 0.99f) LaunchedEffect(Unit) { habitToRelapse?.let { habitViewModel.relapseHabit(it) }; habitToRelapse = null; showFailAnim = false } } }
            if (habitToRelapse != null && !showFailAnim) { AlertDialog(onDismissRequest = { habitToRelapse = null }, icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) }, title = { Text("Mücadele Zorludur") }, text = { Text("Pes etmek yok, sadece bir tökezleme. Dürüst müsün? 1 Canın azalacak.") }, confirmButton = { Button(onClick = { showFailAnim = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Evet, Yaptım") } }, dismissButton = { TextButton(onClick = { habitToRelapse = null }) { Text("Vazgeç") } }) }
            if (habitToDelete != null) { AlertDialog(onDismissRequest = { habitToDelete = null }, icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }, title = { Text("Veda mı?") }, text = { Text("\"${habitToDelete?.name}\" kaydını siliyorsun. Bu işlem geri alınamaz.") }, confirmButton = { Button(onClick = { habitToDelete?.let { habitViewModel.deleteHabit(it.id) }; habitToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Sil") } }, dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("Vazgeç") } }) }
        }
    }
}

@Composable
fun SegmentedProgressBar(
    totalSegments: Int,
    filledSegments: Int,
    color: Color = Color(0xFF4CAF50)
) {
    val segments = if (totalSegments > 0) totalSegments else 1
    Row(
        modifier = Modifier.fillMaxWidth().height(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until segments) {
            val isFilled = i < filledSegments
            val segmentColor = if (isFilled) color else Color.LightGray.copy(alpha = 0.5f)
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(segmentColor))
        }
    }
}

@Composable
fun ModernHabitCard(
    habit: Habit,
    onRelapseClick: () -> Unit,
    onClick: () -> Unit
) {
    val isAlive = habit.currentLives > 0
    val lastResetTime = if (habit.relapseHistory.isNotEmpty()) habit.relapseHistory.last() else habit.startTime
    val currentTime = System.currentTimeMillis()
    val hoursPassed = TimeUnit.MILLISECONDS.toHours(currentTime - lastResetTime)
    val multiplier = 10 
    val currentScore = hoursPassed * multiplier
    val rankInfo = getRankFromXp(currentScore)

    val isRegenerating = habit.currentLives < habit.maxLives
    val daysNeeded = when(habit.difficulty) { 1 -> 10L; 2 -> 7L; 3 -> 5L; else -> 7L }
    val baseTime = if (habit.lastRegenerationTime > 0) habit.lastRegenerationTime else lastResetTime
    val timePassedSinceRegen = currentTime - baseTime
    
    val totalTimeNeeded = TimeUnit.DAYS.toMillis(daysNeeded)
    
    // GÜVENLİ HESAPLAMA: Sıfıra bölme koruması
    val rawProgress = if (totalTimeNeeded > 0) {
        (timePassedSinceRegen.toFloat() / totalTimeNeeded.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    
    val animatedProgress by animateFloatAsState(targetValue = rawProgress, animationSpec = tween(1000))
    
    val daysPassed = TimeUnit.MILLISECONDS.toDays(timePassedSinceRegen).toInt()
    val totalDays = daysNeeded.toInt()
    val filledSegments = daysPassed.coerceAtMost(totalDays)
    val daysRemaining = (totalDays - daysPassed).coerceAtLeast(0)

    // XP PROGRESS HESABI (GÜVENLİ)
    val xpProgress = if (rankInfo.nextRankXp != null) {
        val prevRankXp = when (rankInfo.level) {
            0 -> 0L 
            1 -> 500L 
            2 -> 1500L
            3 -> 4000L 
            4 -> 10000L
            else -> 0L
        }
        
        val totalXpInRank = (rankInfo.nextRankXp - prevRankXp).coerceAtLeast(1) // 0 olmasın
        val currentXpInRank = currentScore - prevRankXp
        
        (currentXpInRank.toFloat() / totalXpInRank.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    val animatedXpProgress by animateFloatAsState(targetValue = xpProgress, animationSpec = tween(1000))

    val progressRingColor = when {
        isRegenerating -> Color(0xFF4CAF50)
        hoursPassed < 24 -> Color(0xFFE53935)
        hoursPassed < 24 * 7 -> Color(0xFFFFB300)
        hoursPassed < 24 * 30 -> Color(0xFF43A047)
        hoursPassed < 24 * 365 -> Color(0xFF1E88E5)
        else -> Color(0xFFFFD700)
    }

    val (displayValue, displayUnit) = if (hoursPassed < 24) { Pair("$hoursPassed", "Saat") } else { Pair("${hoursPassed / 24}", "Gün") }
    val cardBackgroundBrush = if (isAlive) { Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.surface, rankInfo.color.copy(alpha = 0.1f))) } else { SolidColor(Color(0xFFFFEBEE)) }

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Box(modifier = Modifier.background(cardBackgroundBrush)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = habit.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = if (isAlive) MaterialTheme.colorScheme.onSurface else Color.Gray, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClick) { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Detaylar", tint = MaterialTheme.colorScheme.primary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        repeat(habit.maxLives) { index ->
                            val heartColor = if (index < habit.currentLives) Color(0xFFE91E63) else Color.LightGray.copy(alpha = 0.5f)
                            val icon = if (index < habit.currentLives) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                            Icon(imageVector = icon, contentDescription = null, tint = heartColor, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(80.dp), color = MaterialTheme.colorScheme.surfaceVariant, strokeWidth = 8.dp)
                        CircularProgressIndicator(progress = { animatedXpProgress }, modifier = Modifier.size(80.dp), color = progressRingColor, strokeWidth = 8.dp, strokeCap = StrokeCap.Round)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = displayValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = displayUnit, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(shape = RoundedCornerShape(50), color = rankInfo.color.copy(alpha = 0.15f), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(rankInfo.icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = rankInfo.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = rankInfo.color)
                                    Text(text = "$currentScore XP", style = MaterialTheme.typography.labelSmall, color = rankInfo.color.copy(alpha = 0.8f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isAlive && rankInfo.nextRankXp != null) {
                            val xpNeeded = rankInfo.nextRankXp - currentScore
                            val percentage = (xpProgress * 100).toInt()
                            Column {
                                Text(text = "Sonraki Seviye: %$percentage", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "+$multiplier XP/Saat", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                             Text(text = "+$multiplier XP/Saat", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isRegenerating) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Can doluyor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("$daysRemaining Gün", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                SegmentedProgressBar(totalSegments = totalDays, filledSegments = filledSegments, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (habit.motivation.isNotBlank() && isAlive) {
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text(text = "\"${habit.motivation}\"", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (isAlive) {
                    val livesMotivation = when {
                        habit.currentLives == habit.maxLives -> "🔥 Kusursuz ilerliyorsun! Bu iradeyle her şeyi başarırsın."
                        habit.currentLives == habit.maxLives - 1 -> "🛡️ Ufak bir tökezleme oldu ama hala güçlüsün. Toparlan!"
                        habit.currentLives == 1 -> "⚔️ Dikkat! Son şansın kaldı. İradene sahip çık!"
                        else -> "Pes etmek yok, devam et!"
                    }
                    Text(text = livesMotivation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = if (habit.currentLives == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRelapseClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer), modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Maalesef Bozdum", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEBEE), RoundedCornerShape(16.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
                        Text(text = "💀 OYUN BİTTİ", fontWeight = FontWeight.Black, color = Color(0xFFD32F2F), letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

// ... RankInfoDialog ve RankRow aynı ...
@Composable
fun RankInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rütbe Sistemi", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Temiz kaldığın her saat XP kazandırır.")
                Divider()
                RankRow("Acemi", "🥚", "0 - 499 XP", Color.Gray)
                RankRow("Kararlı", "🌱", "500 - 1,499 XP", Color(0xFF4CAF50))
                RankRow("Savaşçı", "⚔️", "1,500 - 3,999 XP", Color(0xFF2196F3))
                RankRow("Usta", "🛡️", "4,000 - 9,999 XP", Color(0xFF9C27B0))
                RankRow("Efsane", "👑", "10,000+ XP", Color(0xFFFFC107))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tamam") } }
    )
}

@Composable
fun RankRow(name: String, icon: String, range: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, fontWeight = FontWeight.Bold, color = color)
        }
        Text(text = range, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}
