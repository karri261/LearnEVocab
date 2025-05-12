package com.example.learningenglishvocab.ui.screen.home

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.DictionaryResponse
import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.model.VocabSet
import com.example.learningenglishvocab.data.repository.DictionaryRepository
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.data.repository.VocabSetRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
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

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val userRepository = UserRepository()
    val dictionaryRepository = DictionaryRepository()
    val studyLogRepository = StudyLogRepository()
    val vocabSetRepository = VocabSetRepository()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var userState by remember { mutableStateOf<User?>(null) }
    var studiedVocabSets by remember { mutableStateOf<List<VocabSet>>(emptyList()) }
    var suggestedVocabSets by remember { mutableStateOf<List<VocabSet>>(emptyList()) }
    var selectedWord by remember { mutableStateOf<DictionaryResponse?>(null) }
    var showWordBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = authViewModel.getCurrentUserId() ?: return@LaunchedEffect
        userState = withContext(Dispatchers.IO) {
            userRepository.getUser(userId)
        }

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
                    val creatorVocabSets = vocabSetRepository.getPublicVocabSetsByCreator(creators.first(), userId, 5)
                    suggestedVocabSetsList.addAll(creatorVocabSets.filter { creatorSet ->
                        vocabSets.none { studiedSet -> studiedSet.vocabSetId == creatorSet.vocabSetId }
                    })

                    // Nếu ít hơn 5, bổ sung từ người khác
                    if (suggestedVocabSetsList.size < 5) {
                        val additionalVocabSets = vocabSetRepository.getRandomPublicVocabSets(userId, (5 - suggestedVocabSetsList.size).toLong())
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
                        val creatorVocabSets = vocabSetRepository.getPublicVocabSetsByCreator(creator, userId, (5 - suggestedVocabSetsList.size).toLong())
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
                }
            )
        }

        // Bottom sheet hiển thị nghĩa chi tiết
        if (showWordBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showWordBottomSheet = false
                    selectedWord = null
                },
                containerColor = Color.White,
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
                                            val url = "https://translate.google.com/translate_tts?ie=UTF-8&q=${selectedWord!!.word}&tl=en&client=tw-ob"
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

//       Các học phần
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

//        Học phần liên quan
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(
                    x = 10.dp,
                    y = 360.dp
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSuggestionClick: (String) -> Unit
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
        creatorUsername = user?.username ?: "Unknown"
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