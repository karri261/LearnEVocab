package com.example.learningenglishvocab.ui.screen.library

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MatchCardScreen(
    modifier: Modifier = Modifier,
    vocabSetViewModel: VocabSetViewModel,
    navController: NavController,
    vocabSetId: String? = null
) {
    val selectedTerms = remember(vocabSetViewModel.terms) {
        vocabSetViewModel.terms.shuffled(Random).take(5).toMutableList()
    }

    val remainingTerms = remember(vocabSetViewModel.terms) {
        vocabSetViewModel.terms.filterNot { selectedTerms.contains(it) }.toMutableList()
    }

    val cardContents = remember {
        mutableStateOf(
            selectedTerms.flatMap { listOf(Pair(it.term, true), Pair(it.definition, false)) }
                .shuffled(Random)
        )
    }

    var elapsedTime by remember { mutableStateOf(0L) }
    var isGameFinished by remember { mutableStateOf(false) }
    var showExitOverlay by remember { mutableStateOf(false) }
    var isTimerPaused by remember { mutableStateOf(false) }
    val timeText = remember(elapsedTime) {
        val minutes = (elapsedTime / 1000) / 60
        val seconds = (elapsedTime / 1000) % 60
        val milliseconds = (elapsedTime % 1000) / 10
        String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
    }

    val selectedCards = remember { mutableStateListOf<Int>() }
    val cardColors =
        remember { mutableStateListOf<Color>().apply { repeat(10) { add(Color(0xFFF7F7F7)) } } } // Màu nền mỗi thẻ
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(vocabSetId) {
        if (!vocabSetId.isNullOrEmpty()) {
            vocabSetViewModel.loadVocabSetById(vocabSetId)
        }
    }

    // Cập nhật thời gian
    LaunchedEffect(isGameFinished, isTimerPaused, showExitOverlay) {
        while (!isGameFinished && !isTimerPaused && !showExitOverlay) {
            delay(10) // Cập nhật mỗi 10ms
            elapsedTime += 10
        }
    }

    // Kiểm tra khi hoàn thành
    LaunchedEffect(cardContents.value) {
        if (cardContents.value.isEmpty()) {
            isGameFinished = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xfff2f1eb))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xfff2f1eb))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        //                        navController.navigate("vocabSetDetail/${vocabSetId}")
                        Log.d("TimerDebug", "Back clicked, pausing timer and showing overlay")
                        showExitOverlay = true
                        isTimerPaused = true
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(110.dp))
                Text(
                    text = timeText,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Tính số hàng dựa trên số thẻ
                val rowCount = (cardContents.value.size + 1) / 2 // Làm tròn lên
                for (rowIndex in 0 until rowCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        // Thẻ bên trái (nếu có)
                        if (rowIndex * 2 < cardContents.value.size) {
                            CardItem(
                                content = cardContents.value[rowIndex * 2].first,
                                isTerm = cardContents.value[rowIndex * 2].second,
                                backgroundColor = cardColors[rowIndex * 2],
                                onClick = {
                                    val index = rowIndex * 2
                                    if (index !in selectedCards) {
                                        if (selectedCards.size < 2) {
                                            selectedCards.add(index)
                                            cardColors[index] = Color(0xFFB6B5B2)
                                        }
                                        if (selectedCards.size == 2) {
                                            val firstIndex = selectedCards[0]
                                            val secondIndex = selectedCards[1]
                                            val firstContent = cardContents.value[firstIndex]
                                            val secondContent = cardContents.value[secondIndex]
                                            val firstTerm =
                                                if (firstContent.second) firstContent.first else selectedTerms.find { it.definition == firstContent.first }?.term
                                            val secondTerm =
                                                if (secondContent.second) secondContent.first else selectedTerms.find { it.definition == secondContent.first }?.term

                                            if (firstTerm != null && secondTerm != null && firstTerm == secondTerm && firstContent.second != secondContent.second) {
                                                // Ghép đúng
                                                cardColors[firstIndex] = Color(0xFFD5FDDB)
                                                cardColors[secondIndex] = Color(0xFFD5FDDB)
                                                coroutineScope.launch {
                                                    delay(500)
                                                    selectedCards.clear()
                                                    selectedTerms.removeIf { it.term == firstTerm }
                                                    if (remainingTerms.isNotEmpty() && selectedTerms.size < 5) {
                                                        // Có từ mới, thêm và xáo trộn
                                                        val newTerm = remainingTerms.removeAt(0)
                                                        selectedTerms.add(newTerm)
                                                        cardContents.value = selectedTerms.flatMap {
                                                            listOf(
                                                                Pair(it.term, true),
                                                                Pair(it.definition, false)
                                                            )
                                                        }.shuffled(Random)
                                                        cardColors.clear()
                                                        cardColors.addAll(List(10) {
                                                            Color(
                                                                0xFFF7F7F7
                                                            )
                                                        })
                                                    } else {
                                                        // Hết từ, chỉ xóa 2 thẻ ghép đúng
                                                        val newContents =
                                                            cardContents.value.toMutableList()
                                                                .apply {
                                                                    if (secondIndex < size && firstIndex < size) {
                                                                        removeAt(
                                                                            maxOf(
                                                                                firstIndex,
                                                                                secondIndex
                                                                            )
                                                                        )
                                                                        removeAt(
                                                                            minOf(
                                                                                firstIndex,
                                                                                secondIndex
                                                                            )
                                                                        )
                                                                    }
                                                                }
                                                        cardContents.value = newContents
                                                        val newColors =
                                                            mutableListOf<Color>().apply {
                                                                addAll(cardColors)
                                                                while (size < newContents.size) add(
                                                                    Color(0xFFF7F7F7)
                                                                )
                                                                if (firstIndex < size) set(
                                                                    firstIndex,
                                                                    Color(0xFFF7F7F7)
                                                                )
                                                                if (secondIndex < size) set(
                                                                    secondIndex,
                                                                    Color(0xFFF7F7F7)
                                                                )
                                                            }
                                                        cardColors.clear()
                                                        cardColors.addAll(newColors.take(newContents.size))
                                                    }
                                                }
                                            } else {
                                                // Ghép sai
                                                cardColors[firstIndex] = Color(0xFFFF9E9C)
                                                cardColors[secondIndex] = Color(0xFFFF9E9C)
                                                coroutineScope.launch {
                                                    delay(2000)
                                                    cardColors[firstIndex] = Color(0xFFF7F7F7)
                                                    cardColors[secondIndex] = Color(0xFFF7F7F7)
                                                    selectedCards.clear()
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(140.dp)
                            )
                        } else {
                            // Placeholder cho thẻ trống
                            Box(modifier = Modifier
                                .weight(1f)
                                .height(140.dp))
                        }
                        // Thẻ bên phải (nếu có)
                        if (rowIndex * 2 + 1 < cardContents.value.size) {
                            CardItem(
                                content = cardContents.value[rowIndex * 2 + 1].first,
                                isTerm = cardContents.value[rowIndex * 2 + 1].second,
                                backgroundColor = cardColors[rowIndex * 2 + 1],
                                onClick = {
                                    val index = rowIndex * 2 + 1
                                    if (index !in selectedCards) {
                                        if (selectedCards.size < 2) {
                                            selectedCards.add(index)
                                            cardColors[index] = Color(0xFFB6B5B2)
                                        }
                                        if (selectedCards.size == 2) {
                                            val firstIndex = selectedCards[0]
                                            val secondIndex = selectedCards[1]
                                            val firstContent = cardContents.value[firstIndex]
                                            val secondContent = cardContents.value[secondIndex]
                                            val firstTerm =
                                                if (firstContent.second) firstContent.first else selectedTerms.find { it.definition == firstContent.first }?.term
                                            val secondTerm =
                                                if (secondContent.second) secondContent.first else selectedTerms.find { it.definition == secondContent.first }?.term

                                            if (firstTerm != null && secondTerm != null && firstTerm == secondTerm && firstContent.second != secondContent.second) {
                                                // Ghép đúng
                                                cardColors[firstIndex] = Color(0xFFD5FDDB)
                                                cardColors[secondIndex] = Color(0xFFD5FDDB)
                                                coroutineScope.launch {
                                                    delay(500)
                                                    selectedCards.clear()
                                                    selectedTerms.removeIf { it.term == firstTerm }
                                                    if (remainingTerms.isNotEmpty() && selectedTerms.size < 5) {
                                                        // Có từ mới, thêm và xáo trộn
                                                        val newTerm = remainingTerms.removeAt(0)
                                                        selectedTerms.add(newTerm)
                                                        cardContents.value = selectedTerms.flatMap {
                                                            listOf(
                                                                Pair(it.term, true),
                                                                Pair(it.definition, false)
                                                            )
                                                        }.shuffled(Random)
                                                        cardColors.clear()
                                                        cardColors.addAll(List(10) {
                                                            Color(
                                                                0xFFF7F7F7
                                                            )
                                                        })
                                                    } else {
                                                        // Hết từ, chỉ xóa 2 thẻ ghép đúng
                                                        val newContents =
                                                            cardContents.value.toMutableList()
                                                                .apply {
                                                                    if (secondIndex < size && firstIndex < size) {
                                                                        removeAt(
                                                                            maxOf(
                                                                                firstIndex,
                                                                                secondIndex
                                                                            )
                                                                        )
                                                                        removeAt(
                                                                            minOf(
                                                                                firstIndex,
                                                                                secondIndex
                                                                            )
                                                                        )
                                                                    }
                                                                }
                                                        cardContents.value = newContents
                                                        val newColors =
                                                            mutableListOf<Color>().apply {
                                                                addAll(cardColors)
                                                                while (size < newContents.size) add(
                                                                    Color(0xFFF7F7F7)
                                                                )
                                                                if (firstIndex < size) set(
                                                                    firstIndex,
                                                                    Color(0xFFF7F7F7)
                                                                )
                                                                if (secondIndex < size) set(
                                                                    secondIndex,
                                                                    Color(0xFFF7F7F7)
                                                                )
                                                            }
                                                        cardColors.clear()
                                                        cardColors.addAll(newColors.take(newContents.size))
                                                    }
                                                }
                                            } else {
                                                // Ghép sai
                                                cardColors[firstIndex] = Color(0xFFFF9E9C)
                                                cardColors[secondIndex] = Color(0xFFFF9E9C)
                                                coroutineScope.launch {
                                                    delay(2000)
                                                    cardColors[firstIndex] = Color(0xFFF7F7F7)
                                                    cardColors[secondIndex] = Color(0xFFF7F7F7)
                                                    selectedCards.clear()
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(140.dp)
                            )
                        } else {
                            // Placeholder cho thẻ trống
                            Box(modifier = Modifier
                                .weight(1f)
                                .height(140.dp))
                        }
                    }
                }
            }
        }

        //      Overlay khi bấm thoát
        if (showExitOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(100f)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bạn có chắc chắn muốn thoát?",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Gray,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                navController.navigate("vocabSetDetail/${vocabSetId}")
                            },
                            modifier = Modifier
                                .requiredWidth(width = 150.dp)
                                .clip(shape = RoundedCornerShape(60.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0f to Color(0xFFF74C54),
                                            0.84f to Color(0xFFFA8246),
                                            1f to Color(0xFFFEAC2F)
                                        ),
                                    )
                                )
                                .padding(
                                    start = 7.dp,
                                    end = 7.dp,
                                    top = 0.dp,
                                    bottom = 0.dp
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                text = "Chắc chắn",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Button(
                            onClick = {
                                showExitOverlay = false
                                isTimerPaused = false
                            },
                            modifier = Modifier.requiredWidth(width = 150.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            )
                        ) {
                            Text(
                                text = "Hủy",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Overlay khi hoàn thành
        if (isGameFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) { }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tuyệt vời!!",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Gray,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bạn đã hoàn thành bộ từ này trong $timeText",
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                // Reset game
                                isGameFinished = false
                                elapsedTime = 0L
                                navController.navigate("matchCard/$vocabSetId")
                            },
                            modifier = Modifier
                                .requiredWidth(width = 150.dp)
                                .clip(shape = RoundedCornerShape(60.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0f to Color(0xFFF74C54),
                                            0.84f to Color(0xFFFA8246),
                                            1f to Color(0xFFFEAC2F)
                                        ),
                                    )
                                )
                                .padding(
                                    start = 7.dp,
                                    end = 7.dp,
                                    top = 0.dp,
                                    bottom = 0.dp
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                text = "Chơi lại",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                        Button(
                            onClick = {
                                navController.navigate("vocabSetDetail/${vocabSetId}")
                            },
                            modifier = Modifier.requiredWidth(width = 150.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            )
                        ) {
                            Text(
                                text = "Quay lại",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardItem(
    content: String,
    isTerm: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = content,
            color = Color.Black,
            lineHeight = 1.5.em,
            style = TextStyle(
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}