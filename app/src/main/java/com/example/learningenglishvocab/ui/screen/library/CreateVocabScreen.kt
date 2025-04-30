package com.example.learningenglishvocab.ui.screen.library

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.viewmodel.LibraryViewModel
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreateVocabScreen(
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel,
    vocabSetViewModel: VocabSetViewModel,
    navController: NavController,
    vocabSetId: String? = null,
) {
    val context = LocalContext.current

    LaunchedEffect(vocabSetId) {
        if (!vocabSetId.isNullOrEmpty()) {
            vocabSetViewModel.loadVocabSetById(vocabSetId)
        } else {
            vocabSetViewModel.clear()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xfff2f1eb))
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .offset(
                    x = 0.dp,
                    y = 23.dp
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigate("libraryMain") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.multiply),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Tạo học phần",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
            )
            IconButton(
                onClick = {
                    if (!vocabSetViewModel.vocabSetId.isNullOrEmpty()) {
                        // Chế độ chỉnh sửa
                        vocabSetViewModel.updateVocabSet(
                            onSuccess = {
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT)
                                    .show()
                                libraryViewModel.loadAllVocabSets()
                                navController.popBackStack()
                            },
                            onFailure = {
                                Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    } else {
                        // Chế độ tạo mới
                        vocabSetViewModel.saveToFirebase(
                            onSuccess = {
                                Toast.makeText(context, "Lưu thành công", Toast.LENGTH_SHORT).show()
                                libraryViewModel.loadAllVocabSets()
                                navController.popBackStack()
                            },
                            onFailure = {
                                Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.done),
                    contentDescription = "Done",
                    tint = Color.Black
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .offset(x = 16.dp, y = 80.dp)
            .requiredWidth(width = 360.dp)
    ) {
        BasicTextField(
            value = vocabSetViewModel.vocabSetName,
            onValueChange = { vocabSetViewModel.vocabSetName = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                lineHeight = 1.43.em,
                color = Color.Black
            ),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Transparent)
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 10.dp),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.weight(1f)) {
                        if (vocabSetViewModel.vocabSetName.isEmpty()) {
                            Text(
                                text = "Chủ đề, học phần",
                                color = Color(0xff858585),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 1.43.em
                                )
                            )
                        }
                        innerTextField()
                    }
                    Divider(color = Color(0xff737070), thickness = 1.dp)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .offset(
                x = 16.dp,
                y = 130.dp
            )
    ) {
        Text(
            text = "Ai có thể xem nội dung này: ",
            color = Color.Black.copy(alpha = 0.8f),
            lineHeight = 2.em,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .offset(x = 36.dp, y = 0.dp)
        )

        Text(
            text = if (vocabSetViewModel.isPublic) "Mọi người" else "Chỉ mình tôi",
            color = Color.Black.copy(alpha = 0.8f),
            lineHeight = 2.em,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .offset(x = 220.dp, y = 0.dp)
                .clickable { vocabSetViewModel.togglePrivacy() }
        )
        Image(
            painter = painterResource(id = R.drawable.setting),
            contentDescription = "Group",
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .offset(x = 16.dp, y = 0.dp)
                .requiredSize(size = 16.dp)
                .clickable { vocabSetViewModel.togglePrivacy() }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 164.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
        contentPadding = PaddingValues(bottom = 20.dp) // Thêm padding dưới
    ) {
        itemsIndexed(vocabSetViewModel.terms, key = { _, term -> term.id }) { index, term ->

            // Tạo dismissState cho từng phần tử trong danh sách
            val dismissState = rememberDismissState()

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.EndToStart),
                background = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xoá",
                            tint = Color.Red
                        )
                    }
                },
                dismissContent = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .background(color = Color(0xfff7f7f7))
                                .border(BorderStroke(1.dp, Color(0xffeaeaea)))
                                .padding(16.dp)
                        ) {
                            // TextField Thuật ngữ
                            BasicTextField(
                                value = term.term,
                                onValueChange = { vocabSetViewModel.updateTerm(index, it) },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 1.43.em,
                                    color = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .padding(bottom = 4.dp),
                                decorationBox = { innerTextField ->
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        ) {
                                            if (term.term.isEmpty()) {
                                                Text(
                                                    text = "Thuật ngữ",
                                                    color = Color(0xff858585),
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        lineHeight = 1.43.em
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                        Divider(color = Color(0xff737070), thickness = 1.dp)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // TextField Định nghĩa
                            BasicTextField(
                                value = term.definition,
                                onValueChange = {
                                    vocabSetViewModel.updateDefinition(
                                        index,
                                        it
                                    )
                                },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 1.43.em,
                                    color = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent),
                                decorationBox = { innerTextField ->
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 12.dp, bottom = 8.dp)
                                        ) {
                                            if (term.definition.isEmpty()) {
                                                Text(
                                                    text = "Định nghĩa",
                                                    color = Color(0xff858585),
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        lineHeight = 1.43.em
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                        Divider(color = Color(0xff737070), thickness = 1.dp)
                                    }
                                }
                            )
                        }
                    }
                }
            )

            // Xử lý khi phần tử bị xoá
            LaunchedEffect(dismissState.isDismissed(DismissDirection.EndToStart)) {
                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                    vocabSetViewModel.removeTerm(index)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = { vocabSetViewModel.addTermField() },
            containerColor = Color(0xFFFF7043),
            shape = CircleShape,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Thêm thuật ngữ",
                tint = Color.White
            )
        }
    }
}
