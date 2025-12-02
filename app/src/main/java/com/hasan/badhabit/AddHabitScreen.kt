package com.hasan.badhabit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitViewModel: HabitViewModel,
    onHabitAdded: () -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var motivation by remember { mutableStateOf("") }
    var difficulty by remember { mutableIntStateOf(1) }
    var showSuccessAnim by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Arka Plan Gradyanı
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. BAŞLIK VE İKON
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star, // Roket yerine Star kullandık
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Yeni Bir Sayfa Aç",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Kendine bir iyilik yap ve bugün başla.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // 2. GİRİŞ FORMU
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    
                    // İsim
                    Text("Alışkanlık", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        placeholder = { Text("Örn: Sigara, Şeker, Tırnak Yeme") },
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Motivasyon
                    Text("Motivasyon (Opsiyonel)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivation,
                        onValueChange = { motivation = it },
                        placeholder = { Text("Neden bırakmak istiyorsun?") },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Zorluk Seviyesi (Gelişmiş Seçici)
                    Text("Zorluk Seviyesi", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DifficultyCard(
                            label = "Kolay",
                            icon = Icons.Default.Face, // Gülen yüz
                            level = 1,
                            selectedLevel = difficulty,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f),
                            onSelect = { difficulty = 1 }
                        )
                        DifficultyCard(
                            label = "Orta",
                            icon = Icons.Default.ThumbUp,
                            level = 2,
                            selectedLevel = difficulty,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f),
                            onSelect = { difficulty = 2 }
                        )
                        DifficultyCard(
                            label = "Zor",
                            icon = Icons.Default.Warning, // Alev/Uyarı
                            level = 3,
                            selectedLevel = difficulty,
                            color = Color(0xFFF44336),
                            modifier = Modifier.weight(1f),
                            onSelect = { difficulty = 3 }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bilgi Kutusu
                    val (cooldown, description) = when(difficulty) {
                        1 -> "10 Gün" to "Sabır gerektirir. Hata yaparsan uzun süre beklersin."
                        2 -> "7 Gün" to "Dengeli bir başlangıç için ideal."
                        3 -> "5 Gün" to "Zorlu mücadele! Hızlı toparlanma şansı."
                        else -> "" to ""
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Can Yenilenme: $cooldown",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Başlat Butonu
                    Button(
                        onClick = {
                            if (habitName.isNotBlank()) {
                                habitViewModel.addHabit(habitName, motivation, difficulty)
                                showSuccessAnim = true
                            } else {
                                Toast.makeText(context, "Lütfen bir alışkanlık adı girin", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text("Takibi Başlat", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // Alt boşluk
        }
        
        // Konfeti Animasyonu
        if (showSuccessAnim) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = 1,
                    isPlaying = true
                )
                
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
                
                if (progress >= 0.99f) {
                    LaunchedEffect(Unit) { onHabitAdded() }
                }
            }
        }
    }
}

@Composable
fun DifficultyCard(
    label: String,
    icon: ImageVector,
    level: Int,
    selectedLevel: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val isSelected = level == selectedLevel
    val borderColor = if (isSelected) color else Color.Transparent
    val containerColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (isSelected) color else Color.Gray

    Surface(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
