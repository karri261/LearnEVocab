package com.example.learningenglishvocab.ui.screen.discover

import android.annotation.SuppressLint
import android.util.Log
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.Term
import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.model.VocabSet
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.example.learningenglishvocab.viewmodel.LibraryViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DiscoverMainScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
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
            Text(
                text = "Khám phá",
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 1.18.em,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 120.dp, y = 10.dp)
            )
        }

//      Tab Layout
        var selectedTab by remember { mutableStateOf(0) } // 0: Chủ đề, 1: Mọi người

        Column(
            modifier = Modifier
                .offset(y = 50.dp)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Tab Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton(
                    text = "Chủ đề",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                Divider(
                    color = Color(0xffc3c1c1),
                    modifier = Modifier
                        .width(1.dp)
                        .height(23.dp)
                )
                TabButton(
                    text = "Mọi người",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }

            // Divider
            Divider(
                color = Color(0xffc3c1c1),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // Content
            when (selectedTab) {
                0 -> TopicScreen(navController)
                1 -> PeopleScreen(navController)
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (isSelected) Color(0xff212020) else Color(0xffa89c9c),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        ),
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun TopicScreen(
    navController: NavController
) {
    val authViewModel = AuthViewModel()
    val userRepository = UserRepository()
    val currentUserId = authViewModel.getCurrentUserId()
    var user by remember { mutableStateOf<User?>(null) }
    var adminVocabSets by remember { mutableStateOf<List<VocabSet>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            user = userRepository.getUser(currentUserId)
        }

        // Lấy VocabSet của admin
        FirebaseFirestore.getInstance().collection("vocab_sets")
            .whereEqualTo("created_by", "4VYH1zG1ddawiFs6wfq9zLYkUa43")
            .get()
            .addOnSuccessListener { result ->
                val vocabSets = result.documents.mapNotNull { doc ->
                    val terms = (doc["terms"] as? List<Map<String, String>>)?.map {
                        Term(it["term"] ?: "", it["definition"] ?: "")
                    } ?: emptyList()
                    VocabSet(
                        vocabSetId = doc.id,
                        vocabSetName = doc["vocabSetName"] as? String ?: "",
                        created_by = doc["created_by"] as? String ?: "",
                        _public = doc["_public"] as? Boolean ?: true,
                        created_at = doc["created_at"] as? Long ?: 0L,
                        updated_at = doc["updated_at"] as? Long ?: 0L,
                        terms = terms,
                        premiumContent = doc["premiumContent"] as? Boolean ?: false
                    )
                }
                adminVocabSets = vocabSets.sortedBy { it.premiumContent }
            }
            .addOnFailureListener { exception ->
                Log.e("TopicScreen", "Error fetching admin vocab sets", exception)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (adminVocabSets.isEmpty()) {
            Text(
                text = "Chưa có bộ từ vựng nào",
                style = TextStyle(fontSize = 16.sp, color = Color.Gray)
            )
        } else {
            LazyColumn {
                items(adminVocabSets) { vocabSet ->
                    VocabSetItem(
                        vocabSet = vocabSet,
                        isPremiumUser = user?.premium ?: false,
                        onClick = {
                            navController?.navigate("vocabSetDetail/${vocabSet.vocabSetId}")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PeopleScreen(
    navController: NavController
) {
    val authViewModel = AuthViewModel()
    val userRepository = UserRepository()
    val currentUserId = authViewModel.getCurrentUserId()
    var user by remember { mutableStateOf<User?>(null) }
    var allVocabSets by remember { mutableStateOf<List<VocabSet>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            user = userRepository.getUser(currentUserId)
        }

        // Lấy VocabSet công khai, không phải của admin
        FirebaseFirestore.getInstance().collection("vocab_sets")
            .whereEqualTo("_public", true)
            .whereNotEqualTo("created_by", "4VYH1zG1ddawiFs6wfq9zLYkUa43")
            .get(Source.SERVER)
            .addOnSuccessListener { result ->
                val vocabSets = result.documents.mapNotNull { doc ->
                    val terms = (doc["terms"] as? List<Map<String, String>>)?.map {
                        Term(it["term"] ?: "", it["definition"] ?: "")
                    } ?: emptyList()
                    val isPremiumContent = doc["premiumContent"] as? Boolean ?: false
                    val createdBy = doc["created_by"] as? String ?: ""
                    Log.d("PeopleScreen", "VocabSet ${doc.id}: premiumContent = $isPremiumContent")
                    if (createdBy == currentUserId) {
                        return@mapNotNull null
                    }
                    VocabSet(
                        vocabSetId = doc.id,
                        vocabSetName = doc["vocabSetName"] as? String ?: "",
                        created_by = doc["created_by"] as? String ?: "",
                        _public = doc["_public"] as? Boolean ?: true,
                        created_at = doc["created_at"] as? Long ?: 0L,
                        updated_at = doc["updated_at"] as? Long ?: 0L,
                        terms = terms,
                        premiumContent = isPremiumContent
                    )
                }
                allVocabSets = vocabSets
                Log.d("PeopleScreen", "Fetched ${vocabSets.size} public vocab sets")
            }
            .addOnFailureListener { exception ->
                Log.e("PeopleScreen", "Error fetching public vocab sets: ${exception.message}", exception)
            }
    }

    // Lọc VocabSet theo searchText
    val filteredVocabSets = remember(allVocabSets, searchText) {
        if (searchText.isBlank()) {
            allVocabSets
        } else {
            allVocabSets.filter {
                it.vocabSetName.contains(searchText, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ô tìm kiếm
        BasicTextField(
            value = searchText,
            onValueChange = { searchText = it },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                lineHeight = 1.43.em
            ),
            modifier = Modifier
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
                                text = "Tìm kiếm học phần",
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

        // Danh sách VocabSet
        if (filteredVocabSets.isEmpty()) {
            Text(
                text = if (searchText.isEmpty()) "Chưa có học phần công khai" else "Không tìm thấy học phần",
                style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 30.dp),
            ) {
                items(filteredVocabSets, key = { it.vocabSetId }) { vocabSet ->
                    VocabSetItem(
                        vocabSet = vocabSet,
                        isPremiumUser = user?.premium ?: false,
                        onClick = {
                            if (!vocabSet.premiumContent || user?.premium == true) {
                                navController?.navigate("vocabSetDetail/${vocabSet.vocabSetId}")
                            } else {
                                Log.d("PeopleScreen", "Cannot access premium VocabSet: ${vocabSet.vocabSetId}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun VocabSetItem(
    vocabSet: VocabSet,
    isPremiumUser: Boolean,
    onClick: () -> Unit
) {
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

    // Kiểm tra xem VocabSet có thể click được không
    val isClickable = !vocabSet.premiumContent || isPremiumUser

    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .requiredWidth(360.dp)
            .requiredHeight(100.dp)
            .clickable(
                enabled = isClickable,
                onClick = {
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
            )
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(15.dp))
                .background(
                    if (vocabSet.premiumContent && !isPremiumUser) Color(0xffd4d4d4) else Color(0xfff6f6f6)
                )
                .border(1.dp, Color(0xffcac8c8), RoundedCornerShape(15.dp))
                .fillMaxSize()
        )

        // Icon khóa cho premium content (không premium user)
        if (vocabSet.premiumContent && !isPremiumUser) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Premium content",
                tint = Color(0xff343333),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .size(20.dp)
            )
        }

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
            if (vocabSet.created_by == "4VYH1zG1ddawiFs6wfq9zLYkUa43") {
                // Hiển thị cho admin
                Image(
                    painter = painterResource(id = R.drawable.eapplogo),
                    contentDescription = "E App Avatar",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "E App",
                    color = Color(0xff343333),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(bottom = 1.dp)
                )
            } else {
                // Hiển thị cho người dùng không phải admin
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
                        .size(20.dp)
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
}