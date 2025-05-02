package com.example.learningenglishvocab.ui.screen.auth

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.ui.bottomNav.BottomNavItem
import com.example.learningenglishvocab.viewmodel.AuthViewModel

@Composable
fun UserLoginView(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var emailState by remember { mutableStateOf(TextFieldValue("")) }
    var passwordState by remember { mutableStateOf(TextFieldValue("")) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0xfff2f1eb))
    ) {
        Image(
            painter = painterResource(id = R.drawable.asset22),
            contentDescription = "header image",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(y = (-35).dp)
                .fillMaxWidth()
                .requiredHeight(height = 225.dp)
                .rotate(degrees = 0f)
        )

        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(
                    x = 24.dp,
                    y = 120.dp
                )
                .requiredWidth(width = 147.dp)
                .requiredHeight(height = 151.dp)
        ) {
            Text(
                text = "Sign",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 65.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .requiredWidth(width = 145.dp)
            )
            Text(
                text = "In",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 65.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 2.dp,
                        y = 75.dp
                    )
                    .requiredWidth(width = 145.dp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.asset71),
            contentDescription = "decor element",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(
                    x = 320.dp,
                    y = 196.dp
                )
                .requiredWidth(width = 31.dp)
                .requiredHeight(height = 32.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .offset(
                    x = (-5).dp,
                    y = 322.dp
                )
                .requiredWidth(width = 350.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Email",
                    color = Color(0xff252526),
                    lineHeight = 1.5.em,
                    style = TextStyle(
                        fontSize = 16.sp
                    )
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(24.dp))
                        .border(
                            border = BorderStroke(1.dp, Color(0xff1a1c3d)),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = emailState,
                        onValueChange = { emailState = it },
                        textStyle = TextStyle(
                            color = Color(0xff252526),
                            fontSize = 14.sp,
                            lineHeight = 1.43.em
                        ),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (emailState.text.isEmpty()) {
                                    Text(
                                        text = "Email",
                                        color = Color(0xffb9bbc0),
                                        style = TextStyle(
                                            color = Color(0xffb9bbc0),
                                            fontSize = 14.sp,
                                            lineHeight = 1.43.em
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Mật khẩu",
                    color = Color(0xff252526),
                    lineHeight = 1.5.em,
                    style = TextStyle(
                        fontSize = 16.sp
                    )
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(24.dp))
                        .border(
                            border = BorderStroke(1.dp, Color(0xff1a1c3d)),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = passwordState,
                            onValueChange = { passwordState = it },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            textStyle = TextStyle(
                                color = Color(0xff252526),
                                fontSize = 14.sp,
                                lineHeight = 1.43.em
                            ),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth(0.9f)) {
                                    if (passwordState.text.isEmpty()) {
                                        Text(
                                            text = "Mật khẩu",
                                            color = Color(0xffb9bbc0),
                                            style = TextStyle(
                                                color = Color(0xffb9bbc0),
                                                fontSize = 14.sp,
                                                lineHeight = 1.43.em
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        Icon(
                            painter = painterResource(id = if (isPasswordVisible) R.drawable.eye else R.drawable.eyeslash),
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            modifier = Modifier
                                .clickable { isPasswordVisible = !isPasswordVisible }
                                .padding(start = 8.dp)
                                .requiredSize(size = 20.dp)
                        )
                    }
                }
            }
        }
        Button(
            onClick = {
                authViewModel.login(
                    email = emailState.text,
                    password = passwordState.text,
                    onSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .offset(x = (-1.5).dp, y = 530.dp)
                .requiredWidth(width = 194.dp)
                .clip(shape = RoundedCornerShape(64.dp))
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
                    start = 16.dp,
                    end = 16.dp,
                    top = 5.dp,
                    bottom = 5.dp
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(
                    text = "Đăng nhập",
                    color = Color.White,
                    lineHeight = 0.83.em,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
        Button(
            onClick = { navController.navigate("register") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 590.dp)
                .requiredWidth(339.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                text = "Bạn chưa có tài khoản? ",
                color = Color(0xff181824),
                textAlign = TextAlign.Center,
                lineHeight = 1.47.em,
                style = TextStyle(
                    fontSize = 17.sp,
                )
            )
            Text(
                text = "Đăng ký",
                color = Color(0xffF04E39),
                textAlign = TextAlign.Center,
                lineHeight = 1.47.em,
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Image(
            painter = painterResource(id = R.drawable.asset31),
            contentDescription = "footer image 1",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(
                    x = 0.dp,
                    y = 675.dp
                )
                .requiredWidth(width = 611.dp)
                .requiredHeight(height = 191.dp)
                .rotate(degrees = 0f)
        )
        Image(
            painter = painterResource(id = R.drawable.asset32),
            contentDescription = "footer image 2",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(
                    x = 0.dp,
                    y = 620.dp
                )
                .requiredWidth(width = 458.dp)
                .requiredHeight(height = 286.dp)
        )
    }
}

object AppTypes {
    val type_Body_14_Regular = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        color = Color.Black
    )
}
