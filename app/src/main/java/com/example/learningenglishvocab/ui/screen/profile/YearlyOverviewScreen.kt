package com.example.learningenglishvocab.ui.screen.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import java.time.LocalDate
import java.time.YearMonth

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyOverviewScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val studyLogRepository = StudyLogRepository()
    val userId = authViewModel.getCurrentUserId() ?: return

    val currentYear = YearMonth.now().year
    val years = (2024..currentYear).toList().reversed()
    var selectedYear by remember { mutableStateOf(currentYear) }
    var showYearDropdown by remember { mutableStateOf(false) }

    var studiedDaysByMonth by remember { mutableStateOf<Map<Int, List<String>>>(emptyMap()) }

    LaunchedEffect(selectedYear) {
        studiedDaysByMonth = studyLogRepository.getStudiedDaysByYear(userId, selectedYear)
    }

    DisposableEffect(selectedYear) {
        onDispose {
            studiedDaysByMonth = emptyMap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F1EB))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Tổng quan năm",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            // Dropdown chọn năm
            Box {
                Row(
                    modifier = Modifier
                        .clickable { showYearDropdown = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedYear.toString(),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color.Black
                    )
                }
                DropdownMenu(
                    expanded = showYearDropdown,
                    onDismissRequest = { showYearDropdown = false }
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year.toString()) },
                            onClick = {
                                selectedYear = year
                                showYearDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Danh sách 12 lịch (mỗi tháng một lịch)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(12) { monthIndex ->
                val month = monthIndex + 1
                val studiedDays = studiedDaysByMonth[month] ?: emptyList()

                Column {
                    // Tiêu đề tháng
                    Text(
                        text = "Tháng $month/$selectedYear",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                    // Lịch dạng grid
                    MiniCalendarGrid(
                        year = selectedYear,
                        month = month,
                        studiedDays = studiedDays,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun MiniCalendarGrid(
    year: Int,
    month: Int,
    studiedDays: List<String>,
    modifier: Modifier = Modifier
) {
    // Tính các thông tin cần thiết cho lịch
    val yearMonth = YearMonth.of(year, month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    // Danh sách các ngày (1 đến daysInMonth)
    val days = (1..daysInMonth).toList()

    // Tính số hàng cần thiết (bao gồm ô trống ở đầu)
    val totalItems = firstDayOfWeek + daysInMonth
    val rows = (totalItems + 6) / 7

    Column(modifier = modifier) {
        // Tiêu đề các ngày trong tuần
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { dayName ->
                Text(
                    text = dayName,
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Grid hiển thị các ngày
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height((rows * 36.dp) + 7.dp)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Thêm các ô trống trước ngày 1
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(32.dp))
            }

            // Hiển thị các ngày
            items(days) { day ->
                val formattedDay = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                val isStudied = formattedDay in studiedDays
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isStudied) Color(0xFFFF8C00) else Color(0xFFF6F6F6)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontSize = 12.sp,
                        color = if (isStudied) Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}