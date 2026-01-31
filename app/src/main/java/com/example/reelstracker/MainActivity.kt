package com.example.reelstracker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.reelstracker.data.LimitManager
import com.example.reelstracker.data.QuotesProvider
import com.example.reelstracker.data.ReelHistoryManager
import com.example.reelstracker.ui.theme.ReelsTrackerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReelsTrackerTheme {
                Surface {
                    TodayStatsScreen()
                }
            }
        }
    }
}

@Composable
fun TodayStatsScreen() {
    val context = LocalContext.current

    var reelCount by remember { mutableIntStateOf(0) }
    var watchTimeMs by remember { mutableLongStateOf(0L) }
    var dailyLimitMin by remember { mutableIntStateOf(30) }
    var quote by remember { mutableStateOf("") }

    val historyManager = remember { ReelHistoryManager(context) }
    val limitManager = remember { LimitManager(context) }

    LaunchedEffect(Unit) {
        dailyLimitMin = (limitManager.getDailyLimit() / 60000).toInt()

        while (true) {
            val todayDate = java.time.LocalDate.now().toString()
            val today = historyManager.getLast7Days()
                .find { it.date == todayDate }

            reelCount = today?.reelCount ?: 0
            watchTimeMs = today?.timeSpentMs ?: 0L

            if (reelCount >= 20 && quote.isEmpty()) {
                quote = QuotesProvider.randomQuote()
            }

            delay(2000)
        }
    }

    val watchMinutes by animateIntAsState(
        targetValue = (watchTimeMs / 60000).toInt(),
        animationSpec = tween(600),
        label = "watchTimeAnim"
    )

    val progress =
        if (dailyLimitMin == 0) 0f
        else (watchMinutes / dailyLimitMin.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "MindScroll",
            style = MaterialTheme.typography.headlineSmall
        )

        // ⏱ Watch Time
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Today's Watch Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "$watchMinutes min",
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // 🎞 Reels Count
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Reels Watched Today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "$reelCount reels",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }

        // 🌱 Quote (only after 20 reels)
        AnimatedVisibility(
            visible = reelCount >= 20,
            enter = fadeIn(tween(600)),
            exit = fadeOut(tween(300))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Text(
                    text = quote,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 🎯 Daily Limit
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Daily Limit (minutes)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = dailyLimitMin.toString(),
                    onValueChange = { newValue ->
                        dailyLimitMin = newValue.toIntOrNull() ?: dailyLimitMin
                    },
                    singleLine = true,
                    modifier = Modifier.width(120.dp)
                )


                Button(
                    onClick = {
                        limitManager.setDailyLimit(dailyLimitMin * 60000L)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Enable Accessibility")
        }
    }
}
