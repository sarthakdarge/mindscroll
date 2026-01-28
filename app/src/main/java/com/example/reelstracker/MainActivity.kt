package com.example.reelstracker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reelstracker.data.AppDatabase
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TodayStatsScreen()
            }
        }
    }
}

@Composable
fun TodayStatsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var reelCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val today = java.time.LocalDate.now().toString()

            val stats = withContext(kotlinx.coroutines.Dispatchers.IO) {
                AppDatabase.get(context)
                    .reelDao()
                    .getStatsForDate(today)
            }

            reelCount = stats?.reelCount ?: 0

            kotlinx.coroutines.delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "MindScroll",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Reels watched today",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = reelCount.toString(),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            )
        }) {
            Text("Enable Accessibility")
        }
    }
}
