// File: app/src/main/java/com/yourname/fitnesstracker/ui/AnalyticsDashboardScreen.kt
package com.yourname.fitnesstracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourname.fitnesstracker.data.Achievement
import com.yourname.fitnesstracker.data.ChartDataPoint
import com.yourname.fitnesstracker.data.WorkoutAnalytics
import com.yourname.fitnesstracker.viewmodel.AnalyticsViewModel
import com.yourname.fitnesstracker.viewmodel.ChartMetric
import com.yourname.fitnesstracker.viewmodel.ChartPeriod
import kotlin.math.max

@Composable
fun AnalyticsDashboardScreen(
    navController: NavController,
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by analyticsViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        analyticsViewModel.refreshData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analytics Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { analyticsViewModel.refreshData() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overview Stats Card
                item {
                    OverviewStatsCard(analytics = uiState.analytics)
                }

                // Achievement Progress Card
                item {
                    AchievementProgressCard(
                        totalPoints = uiState.totalPoints,
                        unlockedCount = uiState.unlockedCount,
                        totalCount = uiState.totalAchievementCount,
                        progress = analyticsViewModel.getAchievementProgress()
                    )
                }

                // Chart Card
                item {
                    ChartCard(
                        chartData = when (uiState.selectedMetric) {
                            ChartMetric.STEPS -> uiState.stepsChartData
                            ChartMetric.DISTANCE -> uiState.distanceChartData
                            else -> uiState.stepsChartData
                        },
                        selectedMetric = uiState.selectedMetric,
                        selectedPeriod = uiState.selectedChartPeriod,
                        onMetricChange = { analyticsViewModel.changeChartMetric(it) },
                        onPeriodChange = { analyticsViewModel.changeChartPeriod(it) }
                    )
                }

                // Recent Achievements
                item {
                    RecentAchievementsCard(
                        achievements = analyticsViewModel.getRecentAchievements(),
                        onViewAll = { navController.navigate("achievements") }
                    )
                }

                // Personal Records Card
                item {
                    PersonalRecordsCard(analytics = uiState.analytics)
                }
            }
        }

        // Error handling
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { analyticsViewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewStatsCard(analytics: WorkoutAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Fitness Journey",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Main stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.FitnessCenter,
                    value = "${analytics.totalWorkouts}",
                    label = "Workouts",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Default.DirectionsWalk,
                    value = "${analytics.totalSteps}",
                    label = "Total Steps",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    icon = Icons.Default.Route,
                    value = String.format("%.1f km", analytics.totalDistance / 1000),
                    label = "Distance",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${analytics.totalCalories.toInt()}",
                    label = "Calories",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Averages
            Text(
                text = "Average per Workout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AverageStatItem(
                    label = "Steps",
                    value = "${analytics.averageSteps.toInt()}"
                )
                AverageStatItem(
                    label = "Distance",
                    value = "${String.format("%.1f", analytics.averageDistance)} km"
                )
                AverageStatItem(
                    label = "Duration",
                    value = "${String.format("%.0f", analytics.averageDuration)} min"
                )
                AverageStatItem(
                    label = "Calories",
                    value = "${analytics.averageCalories.toInt()}"
                )
            }
        }
    }
}

@Composable
fun AchievementProgressCard(
    totalPoints: Int,
    unlockedCount: Int,
    totalCount: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievement Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$totalPoints pts",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Column {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$unlockedCount / $totalCount Unlocked",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ChartCard(
    chartData: List<ChartDataPoint>,
    selectedMetric: ChartMetric,
    selectedPeriod: ChartPeriod,
    onMetricChange: (ChartMetric) -> Unit,
    onPeriodChange: (ChartPeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Progress Chart",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Metric selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(ChartMetric.values()) { metric ->
                    FilterChip(
                        onClick = { onMetricChange(metric) },
                        label = { Text(metric.label) },
                        selected = metric == selectedMetric
                    )
                }
            }

            // Period selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(ChartPeriod.values()) { period ->
                    FilterChip(
                        onClick = { onPeriodChange(period) },
                        label = { Text(period.label) },
                        selected = period == selectedPeriod
                    )
                }
            }

            // Chart
            if (chartData.isNotEmpty()) {
                SimpleLineChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val range = maxValue - minValue

        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw axes
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )

        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )

        // Draw data points and lines
        val path = Path()
        val gradientPath = Path()

        data.forEachIndexed { index, point ->
            val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * chartWidth
            val y = height - padding - ((point.value - minValue) / range.coerceAtLeast(0.1f)) * chartHeight

            if (index == 0) {
                path.moveTo(x, y)
                gradientPath.moveTo(x, height - padding)
                gradientPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                gradientPath.lineTo(x, y)
            }

            // Draw point
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Close gradient path
        gradientPath.lineTo(width - padding, height - padding)
        gradientPath.close()

        // Draw gradient fill
        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun RecentAchievementsCard(
    achievements: List<Achievement>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }

            if (achievements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No achievements yet. Keep working out!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                achievements.take(3).forEach { achievement ->
                    AchievementListItem(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun PersonalRecordsCard(analytics: WorkoutAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Personal Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PersonalRecordItem(
                    icon = Icons.Default.Route,
                    label = "Longest Distance",
                    value = "${String.format("%.1f", analytics.longestDistance)} km",
                    color = MaterialTheme.colorScheme.tertiary
                )

                PersonalRecordItem(
                    icon = Icons.Default.Whatshot,
                    label = "Current Streak",
                    value = "${analytics.currentStreak} days",
                    color = MaterialTheme.colorScheme.error
                )

                PersonalRecordItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Best Streak",
                    value = "${analytics.bestStreak} days",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AverageStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementListItem(achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Achievement Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getAchievementIcon(achievement.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Achievement Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Points
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Text(
                text = "+${achievement.points}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun PersonalRecordItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

fun getAchievementIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "steps" -> Icons.Default.DirectionsWalk
        "distance" -> Icons.Default.Route
        "duration" -> Icons.Default.Timer
        "workouts" -> Icons.Default.FitnessCenter
        "streak" -> Icons.Default.Whatshot
        "calories" -> Icons.Default.LocalFireDepartment
        "special" -> Icons.Default.Star
        else -> Icons.Default.EmojiEvents
    }
}