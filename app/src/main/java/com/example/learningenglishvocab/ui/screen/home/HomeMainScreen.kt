package com.example.learningenglishvocab.ui.screen.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.material.AlertDialog
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.DictionaryResponse
import com.example.learningenglishvocab.data.model.RecognizedWord
import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.model.VocabSet
import com.example.learningenglishvocab.data.repository.DictionaryRepository
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.data.repository.VocabSetRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

@SuppressLint("NewApi", "UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController,
    onShowImageOverlayChange: (Boolean) -> Unit
) {
    val userRepository = UserRepository()
    val dictionaryRepository = DictionaryRepository()
    val studyLogRepository = StudyLogRepository()
    val vocabSetRepository = VocabSetRepository()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var userState by remember { mutableStateOf<User?>(null) }
    var studiedVocabSets by remember { mutableStateOf<List<VocabSet>>(emptyList()) }
    var suggestedVocabSets by remember { mutableStateOf<List<VocabSet>>(emptyList()) }
    var selectedWord by remember { mutableStateOf<DictionaryResponse?>(null) }
    var showWordBottomSheet by remember { mutableStateOf(false) }

    var uploadedImage by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedWords by remember { mutableStateOf<List<RecognizedWord>>(emptyList()) }
    var showImageOverlay by remember { mutableStateOf(false) }
    var isPremium by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = authViewModel.getCurrentUserId() ?: return@LaunchedEffect
        userState = withContext(Dispatchers.IO) {
            userRepository.getUser(userId)
        }
        isPremium = userState?.premium == true

//      Lấy danh sách vocabset đã học và hiển thị lên
        val logs = studyLogRepository.getStudyLogs(userId)
        val vocabSetIds = logs.map { it.vocabSetId }.distinct()
        Log.d("VocabSetSuggest", "VocabSet IDs from StudyLog: $vocabSetIds")

        val studiedVocabSetsFlow = flow {
            val vocabSets = mutableListOf<VocabSet>()
            vocabSetIds.take(5).forEach { id ->
                suspendCancellableCoroutine { cont ->
                    vocabSetRepository.getVocabSetById(id) { vocabSet ->
                        vocabSets.add(vocabSet)
                        Log.d("VocabSetSuggest", "Fetched VocabSet: $vocabSet")
                        cont.resume(Unit)
                    }
                }
            }
            emit(vocabSets)
        }

//  Lấy vocabset gợi ý
        studiedVocabSetsFlow.collect { vocabSets ->
            studiedVocabSets = vocabSets
            Log.d("VocabSetSuggest", "Studied VocabSets: $studiedVocabSets")

            // Lấy vocabset gợi ý
            val creators = vocabSets.map { it.created_by }.distinct().filter { it != userId }
            Log.d("VocabSetSuggest", "Creators: $creators")

            val suggestedVocabSetsList = mutableListOf<VocabSet>()
            if (creators.isNotEmpty()) {
                if (creators.size == 1) {
                    // Một người tạo: Lấy tối đa 5 VocabSet công khai, không premium
                    val creatorVocabSets =
                        vocabSetRepository.getPublicVocabSetsByCreator(creators.first(), userId, 5)
                    suggestedVocabSetsList.addAll(creatorVocabSets.filter { creatorSet ->
                        vocabSets.none { studiedSet -> studiedSet.vocabSetId == creatorSet.vocabSetId }
                    })

                    // Nếu ít hơn 5, bổ sung từ người khác
                    if (suggestedVocabSetsList.size < 5) {
                        val additionalVocabSets = vocabSetRepository.getRandomPublicVocabSets(
                            userId,
                            (5 - suggestedVocabSetsList.size).toLong()
                        )
                        suggestedVocabSetsList.addAll(additionalVocabSets.filter { additionalSet ->
                            vocabSets.none { studiedSet -> studiedSet.vocabSetId == additionalSet.vocabSetId } &&
                                    suggestedVocabSetsList.none { suggestedSet -> suggestedSet.vocabSetId == additionalSet.vocabSetId }
                        })
                    }
                } else {
                    // Nhiều người tạo: Random chọn tối đa 3 người, lấy VocabSet
                    val randomCreators = creators.shuffled().take(3)
                    Log.d("VocabSetSuggest", "Random Creators: $randomCreators")
                    for (creator in randomCreators) {
                        val creatorVocabSets = vocabSetRepository.getPublicVocabSetsByCreator(
                            creator,
                            userId,
                            (5 - suggestedVocabSetsList.size).toLong()
                        )
                        suggestedVocabSetsList.addAll(creatorVocabSets.filter { creatorSet ->
                            vocabSets.none { studiedSet -> studiedSet.vocabSetId == creatorSet.vocabSetId }
                        })
                        if (suggestedVocabSetsList.size >= 5) break
                    }
                }
            } else {
                // Chưa học VocabSet nào của người khác: Lấy ngẫu nhiên 5 VocabSet công khai, không premium
                val randomVocabSets = vocabSetRepository.getRandomPublicVocabSets(userId, 5)
                suggestedVocabSetsList.addAll(randomVocabSets.filter { randomSet ->
                    vocabSets.none { studiedSet -> studiedSet.vocabSetId == randomSet.vocabSetId }
                })
            }

            // Nếu vẫn rỗng
            if (suggestedVocabSetsList.isEmpty()) {
                val discoverLikeVocabSets = try {
                    vocabSetRepository.collection
                        .whereEqualTo("_public", true)
                        .whereEqualTo("premiumContent", false)
                        .limit(5)
                        .get()
                        .await()
                        .documents.mapNotNull { doc ->
                            doc.toObject(VocabSet::class.java)?.apply { vocabSetId = doc.id }
                        }
                } catch (e: Exception) {
                    Log.e("VocabSetSuggest", "Error fetching discover-like vocab sets: $e")
                    emptyList()
                }
                Log.d("VocabSetSuggest", "Discover-like VocabSets: ${discoverLikeVocabSets.size}")
                suggestedVocabSetsList.addAll(discoverLikeVocabSets.filter { discoverSet ->
                    vocabSets.none { studiedSet -> studiedSet.vocabSetId == discoverSet.vocabSetId }
                })
            }

            Log.d("VocabSetSuggest", "Final Suggested VocabSets: $suggestedVocabSetsList")
            suggestedVocabSets = suggestedVocabSetsList.take(5)
        }
    }

