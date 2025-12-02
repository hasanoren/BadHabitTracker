package com.hasan.badhabit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habit: Habit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Hata Takvimi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Takvim Bileşeni
            RelapseCalendar(relapseHistory = habit.relapseHistory)
        }
    }
}

@Composable
fun RelapseCalendar(relapseHistory: List<Long>) {
    // Görüntülenen Tarih (Varsayılan: Bugün)
    // remember { mutableStateOf(...) } kullanarak ekran yeniden çizildiğinde tarihi koruyoruz.
    var displayedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val currentMonth = displayedDate.get(Calendar.MONTH)
    val currentYear = displayedDate.get(Calendar.YEAR)
    
    // Ayın kaç gün çektiğini bul
    // Kopyasını oluşturup hesaplıyoruz ki displayedDate bozulmasın
    val cal = displayedDate.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Ayın ilk gününün haftanın hangi günü olduğunu bul (Pzt=0...Paz=6)
    // Calendar.DAY_OF_WEEK: Pazar=1, Pzt=2...
    val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayedDate.time)

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Ay Değiştirme Satırı ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = displayedDate.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    displayedDate = newCal
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Önceki Ay")
                }

                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    val newCal = displayedDate.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    displayedDate = newCal
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Sonraki Ay")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Gün İsimleri ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // --- Günler Grid'i ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Boşluklar
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.size(40.dp))
                }

                // Günler
                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val isRelapseDay = isDateInHistory(day, currentMonth, currentYear, relapseHistory)
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp) // Kare değil daire olsun diye size sabitliyoruz
                            .background(
                                color = if (isRelapseDay) Color(0xFFE57373) else Color(0xFFEEEEEE),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            color = if (isRelapseDay) Color.White else Color.Black,
                            fontWeight = if (isRelapseDay) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lejant
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(Color(0xFFE57373), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hata Yapılan Günler", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun isDateInHistory(day: Int, month: Int, year: Int, history: List<Long>): Boolean {
    val cal = Calendar.getInstance()
    for (timestamp in history) {
        cal.timeInMillis = timestamp
        if (cal.get(Calendar.DAY_OF_MONTH) == day &&
            cal.get(Calendar.MONTH) == month &&
            cal.get(Calendar.YEAR) == year
        ) {
            return true
        }
    }
    return false
}
