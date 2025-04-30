package com.example.learningenglishvocab.ui.screen.home

import androidx.compose.foundation.Image
import com.example.learningenglishvocab.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learningenglishvocab.viewmodel.AuthViewModel

@Composable
fun HomeView(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0xfff2f1eb))
    ) {
        Image(
            painter = painterResource(id = R.drawable.eapplogo),
            contentDescription = "EApp Logo 1",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 90.dp,
                    y = 310.dp)
                .requiredWidth(width = 235.dp)
                .requiredHeight(height = 192.dp))
    }
    Button(
        onClick = {
            authViewModel.logout()
        },
        modifier = modifier
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF74C54)),
    ) {
        Text(
            text = "Đăng xuất",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}