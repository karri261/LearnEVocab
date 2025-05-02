package com.example.learningenglishvocab.ui.screen.library

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.VocabSet
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.ui.screen.auth.AppTypes
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.example.learningenglishvocab.viewmodel.LibraryViewModel
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryMainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel,
    vocabSetViewModel: VocabSetViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        libraryViewModel.loadAllVocabSets()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xfff2f1eb))
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .offset(x = 0.dp, y = 23.dp)
                .requiredWidth(width = 375.dp)
                .requiredHeight(height = 40.dp)
        ) {
            Button(
                onClick = {
                    vocabSetViewModel.clear()
                    navController.navigate("createVocabSet/null")
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 300.dp, y = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.plusmath),
                    contentDescription = "Plus Math",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.requiredSize(23.dp)
                )
            }
            Text(
                text = "Thư viện",
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 1.18.em,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 130.dp, y = 10.dp)
            )
        }

        val userRepository = UserRepository()
        val studyLogRepository = StudyLogRepository()

        var expanded by remember { mutableStateOf(false) }
        val options = listOf("Tất cả", "Đã tạo", "Đã tải về")
        var selectedOptionText by remember { mutableStateOf(options[0]) }

        var searchText by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()

        val vocabSetWithLogs = libraryViewModel.vocabSetWithLogs
        val currentUserId = authViewModel.getCurrentUserId()
        var currentUsername by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            val userId = authViewModel.getCurrentUserId() ?: return@LaunchedEffect
            val user = userRepository.getUser(userId)
            currentUsername = user?.username
        }

        val filteredVocabSetWithLogs = remember(vocabSetWithLogs, selectedOptionText, searchText) {
            val baseList = when (selectedOptionText) {
                "Tất cả" -> vocabSetWithLogs
                "Đã tạo" -> vocabSetWithLogs.filter { it.first.created_by == currentUserId }
                "Đã tải về" -> vocabSetWithLogs.filter { it.first.created_by != currentUserId }
                else -> vocabSetWithLogs
            }
            if (searchText.isBlank()) {
                baseList
            } else {
                baseList.filter {
                    it.first.vocabSetName.contains(searchText, ignoreCase = true)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 26.dp, y = 80.dp)
                .requiredWidth(90.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                BasicTextField(
                    value = selectedOptionText,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp),
                    modifier = Modifier
                        .menuAnchor()
                        .requiredHeight(36.dp)
                        .border(1.dp, Color(0xffaaaeb0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(Modifier.weight(1f)) {
                            innerTextField()
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .requiredWidth(150.dp)
                ) {
                    options.forEachIndexed { index, selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                selectedOptionText = selectionOption
                                expanded = false
                            }
                        )
                        if (index != options.lastIndex) {
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        BasicTextField(
            value = searchText,
            onValueChange = { searchText = it },
            singleLine = true,
            textStyle = AppTypes.type_Body_14_Regular.copy(
                color = Color.Black,
                lineHeight = 1.43.em
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 24.dp, y = 127.dp)
                .requiredWidth(360.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xfff6f6f6))
                .border(1.dp, Color(0xffbdbdc4), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "search",
                        modifier = Modifier.requiredSize(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Lọc học phần",
                                color = Color(0xffb9bbc0),
                                style = AppTypes.type_Body_14_Regular,
                                lineHeight = 1.43.em
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        val groupedSets = remember(filteredVocabSetWithLogs) {
            libraryViewModel.groupVocabSetsByDate(filteredVocabSetWithLogs)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 170.dp)
                .imePadding()
        ) {
            Log.d("DEBUG", "CurrentUserId: $currentUserId")
            groupedSets.forEach { (dateTitle, sets) ->
                item {
                    Text(
                        text = dateTitle,
                        color = Color(0xff4c4c4c),
                        lineHeight = 1.43.em,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 27.dp, top = 16.dp, bottom = 0.dp)
                    )
                }

                items(sets) { vocabSet ->
                    VocabSetItem(vocabSet = vocabSet) {
                        // Cập nhật lịch sử học
                        coroutineScope.launch {
                            val logs = studyLogRepository.getStudyLogs(currentUserId ?: "")
                            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val hasLogForToday = logs.any { it.date == today && it.vocabSetId == vocabSet.vocabSetId }

                            if (!hasLogForToday) {
                                studyLogRepository.logStudySession(currentUserId ?: "", vocabSet.vocabSetId)
                            }

                            navController.navigate("vocabSetDetail/${vocabSet.vocabSetId}")
                            libraryViewModel.loadAllVocabSets() // Tải lại danh sách
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun VocabSetItem(vocabSet: VocabSet, onClick: () -> Unit) {
    val authViewModel = AuthViewModel()
    val studyLogRepository = StudyLogRepository()
    val userRepository = UserRepository()
    val coroutineScope = rememberCoroutineScope()
    val userId = authViewModel.getCurrentUserId() ?: return
    var creatorUsername by remember { mutableStateOf("") }
    var avatarBase64 by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vocabSet.created_by) {
        val user = userRepository.getUser(vocabSet.created_by)
        creatorUsername = user?.username ?: "Unknown"
        avatarBase64 = user?.avatar
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .requiredWidth(360.dp)
            .requiredHeight(100.dp)
            .clickable {
                coroutineScope.launch {
                    // Lấy danh sách log của người dùng
                    val logs = studyLogRepository.getStudyLogs(userId)
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                    // Kiểm tra xem đã có log cho ngày hôm nay chưa
                    val hasLogForToday = logs.any { it.date == today && it.vocabSetId == vocabSet.vocabSetId }

                    if (!hasLogForToday) {
                        val currentStreak = studyLogRepository.calculateStreak(userId)
                        userRepository.updateUserStreak(userId, currentStreak)
                        studyLogRepository.logStudySession(userId, vocabSet.vocabSetId)
                        delay(100)
                    }

                    onClick()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xfff6f6f6))
                .border(1.dp, Color(0xffcac8c8), RoundedCornerShape(15.dp))
                .fillMaxSize()
        )

        Text(
            text = vocabSet.vocabSetName,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 1.43.em,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 18.dp, y = 8.dp)
        )

        // Số từ vựng
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 15.dp, y = 37.dp)
                .requiredWidth(80.dp)
                .requiredHeight(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xffe2e2e2))
            )
            Text(
                text = "${vocabSet.terms.size} từ vựng",
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 1.5.em,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        // Avatar + người tạo
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 18.dp, y = 70.dp)
                .requiredHeight(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = avatarBase64?.let {
                    try {
                        val bytes = Base64.decode(it, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        bitmap?.let { BitmapPainter(it.asImageBitmap()) }
                    } catch (e: Exception) {
                        null
                    }
                } ?: painterResource(id = R.drawable.eapplogo),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(20.dp) //
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = creatorUsername,
                color = Color(0xff343333),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(bottom = 1.dp)
            )
        }
    }
}