package com.example.learningenglishvocab.ui.screen.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.Term
import com.example.learningenglishvocab.data.model.TermStatus
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel

@Composable
fun VocabLearnResultScreen(
    modifier: Modifier = Modifier,
    vocabSetViewModel: VocabSetViewModel,
    navController: NavController,
    vocabSetId: String? = null
) {
    val answeredTerms = vocabSetViewModel.answeredTerms
    val unknownTerms = vocabSetViewModel.unknownTerms
    val knownCount = vocabSetViewModel.terms.count { it.status == TermStatus.KNOWN }
    val unknownCount = vocabSetViewModel.terms.count { it.status != TermStatus.KNOWN }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xfff2f1eb))
            .padding(start = 16.dp, end = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .offset(y = 23.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (unknownCount == 0) {
                        vocabSetViewModel.clearLearningResults()
                    }
                    navController.navigate("vocabSetDetail/${vocabSetId}")
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.multiply),
                    contentDescription = "Close",
                    tint = Color.Black
                )
            }
            Text(
                text = "Kết quả luyện tập",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.offset(x = 80.dp)
            )
        }

        // Hiển thị số lượng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$unknownCount",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(text = "Chưa biết", fontSize = 16.sp, color = Color.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$knownCount",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(text = "Đã biết", fontSize = 16.sp, color = Color.Black)
            }
        }

        // Nút luyện tập lại
        Button(
            onClick = {
                if (unknownCount == 0) {
                    vocabSetViewModel.clearLearningResults()
                    if (vocabSetId != null) {
                        navController.navigate("vocabLearn/$vocabSetId?retry=false")
                    }
                } else {
                    if (unknownTerms.isNotEmpty() && vocabSetId != null) {
                        navController.navigate("vocabLearn/$vocabSetId?retry=true")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB74D),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 180.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(5.dp)
        ) {
            Text(
                text = if (unknownCount == 0) {
                    "Luyện tập lại bộ từ vựng"
                } else {
                    "Luyện tập lại các từ chưa biết"
                },
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }

        // Hiển thị danh sách từ theo lượt
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 260.dp)
        ) {
            val rounds = answeredTerms.map { it.round }.distinct().sorted()
            rounds.forEach { round ->
                item {
                    Text(
                        text = "Lượt $round",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                val termsInRound = answeredTerms
                    .filter { it.round == round }
                    .groupBy { it.term.id }
                    .map { it.value.first() }
                items(termsInRound) { answered ->
                    TermResultCard(
                        term = answered.term,
                        isCorrect = answered.isCorrect
                    )
                }
            }
        }
    }
}

@Composable
fun TermResultCard(term: Term, isCorrect: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF7F7F7) // Màu nền cố định
        )
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = term.term,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = term.definition,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}