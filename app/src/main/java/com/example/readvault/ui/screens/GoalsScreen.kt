package com.example.readvault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.data.model.Badge
import com.example.readvault.data.model.ReadingChallenge
import com.example.readvault.data.model.ReadingGoal
import com.example.readvault.data.model.ReadingSession
import com.example.readvault.ui.viewmodel.ReadVaultViewModel

@Composable
fun GoalsScreen(
    viewModel: ReadVaultViewModel,
    modifier: Modifier = Modifier
) {
    val goal by viewModel.readingGoal.collectAsState(initial = ReadingGoal())
    val badges by viewModel.badges.collectAsState(initial = emptyList())
    val challenges by viewModel.challenges.collectAsState(initial = emptyList())
    val sessions by viewModel.readingSessions.collectAsState()

    var showEditGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Reading Goals & Streaks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Build a lasting daily reading habit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showEditGoalDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Goals")
                }
            }
        }

        // Streak & Daily Summary Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "${goal.currentStreakDays} Day Streak!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Longest streak: ${goal.longestStreakDays} days • Active habit",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Daily Goals Rings / Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalMetricCard(
                    title = "Daily Time",
                    value = "${goal.minutesReadToday} / ${goal.dailyMinutesGoal} m",
                    percent = (goal.minutesReadToday.toFloat() / goal.dailyMinutesGoal).coerceIn(0f, 1f),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                GoalMetricCard(
                    title = "Daily Pages",
                    value = "${goal.pagesReadToday} / ${goal.dailyPagesGoal} p",
                    percent = (goal.pagesReadToday.toFloat() / goal.dailyPagesGoal).coerceIn(0f, 1f),
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                GoalMetricCard(
                    title = "Year Challenge",
                    value = "${goal.booksReadThisYear} / ${goal.yearlyBooksGoal} b",
                    percent = (goal.booksReadThisYear.toFloat() / goal.yearlyBooksGoal).coerceIn(0f, 1f),
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Reading Challenges
        item {
            Text(
                text = "Active Challenges",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(challenges) { ch ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = ch.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        SuggestionChip(
                            onClick = {},
                            label = { Text("+${ch.rewardPoints} pts") }
                        )
                    }
                    Text(
                        text = ch.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (ch.currentDays.toFloat() / ch.targetDays).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ch.currentDays} of ${ch.targetDays} completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Achievements & Badges
        item {
            Text(
                text = "Achievements & Badges",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(badges) { b ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (b.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (b.isUnlocked) Color(0xFFF59E0B) else Color.Gray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (b.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (b.isUnlocked) Color.White else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = b.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (b.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = b.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (b.isUnlocked && b.unlockedDate != null) {
                        Text(
                            text = b.unlockedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Recent Reading Sessions
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(sessions.take(5)) { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = s.bookTitle, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text = "${s.durationMinutes} mins • ${s.pagesRead} pages",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showEditGoalDialog) {
        var minInput by remember { mutableStateOf(goal.dailyMinutesGoal.toString()) }
        var pageInput by remember { mutableStateOf(goal.dailyPagesGoal.toString()) }
        var bookInput by remember { mutableStateOf(goal.yearlyBooksGoal.toString()) }

        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            title = { Text("Update Reading Goals") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = minInput,
                        onValueChange = { minInput = it },
                        label = { Text("Daily Minutes Target") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pageInput,
                        onValueChange = { pageInput = it },
                        label = { Text("Daily Pages Target") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bookInput,
                        onValueChange = { bookInput = it },
                        label = { Text("Yearly Books Target") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = minInput.toIntOrNull() ?: 30
                        val p = pageInput.toIntOrNull() ?: 20
                        val b = bookInput.toIntOrNull() ?: 24
                        viewModel.updateReadingGoals(m, p, b)
                        showEditGoalDialog = false
                    }
                ) {
                    Text("Save Goals")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun GoalMetricCard(
    title: String,
    value: String,
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percent },
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}