//  Upload ảnh và boundary
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                coroutineScope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(it)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        uploadedImage = bitmap
                        showImageOverlay = true
                        onShowImageOverlayChange(true)
                        // Nhận diện văn bản
                        recognizeTextFromImage(bitmap) { words ->
                            recognizedWords = words
                        }
                    } catch (e: Exception) {
                        Log.e("HomeMainScreen", "Error processing image: ${e.message}", e)
                    }
                }
            }
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xfff2f1eb))
            .padding(start = 23.dp, end = 23.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        //      Header
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .offset(
                    x = 0.dp,
                    y = 5.dp
                )
                .fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(R.drawable.eapplogo),
                contentDescription = "Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(60.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "\uD83D\uDD25",
                    fontSize = 23.sp
                )
                Text(
                    text = userState?.streak?.toString() ?: "0",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8C00)
                )
            }
        }

//        Từ điển
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(100f)
        ) {
            SearchBar(
                modifier = Modifier.offset(y = 75.dp),
                onSuggestionClick = { word ->
                    coroutineScope.launch {
                        selectedWord = dictionaryRepository.searchWord(word)
                        showWordBottomSheet = true
                    }
                },
                onCameraClick = {
                    if (isPremium) {
                        pickImageLauncher.launch("image/*")
                    } else {
                        showLimitDialog = true
                    }
                }
            )
        }

//       Các học phần
        if (studiedVocabSets.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 10.dp,
                        y = 160.dp
                    )
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Các học phần của bạn",
                    color = Color(0xffa3a3a3),
                    lineHeight = 1.25.em,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.align(Alignment.TopStart)
                )
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = 30.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 0.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(studiedVocabSets.take(5)) { vocabSet ->
                        VocabSetItem(
                            vocabSet = vocabSet,
                            onClick = {
                                coroutineScope.launch {
                                    val userId = authViewModel.getCurrentUserId() ?: return@launch
                                    val logs = studyLogRepository.getStudyLogs(userId)
                                    val today =
                                        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    val hasLogForToday =
                                        logs.any { it.date == today && it.vocabSetId == vocabSet.vocabSetId }

                                    if (!hasLogForToday) {
                                        val currentStreak =
                                            studyLogRepository.calculateStreak(userId)
                                        userRepository.updateUserStreak(userId, currentStreak)
                                        studyLogRepository.logStudySession(
                                            userId,
                                            vocabSet.vocabSetId
                                        )
                                    }

                                    navController.navigate("vocabSetDetail/${vocabSet.vocabSetId}")
                                }
                            }
                        )
                    }
                    item {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "View Library",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(20.dp)
                                .offset(y = 37.dp)
                                .rotate(180f)
                                .clickable {
                                    navController.navigate("libraryMain") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                        )
                    }
                }
            }
        }

