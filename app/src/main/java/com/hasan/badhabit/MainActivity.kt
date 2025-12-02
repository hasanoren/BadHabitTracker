package com.hasan.badhabit

import AuthViewModel
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val prefs = getSharedPreferences("bad_habit_prefs", Context.MODE_PRIVATE)
            val hour = prefs.getInt("notif_hour", 21)
            val minute = prefs.getInt("notif_minute", 0)
            scheduleDailyReminder(hour, minute)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("bad_habit_prefs", Context.MODE_PRIVATE)
        val savedHour = prefs.getInt("notif_hour", 21)
        val savedMinute = prefs.getInt("notif_minute", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleDailyReminder(savedHour, savedMinute)
            }
        } else {
            scheduleDailyReminder(savedHour, savedMinute)
        }

        setContent {
            // Tema State'i
            val systemDark = isSystemInDarkTheme()
            // Eğer kayıtlı bir tercih yoksa sistem temasını kullan
            // SharedPreferences'ta boolean saklarken default value önemli
            // Burada "is_dark_mode" anahtarı yoksa -1 gibi bir şey dönemeyiz, o yüzden
            // contains ile kontrol edip varsa alacağız, yoksa sistem temasını kullanacağız.
            val isDarkSaved = if (prefs.contains("is_dark_mode")) {
                prefs.getBoolean("is_dark_mode", false)
            } else {
                systemDark
            }
            
            var isDarkTheme by remember { mutableStateOf(isDarkSaved) }

            // Basit Renk Paletleri (Eğer Theme.kt yoksa diye güvenli liman)
            val darkColors = darkColorScheme(
                primary = Color(0xFFBB86FC),
                secondary = Color(0xFF03DAC6),
                background = Color(0xFF121212),
                surface = Color(0xFF121212),
                onPrimary = Color.Black,
                onSecondary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White,
            )

            val lightColors = lightColorScheme(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC6),
                background = Color.White,
                surface = Color.White,
                onPrimary = Color.White,
                onSecondary = Color.Black,
                onBackground = Color.Black,
                onSurface = Color.Black,
            )
            
            // Eğer projenizde "BadHabitTrackerTheme" varsa onu kullanın:
            // BadHabitTrackerTheme(darkTheme = isDarkTheme) { ... }
            // Ama ben garanti olsun diye MaterialTheme kullanıyorum:
            
            MaterialTheme(
                colorScheme = if (isDarkTheme) darkColors else lightColors
            ) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val habitViewModel: HabitViewModel = viewModel()
                val userViewModel: UserViewModel = viewModel()

                val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState(initial = false)
                val habits by habitViewModel.habits.collectAsState()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf("home", "friends", "profile") && isUserLoggedIn

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Ana Sayfa") },
                                    label = { Text("Takip") },
                                    selected = currentRoute == "home",
                                    onClick = { if (currentRoute != "home") navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Arkadaşlar") },
                                    label = { Text("Arkadaşlar") },
                                    selected = currentRoute == "friends",
                                    onClick = { if (currentRoute != "friends") navController.navigate("friends") { popUpTo("home") } }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profil") },
                                    label = { Text("Profil") },
                                    selected = currentRoute == "profile",
                                    onClick = { if (currentRoute != "profile") navController.navigate("profile") { popUpTo("home") } }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController, 
                        startDestination = if (isUserLoggedIn) "home" else "auth",
                        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                    ) {
                        composable("auth") {
                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { navController.navigate("home") { popUpTo("auth") { inclusive = true } } }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                habitViewModel = habitViewModel
                            )
                        }
                        composable("friends") { FriendsScreen(userViewModel = userViewModel) }
                        
                        composable("profile") { 
                            ProfileScreen(
                                userViewModel = userViewModel, 
                                authViewModel = authViewModel,
                                onUpdateNotificationTime = { hour, minute ->
                                    prefs.edit().putInt("notif_hour", hour).putInt("notif_minute", minute).apply()
                                    scheduleDailyReminder(hour, minute)
                                },
                                // YENİ: Tema parametrelerini gönderiyoruz
                                isDarkTheme = isDarkTheme,
                                onThemeSwitch = {
                                    val newMode = !isDarkTheme
                                    isDarkTheme = newMode
                                    prefs.edit().putBoolean("is_dark_mode", newMode).apply()
                                }
                            ) 
                        }
                        
                        composable("add_habit") {
                            AddHabitScreen(
                                habitViewModel = habitViewModel,
                                onHabitAdded = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "habit_detail/{habitId}",
                            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId")
                            val selectedHabit = habits.find { it.id == habitId }
                            
                            if (selectedHabit != null) {
                                HabitDetailScreen(
                                    habit = selectedHabit,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun scheduleDailyReminder(hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelUniqueWork("DailyReminder")

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, hour)
        dueDate.set(Calendar.MINUTE, minute)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        Log.d("DailyReminder", "Yeni hatırlatıcı kuruluyor: $hour:$minute (Fark: ${timeDiff / 1000} sn)")

        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "DailyReminder",
            ExistingWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }
}
