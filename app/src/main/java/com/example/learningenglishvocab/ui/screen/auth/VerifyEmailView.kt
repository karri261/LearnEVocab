package com.example.learningenglishvocab.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun VerifyEmailView(modifier: Modifier = Modifier, navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        while (true) {
            currentUser?.reload()?.await()  // Cập nhật trạng thái tài khoản
            if (currentUser?.isEmailVerified == true) {
                navController.navigate("home")
                break
            }
            delay(1000) // Kiểm tra lại sau 5 giây
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0xfff2f1eb))
    ) {
        Image(
            painter = painterResource(id = R.drawable.verifyemail),
            contentDescription = "image 5",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(
                    x = 150.dp,
                    y = 254.dp
                )
                .requiredSize(size = 105.dp)
        )
        Text(
            text = "Xác minh địa chỉ email của bạn",
            textAlign = TextAlign.Center,
            color = Color.Black,
            lineHeight = 1.em,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.TopStart)
                .offset(y = 389.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        Text(
            text = "Vui lòng nhấn vào link trong email mà chúng tôi đã gửi cho bạn để xác minh tài khoản",
            color = Color(0xffa3a3a3),
            textAlign = TextAlign.Center,
            lineHeight = 1.67.em,
            style = TextStyle(
                fontSize = 15.sp
            ),
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .offset(
                    x = 0.dp,
                    y = 426.dp
                )
                .requiredWidth(width = 339.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        Button(
            onClick = { viewModel.resendVerificationEmail() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 490.dp)
                .requiredWidth(339.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                text = "Gửi lại",
                color = Color(0xfff26c3c),
                textAlign = TextAlign.Center,
                lineHeight = 1.47.em,
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Button(
            onClick = { navController.navigate("register") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 520.dp)
                .requiredWidth(339.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                text = "Quay lại",
                color = Color(0xff929295),
                textAlign = TextAlign.Center,
                lineHeight = 1.47.em,
                style = TextStyle(
                    fontSize = 17.sp,
                )
            )
        }
    }
}

@Preview
@Composable
private fun XcMinhEmailPreview() {
    val navController = rememberNavController()
    val viewModel = AuthViewModel()
    VerifyEmailView(Modifier, navController, viewModel)
}