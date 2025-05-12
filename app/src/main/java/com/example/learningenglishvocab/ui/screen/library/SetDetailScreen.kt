package com.example.learningenglishvocab.ui.screen.library

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.Term
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel
import kotlinx.coroutines.delay
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.io.encoding.ExperimentalEncodingApi

@androidx.annotation.OptIn(UnstableApi::class)
@SuppressLint("RememberReturnType")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetDetailScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    vocabSetViewModel: VocabSetViewModel,
    navController: NavController,
    vocabSetId: String? = null
) {
    val userRepository = UserRepository()

    var showSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showReadyOverlay by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) }
    var startCountdown by remember { mutableStateOf(false) }

    var currentUsername by remember { mutableStateOf<String?>(null) }
    var action by remember { mutableStateOf<String?>(null) }

    val studyLogRepository = StudyLogRepository()
    val userId = authViewModel.getCurrentUserId() ?: return
    var creatorUsername by remember { mutableStateOf("") }
    var avatarBase64 by remember { mutableStateOf<String?>(null) }
    var showStreakOverlay by remember { mutableStateOf(false) }
    var previousStreak by remember { mutableStateOf(0) }
    var currentStreak by remember { mutableStateOf(0) }
    var displayStreak by remember { mutableStateOf(0) }

    // Tính streak trước và sau khi vào màn hình
    LaunchedEffect(Unit) {
        previousStreak = userRepository.getUserStreak(userId)
        delay(500L)
        currentStreak = studyLogRepository.calculateStreak(userId)

        if (currentStreak != previousStreak) {
            showStreakOverlay = true
            displayStreak = if (currentStreak < previousStreak) 0 else previousStreak
            delay(1000L)
            displayStreak = currentStreak
            delay(2000L)
            showStreakOverlay = false
            userRepository.updateUserStreak(userId, currentStreak)
        }
    }

    LaunchedEffect(Unit) {
        val user = userRepository.getUser(userId)
        currentUsername = user?.username
    }

    LaunchedEffect(vocabSetViewModel.vocabSetId) {
        if (!vocabSetId.isNullOrEmpty()) {
            vocabSetViewModel.loadVocabSetById(vocabSetId)
            val vocabSet = vocabSetViewModel
            val creator = userRepository.getUser(vocabSet.created_by)
            creatorUsername = creator?.username ?: "Unknown"
            avatarBase64 = creator?.avatar
        }
    }


    // LaunchedEffect để chạy đếm ngược
    LaunchedEffect(showReadyOverlay, startCountdown, countdown) {
        if (showReadyOverlay && startCountdown && countdown > 0) {
            delay(1000L)
            countdown -= 1
        } else if (showReadyOverlay && startCountdown && countdown == 0) {
            delay(1500L)
            showReadyOverlay = false
            vocabSetId?.let {
                navController.navigate("matchCard/$it")
            }
        }
    }

    // LaunchedEffect để xử lý hành động edit/delete
    LaunchedEffect(action) {
        if (action != null) {
            try {
                if (currentUsername != null && vocabSetViewModel.created_by == userId) {
                    when (action) {
                        "edit" -> {
                            showSheet = false
                            vocabSetId?.let {
                                navController.navigate("createVocabSet/$it")
                            }
                        }

                        "delete" -> {
                            vocabSetViewModel.deleteVocabSet(
                                onSuccess = {
                                    navController.popBackStack()
                                },
                                onFailure = { e ->
                                    Log.e("DeleteVocabSet", "Lỗi khi xóa: ${e.message}")
                                }
                            )
                        }
                    }
                } else {
                    showSheet = false
                    Toast.makeText(
                        context,
                        "Bạn không có quyền ${if (action == "edit") "chỉnh sửa" else "xóa"} bộ từ này",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                showSheet = false
                Toast.makeText(
                    context,
                    "Lỗi khi kiểm tra quyền ${if (action == "edit") "chỉnh sửa" else "xóa"}: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Reset action sau khi xử lý
            action = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xfff2f1eb))
                .padding(16.dp),
        ) {
            //        Header
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    navController.navigate("libraryMain") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                IconButton(
                    onClick = { showSheet = true },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more),
                        contentDescription = "More",
                        tint = Color.Black
                    )
                }
                if (showSheet) {
                    VocabSetOptionsSheet(
                        onEdit = {
                            action = "edit"
                        },
                        onDelete = {
                            action = "delete"
                        },
                        onDismiss = {
                            showSheet = false
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp, bottom = 10.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Flashcard Preview
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(
                                x = 15.dp, y = 15.dp
                            ),
                        contentPadding = PaddingValues(end = 50.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vocabSetViewModel.terms) { term ->
                            TermCard(term = term)
                        }
                    }
                }

                // Information
                item {
                    val vocabSetName = vocabSetViewModel.vocabSetName
                    val createdBy = vocabSetViewModel.created_by
                    val terms = vocabSetViewModel.terms

                    Box(
                        modifier = modifier
                            .offset(x = 15.dp, y = 25.dp)
                            .requiredWidth(300.dp)
                            .requiredHeight(53.dp)
                    ) {
                        Text(
                            text = vocabSetName,
                            color = Color.Black,
                            lineHeight = 1.25.em,
                            style = TextStyle(
                                fontSize = 23.sp, fontWeight = FontWeight.Bold
                            )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 0.dp, y = 33.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
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
                                    contentDescription = "avatar",
                                    modifier = Modifier
                                        .requiredSize(26.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = creatorUsername,
                                    color = Color(0xff343333),
                                    style = TextStyle(fontSize = 14.sp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Divider(
                                color = Color(0xffc4c0c0),
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${terms.size} thuật ngữ",
                                color = Color(0xff343333), style = TextStyle(fontSize = (13.5).sp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Navigate
                item {
                    Box(
                        modifier = Modifier
                            .offset(x = 15.dp, y = 30.dp)
                            .width(350.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xfff7f7f7))
                            .clickable {
                                vocabSetId?.let {
                                    navController.navigate("vocabFlashCard/$it")
                                }
                            }
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_1),
                                contentDescription = "Thẻ ghi nhớ",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Thẻ ghi nhớ", color = Color.Black, style = TextStyle(
                                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .offset(x = 15.dp, y = 30.dp)
                            .width(350.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xfff7f7f7))
                            .clickable {
                                vocabSetId?.let {
                                    navController.navigate("vocabLearn/$it")
                                }
                            }
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_2),
                                contentDescription = "Luyện tập",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Luyện tập", color = Color.Black, style = TextStyle(
                                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .offset(x = 15.dp, y = 30.dp)
                            .width(350.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xfff7f7f7))
                            .clickable {
                                showReadyOverlay = true
                                countdown = 3
                                startCountdown = false
                            }
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card_3),
                                contentDescription = "Ghép thẻ",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ghép thẻ", color = Color.Black, style = TextStyle(
                                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = 15.dp, y = 40.dp
                            )
                            .requiredWidth(width = 323.dp)
                            .requiredHeight(height = 24.dp)
                    ) {
                        Text(
                            text = "Thuật ngữ",
                            color = Color.Black,
                            lineHeight = 1.43.em,
                            style = TextStyle(
                                fontSize = 18.sp, fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                        )
                    }
                }

                items(vocabSetViewModel.terms) { term ->
                    Box(
                        modifier = Modifier
                            .offset(x = 15.dp, y = 40.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredWidth(350.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xfff7f7f7))
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    text = term.term,
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 22.sp),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = term.definition,
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 15.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showReadyOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .zIndex(100f) // Đảm bảo overlay ở trên cùng
                    .clickable {
                        showReadyOverlay = false
                        startCountdown = false // Reset trạng thái đếm ngược
                        countdown = 3 // Reset đếm ngược
                    }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent)
                        .requiredSize(width = 300.dp, height = 200.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!startCountdown) {
                        Text(
                            text = "Bạn đã sẵn sàng chưa?",
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
                    }

                    if (startCountdown) {
                        AnimatedContent(
                            targetState = countdown,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()) with
                                        (slideOutVertically { height -> -height } + fadeOut())
                            }
                        ) { count ->
                            Text(
                                text = if (count > 0) count.toString() else "Go!",
                                style = TextStyle(
                                    fontSize = 70.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFA6F3E)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!startCountdown) {
                        Button(
                            onClick = {
                                startCountdown = true
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
                                    start = 10.dp,
                                    end = 10.dp,
                                    top = 0.dp,
                                    bottom = 0.dp
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                text = "Sẵn sàng",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Overlay hiển thị streak
    if (showStreakOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)) // Nền đen mờ
                .zIndex(100f)
                .clickable(
                    onClick = { showStreakOverlay = false },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showStreakOverlay,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(1000))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "\uD83D\uDD25",
                        fontSize = 60.sp
                    )
                    AnimatedContent(
                        targetState = displayStreak,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(500)) with
                                    fadeOut(animationSpec = tween(500)))
                        }
                    ) { streak ->
                        Text(
                            text = streak.toString(),
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8C00)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TermCard(term: Term) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flipAnimation"
    )

    Box(modifier = Modifier
        .requiredWidth(314.dp)
        .requiredHeight(166.dp)
        .graphicsLayer {
            rotationX = rotation
            cameraDistance = 8 * density
        }
        .clip(RoundedCornerShape(17.dp))
        .background(Color(0xfff6f6f6))
        .drawBehind {
            drawRoundRect(
                color = Color(0xffcac8c8),
                size = size,
                cornerRadius = CornerRadius(17.dp.toPx(), 17.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        .clickable { flipped = !flipped }) {
        Box(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (rotation > 90f) rotationX = 180f
            }
            .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (rotation <= 90f) term.term else term.definition, style = TextStyle(
                    fontSize = 20.sp, fontWeight = FontWeight.Medium
                ), textAlign = TextAlign.Center, color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabSetOptionsSheet(
    onEdit: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ListItem(headlineContent = {
                Text(
                    text = "Sửa bộ từ vựng", color = Color.Black
                )
            }, leadingContent = {
                Icon(Icons.Default.Edit, contentDescription = null)
            }, modifier = Modifier.clickable { onEdit() })
            ListItem(headlineContent = {
                Text(
                    text = "Xoá bộ từ vựng", color = Color.Black
                )
            }, leadingContent = {
                Icon(Icons.Default.Delete, contentDescription = null)
            }, modifier = Modifier.clickable { onDelete() })
        }
    }
}