//        Học phần liên quan
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(
                    x = 10.dp,
                    y = if (studiedVocabSets.isNotEmpty()) 360.dp else 160.dp
                )
                .fillMaxWidth()
        ) {
            Text(
                text = "Có thể bạn quan tâm",
                color = Color(0xffa3a3a3),
                lineHeight = 1.25.em,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.align(Alignment.TopStart)
            )

            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 30.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 0.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(suggestedVocabSets) { vocabSet ->
                    VocabSetItem(
                        vocabSet = vocabSet,
                        onClick = {
                            coroutineScope.launch {
                                val userId = authViewModel.getCurrentUserId() ?: return@launch
                                val logs = studyLogRepository.getStudyLogs(userId)
                                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val hasLogForToday =
                                    logs.any { it.date == today && it.vocabSetId == vocabSet.vocabSetId }

                                if (!hasLogForToday) {
                                    val currentStreak = studyLogRepository.calculateStreak(userId)
                                    userRepository.updateUserStreak(userId, currentStreak)
                                    studyLogRepository.logStudySession(userId, vocabSet.vocabSetId)
                                }

                                navController.navigate("vocabSetDetail/${vocabSet.vocabSetId}")
                            }
                        }
                    )
                }
                item {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "View Discover",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                            .offset(y = 37.dp)
                            .rotate(180f)
                            .clickable {
                                navController.navigate("discover") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                    )
                }
            }
        }
    }

    //  Overlay hiển thị ảnh
    if (showImageOverlay && uploadedImage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .zIndex(500f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.multiply),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(30.dp)
                    .zIndex(510f)
                    .clickable {
                        showImageOverlay = false
                        onShowImageOverlayChange(false)
                        uploadedImage = null
                        recognizedWords = emptyList()
                    }
            )

            // Ảnh và bounding box
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val imageWidth = uploadedImage!!.width.toFloat()
                val imageHeight = uploadedImage!!.height.toFloat()
                val canvasWidth = constraints.maxWidth.toFloat()
                val canvasHeight = constraints.maxHeight.toFloat()

                // Tính tỷ lệ và offset bên ngoài Canvas
                val scale = minOf(canvasWidth / imageWidth, canvasHeight / imageHeight)
                val scaledWidth = imageWidth * scale
                val scaledHeight = imageHeight * scale
                val offsetX = (canvasWidth - scaledWidth) / 2
                val offsetY = (canvasHeight - scaledHeight) / 2

                Log.d("CanvasDebug", "Canvas: width=$canvasWidth, height=$canvasHeight")
                Log.d("CanvasDebug", "Image: width=$imageWidth, height=$imageHeight")
                Log.d("CanvasDebug", "Scaled: width=$scaledWidth, height=$scaledHeight")
                Log.d("CanvasDebug", "Scale=$scale, OffsetX=$offsetX, OffsetY=$offsetY")

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {}
                ) {
                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(offsetX, offsetY)
                        canvas.scale(scale, scale)
                        drawImage(
                            image = uploadedImage!!.asImageBitmap(),
                            topLeft = Offset(0f, 0f)
                        )
                        canvas.restore()
                    }

                    // Vẽ bounding box với tọa độ điều chỉnh
                    recognizedWords.forEach { word ->
                        val rect = word.boundingBox
                        val left = (rect.left * scale + offsetX).toFloat().coerceAtLeast(0f)
                        val top = (rect.top * scale + offsetY).toFloat().coerceAtLeast(0f)
                        val right = (rect.right * scale + offsetX).toFloat()
                            .coerceAtMost(canvasWidth)
                        val bottom = (rect.bottom * scale + offsetY).toFloat()
                            .coerceAtMost(canvasHeight)

                        if (left < right && top < bottom) {
                            drawRect(
                                color = Color.Red,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }

                // Xử lý click vào từ với tọa độ điều chỉnh
                val density = LocalDensity.current
                recognizedWords.forEach { word ->
                    val rect = word.boundingBox
                    // Tính toán tọa độ đã scale
                    val left = rect.left * scale + offsetX
                    val top = rect.top * scale + offsetY
                    val right = rect.right * scale + offsetX
                    val bottom = rect.bottom * scale + offsetY

                    Log.d(
                        "BoundingBoxAdjusted",
                        "Word: ${word.text}, Left: $left, Top: $top, Right: $right, Bottom: $bottom"
                    )

                    with(density) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = left.toDp(),
                                    y = top.toDp()
                                )
                                .size(
                                    width = (right - left).toDp(),
                                    height = (bottom - top).toDp()
                                )
                                .zIndex(520f)
                                .clickable {
                                    Log.d("WordClick", "Clicked word: ${word.text}")
                                    coroutineScope.launch {
                                        selectedWord =
                                            dictionaryRepository.searchWord(word.text)
                                        showWordBottomSheet = true
                                    }
                                }
                                .background(Color.Green.copy(alpha = 0.3f)) // Giữ màu xanh để debug
                        )
                    }
                }
            }
        }
    }

    // Bottom sheet hiển thị nghĩa chi tiết
    if (showWordBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showWordBottomSheet = false
                selectedWord = null
            },
            containerColor = Color.White,
            modifier = Modifier.zIndex(1000f),
            windowInsets = WindowInsets(0)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 40.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nội dung từ
                if (selectedWord != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedWord!!.word,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val mediaPlayer = MediaPlayer()
                                        val url =
                                            "https://translate.google.com/translate_tts?ie=UTF-8&q=${selectedWord!!.word}&tl=en&client=tw-ob"
                                        mediaPlayer.setDataSource(url)
                                        mediaPlayer.prepare()
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener { it.release() }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.speaker),
                                contentDescription = "Play pronunciation",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    selectedWord!!.meanings.forEach { meaning ->
                        Text(
                            text = meaning.partOfSpeech,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF555555),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        meaning.definitions.forEach { definition ->
                            Text(
                                text = "- ${definition.definition}",
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Không tìm thấy nghĩa của từ này",
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

    }

    // Thêm AlertDialog cho người dùng không Premium
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
                    text = "Để sử dụng tính năng này, vui lòng nâng cấp lên tài khoản Premium!",
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
                                navController.navigate("profile") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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

                    TextButton(
                        onClick = { showLimitDialog = false },
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSuggestionClick: (String) -> Unit,
    onCameraClick: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val dictionaryRepository = DictionaryRepository()
    val focusManager = LocalFocusManager.current

    Column { // Xóa Modifier.clickable
        BasicTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                coroutineScope.launch {
                    delay(300)
                    suggestions = if (it.isNotBlank()) {
                        dictionaryRepository.getSuggestions(it.trim())
                    } else {
                        emptyList()
                    }
                }
            },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                lineHeight = 1.43.em
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .requiredWidth(360.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xfff6f6f6))
                .border(1.dp, Color(0xffbdbdc4), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (!it.isFocused) {
                        suggestions = emptyList()
                    } else {
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
                                text = "Tra từ",
                                color = Color(0xffb9bbc0),
                                fontSize = 14.sp,
                                lineHeight = 1.43.em
                            )
                        }
                        innerTextField()
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Camera",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onCameraClick() },
                        tint = Color.Black
                    )
                }
            }
        )

        if (isFocused && suggestions.isNotEmpty()) {
            Box(modifier = Modifier.zIndex(200f)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 80.dp)
                        .zIndex(1000f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            Text(
                                text = suggestion,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .clickable {
                                        searchText = suggestion
                                        onSuggestionClick(suggestion)
                                        suggestions = emptyList()
                                        isFocused = false
                                        focusManager.clearFocus()
                                    }
                            )
                            Divider(color = Color(0xFFEEEEEE))
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
        creatorUsername = user?.username ?: "E App"
        avatarBase64 = user?.avatar
    }

    Box(
        modifier = Modifier
            .requiredWidth(300.dp)
            .requiredHeight(100.dp)
            .clickable {
                coroutineScope.launch {
                    // Lấy danh sách log của người dùng
                    val logs = studyLogRepository.getStudyLogs(userId)
                    val today = LocalDate
                        .now()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)

                    // Kiểm tra xem đã có log cho ngày hôm nay chưa
                    val hasLogForToday =
                        logs.any { it.date == today && it.vocabSetId == vocabSet.vocabSetId }

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

// Hàm nhận diện văn bản sử dụng ML Kit
fun recognizeTextFromImage(bitmap: Bitmap, onResult: (List<RecognizedWord>) -> Unit) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val words = visionText.textBlocks.flatMap { block ->
                block.lines.flatMap { line ->
                    line.elements.mapNotNull { element ->
                        val boundingBox = element.boundingBox
                        if (boundingBox != null) {
                            RecognizedWord(
                                text = element.text,
                                boundingBox = boundingBox
                            )
                        } else {
                            null
                        }
                    }
                }
            }
            onResult(words)
        }
        .addOnFailureListener { e ->
            Log.e("TextRecognition", "Error: $e")
            onResult(emptyList())
        }
}