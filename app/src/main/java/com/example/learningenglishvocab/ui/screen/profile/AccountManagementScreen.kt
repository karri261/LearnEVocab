package com.example.learningenglishvocab.ui.screen.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    Log.d("AccountManagement", "AccountManagementScreen recomposed")
    val userId = authViewModel.getCurrentUserId()
    if (userId == null) {
        Log.d("AccountManagement", "userId is null, relying on EApp navigation")
        return
    }

    val userRepository = UserRepository()
    var userState by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userState = userRepository.getUser(userId)
    }

    // Dialog xác nhận xóa tài khoản
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa tài khoản") },
            text = {
                Column {
                    Text("Nhập mật khẩu hiện tại để xác nhận xóa tài khoản. Hành động này không thể hoàn tác.")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        placeholder = { Text("Mật khẩu hiện tại") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentPassword.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                        } else {
                            val email = authViewModel.getCurrentUserEmail() ?: ""
                            if (email.isBlank()) {
                                Toast.makeText(context, "Không tìm thấy email, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            } else {
                                authViewModel.deleteAccount(
                                    email = email,
                                    currentPassword = currentPassword,
                                    onSuccess = {
                                        Toast.makeText(context, "Tài khoản đã được xóa!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                                showDeleteDialog = false
                                currentPassword = ""
                            }
                        }
                    }
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    currentPassword = ""
                }) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F1EB))
            .padding(16.dp)
    ) {
        // Header với nút quay lại
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Quản lý tài khoản",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // Cân đối với IconButton
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6))
                .height(50.dp)
                .clip(RoundedCornerShape(15.dp))
                .border(
                    border = BorderStroke(1.dp, Color(0xff929295).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nâng cấp tài khoản của bạn",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Button(
                onClick = {},
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(50.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFFF74C54),
                                0.75f to Color(0xFFFA8246),
                                1f to Color(0xFFFEAC2F)
                            ),
                        )
                    )
                    .height(40.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = "Nâng cấp",
                    color = Color.White,
                    lineHeight = 0.83.em,
                    fontSize = 17.sp,
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Phần thông tin cá nhân
        Text(
            text = "Thông tin cá nhân",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6))
                .border(
                    border = BorderStroke(1.dp, Color(0xff929295).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(15.dp)
                )
                .clip(RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F6F6))
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 13.dp)
            ) {
                // Tên người dùng
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF6F6F6)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tên người dùng",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = userState?.username ?: "E-App User",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Edit username",
                        tint = Color.Gray,
                        modifier = Modifier.clickable { navController.navigate("changeUsername") }
                    )
                }
                Divider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = Color(0xFF929295).copy(alpha = 0.5f),
                    thickness = 1.5.dp
                )

                // Email
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF6F6F6)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Email",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = userState?.email ?: "tennguoidung${userId.takeLast(8)}@gmail.com",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                Divider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = Color(0xFF929295).copy(alpha = 0.5f),
                    thickness = 1.5.dp
                )

                // Mật khẩu
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF6F6F6)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mật khẩu",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "********",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Change password",
                        tint = Color.Gray,
                        modifier = Modifier.clickable { navController.navigate("changePassword") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Phần thông báo
        Text(
            text = "Thông báo",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6))
                .clip(RoundedCornerShape(8.dp))
                .border(
                    border = BorderStroke(1.dp, Color(0xff929295).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 16.dp, vertical = 5.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F6F6)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhắc nhở",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Switch(
                    checked = false, // Giá trị mặc định
                    onCheckedChange = { /* Chưa triển khai chức năng */ },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF8C00),
                        uncheckedThumbColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Phần giới thiệu
        Text(
            text = "Giới thiệu",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F6F6)).background(Color(0xFFF6F6F6))
                .border(
                    border = BorderStroke(1.dp, Color(0xff929295).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(15.dp)
                )
                .clip(RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F6F6))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF6F6F6))
                        .clickable { navController.navigate("privacyAndSupport") },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quyền riêng tư",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Privacy",
                        tint = Color.Gray
                    )
                }
                Divider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    color = Color(0xFF929295).copy(alpha = 0.5f),
                    thickness = 1.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF6F6F6))
                        .clickable { navController.navigate("privacyAndSupport") },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Điều khoản dịch vụ",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Terms of Service",
                        tint = Color.Gray
                    )
                }
                Divider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    color = Color(0xFF929295).copy(alpha = 0.5f),
                    thickness = 1.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF6F6F6))
                        .clickable { navController.navigate("privacyAndSupport") },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trung tâm hỗ trợ",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Support",
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút đăng xuất
        Button(
            onClick = {
                Log.d("AccountManagement", "Logout button clicked")
                authViewModel.logout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA6F3E))
        ) {
            Text(
                text = "Đăng xuất",
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút xóa tài khoản
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
        ) {
            Text(
                text = "Xóa tài khoản",
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}