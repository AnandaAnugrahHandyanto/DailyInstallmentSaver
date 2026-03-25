package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: InstallmentViewModel) {
    val logsByDate by viewModel.logsByDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title), fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                AccountMockSection()
            }
            
            item {
                Text(
                    text = stringResource(R.string.monthly_calendar),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                CalendarView(logsByDate)
            }

            item {
                Text(
                    text = stringResource(R.string.savings_trend),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                SimpleLineChart(logsByDate)
            }
        }
    }
}

@Composable
fun AccountMockSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.account), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.sync_status), style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { /* Mock Login */ },
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(stringResource(R.string.login_mock), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CalendarView(logsByDate: Map<String, Any>) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    
    val monthName = remember(calendar) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }
    
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val isCurrentMonth = Calendar.getInstance().get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                         Calendar.getInstance().get(Calendar.YEAR) == calendar.get(Calendar.YEAR)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.prev_month))
                }
                Text(monthName, fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month))
                }
            }
            
            Spacer(Modifier.height(8.dp))

            val firstDayOfWeek = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK)
            val offset = firstDayOfWeek - 1
            
            for (w in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (d in 0 until 7) {
                        val dayNum = w * 7 + d - offset + 1
                        if (dayNum in 1..daysInMonth) {
                            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-$dayNum"
                            val isSaved = logsByDate.containsKey(dateKey)
                            val isToday = isCurrentMonth && dayNum == currentDay
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                                        else Color.Transparent, 
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSaved) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                    } else if (isCurrentMonth && dayNum < currentDay) {
                                        Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        } else {
                            Spacer(Modifier.size(40.dp))
                        }
                    }
                }
                if (w * 7 - offset + 1 > daysInMonth) break
            }
        }
    }
}

@Composable
fun SimpleLineChart(logsByDate: Map<String, Any>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val width = size.width
        val height = size.height
        val points = listOf(0.2f, 0.5f, 0.4f, 0.8f, 0.6f, 0.9f, 0.7f) // Mock data points
        
        val stepX = width / (points.size - 1)
        
        drawLine(Color.Gray, Offset(0f, height), Offset(width, height), strokeWidth = 2f)
        
        for (i in 0 until points.size - 1) {
            val startX = i * stepX
            val startY = height - (points[i] * height)
            val endX = (i + 1) * stepX
            val endY = height - (points[i+1] * height)
            
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
    }
}
