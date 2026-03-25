package com.savares.dailyinstallmentsaver.ui

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.model.SavingLogEntity
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: InstallmentViewModel) {
    val uiState by viewModel.statsUiState.collectAsState()

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
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 120.dp)
        ) {
            item(key = "calendar_section") {
                Text(
                    text = stringResource(R.string.monthly_calendar),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                CalendarPagerView(uiState.logsByDate, viewModel)
            }

            item(key = "trend_section") {
                Text(
                    text = stringResource(R.string.savings_trend),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                SimpleLineChart(uiState.trendPoints)
            }
        }
    }
}

@Composable
fun CalendarPagerView(logsByDate: Map<String, List<SavingLogEntity>>, viewModel: InstallmentViewModel) {
    val pagerState = rememberPagerState(initialPage = 500, pageCount = { 1000 })
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(15.dp) else Modifier)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val currentMonth = remember(pagerState.currentPage) {
                    Calendar.getInstance().apply { add(Calendar.MONTH, pagerState.currentPage - 500) }
                }
                val monthName = remember(currentMonth) {
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    
                    Text(monthName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    beyondViewportPageCount = 1
                ) { page ->
                    val monthCal = remember(page) {
                        Calendar.getInstance().apply { add(Calendar.MONTH, page - 500) }
                    }
                    MonthGrid(monthCal, logsByDate, viewModel)
                }
            }
        }
    }
}

@Composable
fun MonthGrid(calendar: Calendar, logsByDate: Map<String, List<SavingLogEntity>>, viewModel: InstallmentViewModel) {
    val daysInMonth = remember(calendar) { calendar.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val firstDayOfWeek = remember(calendar) {
        (calendar.clone() as Calendar).apply { 
            set(Calendar.DAY_OF_MONTH, 1) 
        }.get(Calendar.DAY_OF_WEEK)
    }
    val offset = firstDayOfWeek - 1
    
    val today = remember { Calendar.getInstance() }
    val isCurrentMonth = remember(calendar, today) {
        calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    }
    val currentDayNum = today.get(Calendar.DAY_OF_MONTH)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (w in 0 until 6) {
            val startDay = w * 7 - offset + 1
            if (startDay > daysInMonth) break
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (d in 0 until 7) {
                    val dayNum = w * 7 + d - offset + 1
                    if (dayNum in 1..daysInMonth) {
                        val isToday = isCurrentMonth && dayNum == currentDayNum
                        val dayCal = remember(calendar, dayNum) {
                            (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) }
                        }
                        val dateKey = remember(dayCal) { viewModel.getDateKey(dayCal) }
                        val isSaved = logsByDate.containsKey(dateKey)
                        val isPast = (calendar.get(Calendar.YEAR) < today.get(Calendar.YEAR)) ||
                                     (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) < today.get(Calendar.MONTH)) ||
                                     (isCurrentMonth && dayNum < currentDayNum)
                        
                        DayItem(dayNum, isToday, isSaved, isPast)
                    } else {
                        Spacer(Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DayItem(dayNum: Int, isToday: Boolean, isSaved: Boolean, isPast: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNum.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSaved) {
                    Icon(
                        Icons.Default.Check, 
                        null, 
                        tint = if (isToday) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.8f), 
                        modifier = Modifier.size(12.dp)
                    )
                } else if (isPast) {
                    Icon(
                        Icons.Default.Close, 
                        null, 
                        tint = Color.Red.copy(alpha = 0.6f), 
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(points: List<Float>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        if (points.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1).coerceAtLeast(1)
        
        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(0f, height), Offset(width, height), strokeWidth = 1f)
        
        for (i in 0 until points.size - 1) {
            val startX = i * stepX
            val startY = height - (points[i] * height * 0.7f) - 20f
            val endX = (i + 1) * stepX
            val endY = height - (points[i+1] * height * 0.7f) - 20f
            
            drawLine(
                color = primaryColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
    }
}
