package com.example.learningenglishvocab.ui.screen.library

import android.annotation.SuppressLint
import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.Term
import com.example.learningenglishvocab.data.model.TermStatus
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("NewApi")
@Composable
fun VocabFlashCardScreen(
    modifier: Modifier = Modifier,
    vocabSetViewModel: VocabSetViewModel,
    navController: NavController,
    vocabSetId: String? = null
) {
    var currentIndex by remember { mutableStateOf(0) }
    var swipedLeftCount by remember { mutableStateOf(0) }
    var swipedRightCount by remember { mutableStateOf(0) }
    val swipeHistory = remember { mutableListOf<String>() }

    val totalTerms = vocabSetViewModel.terms.size
    val isCompleted = currentIndex >= totalTerms

    LaunchedEffect(vocabSetId) {
        if (!vocabSetId.isNullOrEmpty()) {
            vocabSetViewModel.loadVocabSetById(vocabSetId)
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
            //        Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(120.dp))

                Text(
                    text = "$currentIndex / $totalTerms",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = if (totalTerms > 0) currentIndex.toFloat() / totalTerms else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .height(6.dp),
                color = Color(0xFF4CAF50), // màu xanh lá
                trackColor = Color.LightGray,
            )

            var swipedLeftCount by remember { mutableStateOf(0) }
            var swipedRightCount by remember { mutableStateOf(0) }

            if (!isCompleted) {
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(80.dp)
                            .requiredHeight(40.dp)
                            .offset(x = (-17).dp, y = 30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xfff7f7f7))
                            .border(2.dp, Color(0xfffdb837), RoundedCornerShape(15.dp))
                    ) {
                        Text(
                            "$swipedLeftCount",
                            color = Color(0xfffdb837),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(x = 40.dp, y = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .requiredWidth(80.dp)
                            .requiredHeight(40.dp)
                            .offset(x = 350.dp, y = 30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xfff7f7f7))
                            .border(2.dp, Color(0xff1b7a4b), RoundedCornerShape(15.dp))
                    ) {
                        Text(
                            "$swipedRightCount",
                            color = Color(0xff1b7a4b),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(x = 30.dp, y = 8.dp)
                        )
                    }
                }

                FlashcardViewer(
                    terms = vocabSetViewModel.terms,
                    currentIndex = currentIndex,
                    swipeHistory = swipeHistory,
                    onSwipedLeft = {
                        swipedLeftCount++
                        swipeHistory.add("left")
                        currentIndex++
                    },
                    onSwipedRight = {
                        swipedRightCount++
                        swipeHistory.add("right")
                        currentIndex++
                    },
                    onSwipedDown = {
                        if (currentIndex > 0 && swipeHistory.isNotEmpty()) {
                            currentIndex--
                            val lastSwipe = swipeHistory.removeLast()
                            if (lastSwipe == "left") {
                                swipedLeftCount--
                            } else if (lastSwipe == "right") {
                                swipedRightCount--
                            }
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = 80.dp)
                ) {
                    FlashcardInstructionText()
                }
            } else {
                val total = swipedLeftCount + swipedRightCount
                val rightPercent = if (total > 0) swipedRightCount * 100f / total else 0f
                val leftPercent = 100f - rightPercent

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cup_icon),
                        contentDescription = "cup_icon",
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 250.dp, y = (-40).dp)
                            .requiredSize(size = 150.dp)
                    )

                    Text(
                        text = "Thật tuyệt! Hãy luyện tập thêm để nắm rõ hơn nhé!",
                        color = Color.Black,
                        lineHeight = 1.76.em,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 22.dp, y = 10.dp)
                            .requiredWidth(227.dp)
                            .requiredHeight(79.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    )

                    Text(
                        text = "Tiến độ của bạn",
                        color = Color.Black,
                        lineHeight = 1.76.em,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 25.dp, y = 180.dp)
                    )

                    Canvas(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = (-80).dp, y = (-30).dp)
                    ) {
                        val sweepRight = 360 * (rightPercent / 100)
                        val sweepLeft = 360 - sweepRight

                        // Phần đã biết (xanh lá)
                        drawArc(
                            color = Color(0xFF1B7A4B),
                            startAngle = -90f,
                            sweepAngle = sweepRight,
                            useCenter = true
                        )

                        // Phần đang học (cam)
                        drawArc(
                            color = Color(0xFFFFA726),
                            startAngle = -90f + sweepRight,
                            sweepAngle = sweepLeft,
                            useCenter = true
                        )
                    }

                    // Đã biết
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 270.dp, y = 250.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1B7A4B).copy(alpha = 0.8f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .width(80.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Đã biết",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "$swipedRightCount",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Đang học
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 270.dp, y = 290.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFA726).copy(alpha = 0.8f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .width(80.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Đang học",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "$swipedLeftCount",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Nút tiếp tục
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 55.dp, y = 530.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = Color(0xffffb74d))
                            .clickable {
                                vocabSetId?.let {
                                    navController.navigate("vocabLearn/$it")
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 20.dp)
                    ) {
                        Text(
                            text = "Tiếp tục với chế độ luyện tập",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 165.dp, y = 580.dp)
                            .clickable {
                                vocabSetId?.let {
                                    navController.navigate("vocabFlashCard/$it")
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Học lại",
                            color = Color.Black,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardViewer(
    terms: List<Term>,
    currentIndex: Int,
    swipeHistory: MutableList<String>,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
    onSwipedDown: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }
    val currentTerm = terms.getOrNull(currentIndex) ?: return

    val rotation = remember { Animatable(0f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val mediaPlayer = remember { MediaPlayer() }

    val borderColor = when (currentTerm.status) {
        TermStatus.LEARNING -> Color(0xfffdb837)
        TermStatus.KNOWN -> Color(0xff1b7a4b)
        else -> Color.Transparent
    }

    suspend fun flipCard() {
        if (!isFlipped) rotation.animateTo(180f)
        else rotation.animateTo(0f)
        isFlipped = !isFlipped
    }

    fun playPronunciation(term: String) {
        coroutineScope.launch {
            try {
                // API Google Text-to-Speech
                val url = "https://translate.google.com/translate_tts?ie=UTF-8&q=$term&tl=en&client=tw-ob"
                mediaPlayer.reset()
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepare()
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 70.dp)
            .height(500.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .graphicsLayer {
                    rotationY =
                        if (rotation.value < 90f) rotation.value else rotation.value - 180f
                    rotationZ = offsetX.value / 40f
                    cameraDistance = 8 * density
                }
                .pointerInput(currentIndex) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value > 200f) {
                                    swipeHistory.add("right")
                                    onSwipedRight()
                                } else if (offsetX.value < -200f) {
                                    swipeHistory.add("left")
                                    onSwipedLeft()
                                }
                                isFlipped = false
                                offsetX.animateTo(0f)
                                rotation.snapTo(0f)
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    )
                }
                .pointerInput(currentIndex) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetY.value > 200f && currentIndex > 0) {
                                    onSwipedDown()
                                }
                                offsetY.animateTo(0f)
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                offsetY.snapTo(offsetY.value + dragAmount)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        coroutineScope.launch { flipCard() }
                    }
                }
                .size(width = 313.dp, height = 487.dp)
                .clip(RoundedCornerShape(15.dp))
                .border(3.dp, borderColor, RoundedCornerShape(15.dp))
                .background(Color(0xfff7f7f7)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (rotation.value < 90f) currentTerm.term else currentTerm.definition,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )

            if (rotation.value < 90f) {
                IconButton(
                    onClick = { playPronunciation(currentTerm.term) },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .offset(x = (-120).dp, y = (-220).dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.speaker),
                        contentDescription = "Play pronunciation",
                        tint = Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardInstructionText() {
    val messages = listOf(
        listOf("Nhấp vào ", "thẻ", " để xem ", "định nghĩa"),
        listOf("Lướt sang ", "trái", " để đánh dấu ", "Đang học"),
        listOf("Lướt sang ", "phải", " để đánh dấu ", "Đã biết"),
        listOf("Lướt ", "xuống dưới", " để quay về ", "thẻ trước")
    )

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % messages.size
        }
    }

    val currentMessage = messages[currentIndex]

    Text(
        text = buildAnnotatedString {
            for ((i, part) in currentMessage.withIndex()) {
                withStyle(
                    style = SpanStyle(
                        fontWeight = if (i % 2 == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                ) {
                    append(part)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun StatBox(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}