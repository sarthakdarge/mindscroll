package com.example.reelstracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.reelstracker.data.LimitManager
import com.example.reelstracker.data.QuotesProvider
import com.example.reelstracker.data.ReelHistoryManager
import com.example.reelstracker.ui.theme.ReelsTrackerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            ReelsTrackerTheme {
                Surface {
                    TodayStatsScreen()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
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
    var showLimitPicker by remember { mutableStateOf(false) }

    val historyManager = remember { ReelHistoryManager(context) }
    val limitManager = remember { LimitManager(context) }

    LaunchedEffect(Unit) {
        dailyLimitMin = (limitManager.getDailyLimit() / 60000).toInt()

        while (true) {
            val today = historyManager.getLast7Days().lastOrNull()
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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "MindScroll",
            style = MaterialTheme.typography.headlineSmall
        )

        // ⏱ Watch Time Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Today's Watch Time",
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
                        .height(6.dp)
                )
            }
        }

        // 🎞 Reels Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Reels Watched Today",
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
                )
            ) {
                Text(
                    text = quote,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 🎯 Daily Limit Card (CLICKABLE)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLimitPicker = true },
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Daily Limit",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "$dailyLimitMin min",
                    style = MaterialTheme.typography.displaySmall
                )
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

    // ✅ LIMIT PICKER DIALOG
    if (showLimitPicker) {
        LimitPickerDialog(
            initialMinutes = dailyLimitMin,
            onDismiss = { showLimitPicker = false },
            onConfirm = {
                dailyLimitMin = it
                limitManager.setDailyLimit(it * 60000L)
                showLimitPicker = false
            }
        )
    }
}

@Composable
fun LimitPickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(initialMinutes / 60) }
    var minutes by remember { mutableIntStateOf((initialMinutes % 60) / 5 * 5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set app timer") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "This app timer will reset at midnight",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    TimePickerColumn(
                        value = hours,
                        range = 0..5,
                        label = "hrs",
                        onChange = { hours = it }
                    )
                    TimePickerColumn(
                        value = minutes,
                        range = 0..55 step 5,
                        label = "mins",
                        onChange = { minutes = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(hours * 60 + minutes)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimePickerColumn(
    value: Int,
    range: IntProgression,
    label: String,
    onChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label)
        Spacer(Modifier.height(8.dp))
        DropdownMenuBox(value, range, onChange)
    }
}

@Composable
fun DropdownMenuBox(
    value: Int,
    range: IntProgression,
    onChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            range.forEach {
                DropdownMenuItem(
                    text = { Text(it.toString()) },
                    onClick = {
                        onChange(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
