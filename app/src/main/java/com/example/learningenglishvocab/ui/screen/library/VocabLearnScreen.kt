package com.example.learningenglishvocab.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.AlertDialog
import androidx.compose.material.LocalTextStyle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.PracticeRecord
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VocabLearnScreen(
    modifier: Modifier = Modifier,
    vocabSetViewModel: VocabSetViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    vocabSetId: String? = null,
    isRetryMode: Boolean = false
) {
    val userRepository = UserRepository()
    val userId = authViewModel.getCurrentUserId() ?: return
    var isPremium by remember { mutableStateOf(false) }
    var practiceRecord by remember { mutableStateOf<PracticeRecord?>(null) }
    var showLimitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val originalTerms = if (isRetryMode) vocabSetViewModel.unknownTerms else vocabSetViewModel.terms
    val shuffledTerms = remember(isRetryMode) { originalTerms.shuffled().toMutableStateList() }
    var currentIndex by remember { mutableStateOf(0) }
    val totalTerms by remember(isRetryMode) { mutableStateOf(originalTerms.size) }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var userAnswer by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var showIncorrectFeedback by remember { mutableStateOf(false) }
    var isFirstAttempt by remember(currentIndex) { mutableStateOf(true) }
    var lastWrongAnswer by remember { mutableStateOf("") }

    // Load user và practice record
    LaunchedEffect(userId, vocabSetId) {
        val user = userRepository.getUser(userId)
        isPremium = user?.premium ?: false
        if (vocabSetId != null) {
            practiceRecord = userRepository.getPracticeRecord(userId, vocabSetId)
        }
    }

    // Kiểm tra giới hạn trước khi học
    LaunchedEffect(practiceRecord, isPremium) {
        if (!isPremium && practiceRecord != null && practiceRecord!!.practiceCount >= 1) {
            showLimitDialog = true
            return@LaunchedEffect
        }
        if (!isRetryMode && vocabSetId != null) {
            vocabSetViewModel.loadVocabSetById(vocabSetId)
        }
        if (isRetryMode) {
            vocabSetViewModel.startNewRound()
        }
        shuffledTerms.clear()
        shuffledTerms.addAll(originalTerms.shuffled())
        focusRequester.requestFocus()
    }

//    LaunchedEffect(vocabSetId, isRetryMode) {
//        if (!isRetryMode && vocabSetId != null) {
//            vocabSetViewModel.loadVocabSetById(vocabSetId)
//        }
//        if (isRetryMode) {
//            vocabSetViewModel.startNewRound()
//        }
//        shuffledTerms.clear()
//        shuffledTerms.addAll(originalTerms.shuffled())
//        focusRequester.requestFocus()
//    }

    LaunchedEffect(isAnswered, isCorrect) {
        if (isAnswered && isCorrect) {
            delay(1500)
            if (currentIndex < shuffledTerms.size - 1) {
                currentIndex++
                userAnswer = ""
                isAnswered = false
                isCorrect = false
                isFirstAttempt = true
                focusRequester.requestFocus()
            } else {
                withContext(Dispatchers.Main) {
                    navController.navigate("vocabLearnResult/$vocabSetId")
                }
            }
        }
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFFFFF))
                .padding(bottom = 16.dp),
            title = {
                Text(
                    text = "Nâng cấp tài khoản",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF343333)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Tài khoản của bạn chỉ được sử dụng tính năng này 1 lần. Nâng cấp lên Premium để luyện tập không giới hạn!",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF343333),
                        lineHeight = 1.5.em
                    ),
                    textAlign = TextAlign.Center
                )
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút Nâng cấp
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(64.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colorStops = arrayOf(
                                        0f to Color(0xFFF74C54),
                                        0.75f to Color(0xFFFA8246),
                                        1f to Color(0xFFFEAC2F)
                                    )
                                )
                            )
                            .height(48.dp)
                            .width(120.dp)
                            .clickable {
                                showLimitDialog = false
                                navController.navigate("profile")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nâng cấp",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Nút OK
                    TextButton(
                        onClick = {
                            showLimitDialog = false
                            navController.navigate("vocabSetDetail/$vocabSetId")
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .width(120.dp)
                    ) {
                        Text(
                            text = "OK",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9E9595)
                            )
                        )
                    }
                }
            },
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xfff2f1eb))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
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
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            // Progress bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .requiredWidth(width = 35.dp)
                        .requiredHeight(height = 20.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(color = Color(0xffd2e8d4))
                ) {
                    Text(
                        text = "${currentIndex + 1}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .offset(y = (-2).dp)
                    )
                }

                LinearProgressIndicator(
                    progress = if (totalTerms > 0) (currentIndex + 1).toFloat() / totalTerms else 0f,
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .weight(1f)
                        .height(6.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color.LightGray
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .requiredWidth(width = 35.dp)
                        .requiredHeight(height = 20.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(color = Color(0xffd2d7d2))
                ) {
                    Text(
                        text = "$totalTerms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .offset(y = (-2).dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (shuffledTerms.isNotEmpty()) {
                        Text(
                            text = shuffledTerms[currentIndex].definition,
                            color = Color.Black,
                            lineHeight = 2.em,
                            style = TextStyle(fontSize = 18.sp),
                            modifier = Modifier
                                .padding(top = 40.dp, start = 20.dp, end = 20.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "Bỏ qua",
                        color = Color(0xff9e9595),
                        lineHeight = 2.35.em,
                        style = TextStyle(fontSize = 17.sp),
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .clickable {
                                if (currentIndex < shuffledTerms.size) {
                                    val currentTerm = shuffledTerms[currentIndex]
                                    if (isFirstAttempt) {
                                        vocabSetViewModel.addAnsweredTerm(currentTerm, false)
                                    }
                                    coroutineScope.launch {
                                        delay(1500)
                                        if (currentIndex < shuffledTerms.size - 1) {
                                            currentIndex++
                                            userAnswer = ""
                                            isAnswered = false
                                            isCorrect = false
                                            isFirstAttempt = true
                                            focusRequester.requestFocus()
                                        } else {
                                            navController.navigate("vocabLearnResult/$vocabSetId")
                                        }
                                    }
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    if (showIncorrectFeedback) {
                        AnswerBox(
                            text = lastWrongAnswer,
                            color = Color(0xfff9b3b3),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        AnswerBox(
                            text = shuffledTerms[currentIndex].term,
                            color = Color(0xffd3ffd6),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else if (isAnswered && isCorrect) {
                        AnswerBox(
                            text = shuffledTerms[currentIndex].term,
                            color = Color(0xffd3ffd6),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y=480.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = userAnswer,
                onValueChange = {
                    userAnswer = it
                    if (showIncorrectFeedback) {
                        showIncorrectFeedback = false
                    }
                },
                modifier = Modifier
                    .width(290.dp)
                    .height(47.dp)
                    .background(
                        color = Color(0xFFF7F7F7),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .focusRequester(focusRequester),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (userAnswer.isEmpty()) {
                            Text(
                                text = "Nhập câu trả lời",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    isAnswered = true
                    isCorrect = userAnswer.trim().equals(shuffledTerms[currentIndex].term, ignoreCase = true)
                    val currentTerm = shuffledTerms[currentIndex]

                    if (isFirstAttempt) {
                        vocabSetViewModel.addAnsweredTerm(currentTerm, isCorrect)
                        isFirstAttempt = false
                    } else if (isCorrect) {
                        vocabSetViewModel.addAnsweredTerm(currentTerm, true)
                    }

                    if (!isCorrect) {
                        lastWrongAnswer = userAnswer
                        showIncorrectFeedback = true
                        coroutineScope.launch {
                            delay(2000)
                            showIncorrectFeedback = false
                        }
                    }
                    userAnswer = ""
                },
                modifier = Modifier
                    .height(47.dp)
                    .width(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFA6F3E),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Gửi",
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun AnswerBox(text: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(color)
            .border(1.dp, Color.Black, RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}