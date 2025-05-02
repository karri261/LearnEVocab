package com.example.learningenglishvocab.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.DictionaryResponse
import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.repository.DictionaryRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val userRepository = UserRepository()
    val dictionaryRepository = DictionaryRepository()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var userState by remember { mutableStateOf<User?>(null) }
    var selectedWord by remember { mutableStateOf<DictionaryResponse?>(null) }
    var showWordBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = authViewModel.getCurrentUserId() ?: return@LaunchedEffect
        userState = withContext(Dispatchers.IO) {
            userRepository.getUser(userId)
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

            Icon(
                painter = painterResource(id = R.drawable.notification),
                contentDescription = "Thông báo",
                tint = Color.Black,
                modifier = Modifier
                    .size(23.dp)
                    .align(Alignment.CenterEnd)
                    .clickable {}
            )
        }

//        Từ điển
        SearchBar(
            modifier = Modifier.offset(y = 75.dp),
            onSuggestionClick = { word ->
                coroutineScope.launch {
                    selectedWord = dictionaryRepository.searchWord(word)
                    showWordBottomSheet = true
                }
            }
        )

        // Bottom sheet hiển thị nghĩa chi tiết
        if (showWordBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showWordBottomSheet = false
                    selectedWord = null
                },
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nội dung từ
                    if (selectedWord != null) {
                        Text(
                            text = selectedWord!!.word,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
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

//        Học phần mới
//        Box(
//            modifier = Modifier
//                .align(alignment = Alignment.TopStart)
//                .offset(
//                    x = 24.dp,
//                    y = 174.dp
//                )
//                .requiredWidth(width = 371.dp)
//                .requiredHeight(height = 169.dp)
//        ) {
//            Text(
//                text = "Các học phần của bạn",
//                color = Color(0xffa3a3a3),
//                textAlign = TextAlign.Center,
//                lineHeight = 1.25.em,
//                style = AppTypes.type_Body_16px_SemiBold)
//            Box(
//                modifier = Modifier
//                    .align(alignment = Alignment.TopStart)
//                    .offset(
//                        x = 0.dp,
//                        y = 37.dp
//                    )
//                    .requiredWidth(width = 240.dp)
//                    .requiredHeight(height = 132.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .requiredWidth(width = 240.dp)
//                        .requiredHeight(height = 132.dp)
//                        .clip(shape = RoundedCornerShape(15.dp))
//                        .background(color = Color(0xfff6f6f6))
//                        .border(
//                            border = BorderStroke(1.dp, Color(0xffcac8c8)),
//                            shape = RoundedCornerShape(15.dp)
//                        ))
//                Text(
//                    text = "B1 Mastery 1",
//                    color = Color.Black,
//                    lineHeight = 1.25.em,
//                    style = TextStyle(
//                        fontSize = 16.sp),
//                    modifier = Modifier
//                        .align(alignment = Alignment.TopStart)
//                        .offset(
//                            x = 18.dp,
//                            y = 13.dp
//                        ))
//                Box(
//                    modifier = Modifier
//                        .align(alignment = Alignment.TopStart)
//                        .offset(
//                            x = 18.dp,
//                            y = 37.dp
//                        )
//                        .requiredWidth(width = 73.dp)
//                        .requiredHeight(height = 19.dp)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .requiredWidth(width = 73.dp)
//                            .requiredHeight(height = 19.dp)
//                            .clip(shape = RoundedCornerShape(15.dp))
//                            .background(color = Color(0xffe2e2e2)))
//                    Text(
//                        text = "35 từ vựng",
//                        color = Color.Black.copy(alpha = 0.7f),
//                        lineHeight = 2.22.em,
//                        style = TextStyle(
//                            fontSize = 9.sp),
//                        modifier = Modifier
//                            .align(alignment = Alignment.TopStart)
//                            .offset(
//                                x = 12.dp,
//                                y = 0.dp
//                            )
//                            .requiredWidth(width = 50.dp)
//                            .requiredHeight(height = 16.dp))
//                }
//                Box(
//                    modifier = Modifier
//                        .align(alignment = Alignment.TopStart)
//                        .offset(
//                            x = 18.dp,
//                            y = 96.dp
//                        )
//                        .requiredWidth(width = 46.dp)
//                        .requiredHeight(height = 23.dp)
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.image),
//                        contentDescription = "image",
//                        modifier = Modifier
//                            .requiredSize(size = 23.dp)
//                            .clip(shape = CircleShape))
//                    Text(
//                        text = "Tôi",
//                        color = Color(0xff343333),
//                        lineHeight = 1.67.em,
//                        style = TextStyle(
//                            fontSize = 12.sp),
//                        modifier = Modifier
//                            .align(alignment = Alignment.TopStart)
//                            .offset(
//                                x = 28.dp,
//                                y = 3.dp
//                            )
//                            .wrapContentHeight(align = Alignment.CenterVertically))
//                }
//            }
//        }

//        Học phần liên quan
//        Box(
//            modifier = Modifier
//                .align(alignment = Alignment.TopStart)
//                .offset(
//                    x = 24.dp,
//                    y = 376.dp
//                )
//                .requiredWidth(width = 371.dp)
//                .requiredHeight(height = 169.dp)
//        ) {
//            Text(
//                text = "Có thể bạn quan tâm",
//                color = Color(0xffa3a3a3),
//                textAlign = TextAlign.Center,
//                lineHeight = 1.25.em,
//                style = AppTypes.type_Body_16px_SemiBold,
//                modifier = Modifier
//                    .align(alignment = Alignment.TopStart)
//                    .offset(
//                        x = 5.dp,
//                        y = 0.dp
//                    ))
//            Box(
//                modifier = Modifier
//                    .align(alignment = Alignment.TopStart)
//                    .offset(
//                        x = 0.dp,
//                        y = 37.dp
//                    )
//                    .requiredWidth(width = 240.dp)
//                    .requiredHeight(height = 132.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .requiredWidth(width = 240.dp)
//                        .requiredHeight(height = 132.dp)
//                        .clip(shape = RoundedCornerShape(15.dp))
//                        .background(color = Color(0xfff6f6f6))
//                        .border(
//                            border = BorderStroke(1.dp, Color(0xffcac8c8)),
//                            shape = RoundedCornerShape(15.dp)
//                        ))
//                Text(
//                    text = "Destination C1 SET",
//                    color = Color.Black,
//                    lineHeight = 1.25.em,
//                    style = TextStyle(
//                        fontSize = 16.sp),
//                    modifier = Modifier
//                        .align(alignment = Alignment.TopStart)
//                        .offset(
//                            x = 18.dp,
//                            y = 13.dp
//                        ))
//                Box(
//                    modifier = Modifier
//                        .align(alignment = Alignment.TopStart)
//                        .offset(
//                            x = 18.dp,
//                            y = 37.dp
//                        )
//                        .requiredWidth(width = 73.dp)
//                        .requiredHeight(height = 19.dp)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .requiredWidth(width = 73.dp)
//                            .requiredHeight(height = 19.dp)
//                            .clip(shape = RoundedCornerShape(15.dp))
//                            .background(color = Color(0xffe2e2e2)))
//                    Text(
//                        text = "45 từ vựng",
//                        color = Color.Black.copy(alpha = 0.7f),
//                        lineHeight = 2.22.em,
//                        style = TextStyle(
//                            fontSize = 9.sp),
//                        modifier = Modifier
//                            .align(alignment = Alignment.TopStart)
//                            .offset(
//                                x = 12.dp,
//                                y = 0.dp
//                            )
//                            .requiredWidth(width = 61.dp)
//                            .requiredHeight(height = 16.dp))
//                }
//                Box(
//                    modifier = Modifier
//                        .align(alignment = Alignment.TopStart)
//                        .offset(
//                            x = 46.dp,
//                            y = 99.dp
//                        )
//                        .requiredWidth(width = 43.dp)
//                        .requiredHeight(height = 20.dp)
//                ) {
//                    Text(
//                        text = "wolf.32",
//                        color = Color(0xff343333),
//                        lineHeight = 1.67.em,
//                        style = TextStyle(
//                            fontSize = 12.sp),
//                        modifier = Modifier
//                            .wrapContentHeight(align = Alignment.CenterVertically))
//                }
//            }
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 80.dp),
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