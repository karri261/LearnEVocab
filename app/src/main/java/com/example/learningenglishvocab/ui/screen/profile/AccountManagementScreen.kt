package com.example.learningenglishvocab.ui.screen.profile

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.Transaction
import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
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

    var showUpgradeBottomSheet by remember { mutableStateOf(false) }
    var customerConfig by remember { mutableStateOf<PaymentSheet.CustomerConfiguration?>(null) }
    var paymentIntentClientSecret by remember { mutableStateOf<String?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }
    var isPaymentLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                coroutineScope.launch {
                    val userId = authViewModel.getCurrentUserId() ?: return@launch
                    userState?.let { currentUser ->
                        val updatedUser = currentUser.copy(premium = true)
                        val success = userRepository.updateUser(updatedUser)
                        if (success) {
                            userState = updatedUser
                            showUpgradeBottomSheet = false
                        }
                        val transaction = Transaction(
                            userId = userId,
                            paymentIntentId = paymentIntentClientSecret?.substringAfter("pi_")
                                ?: "",
                            amount = 59900000,
                            currency = "vnd",
                            timestamp = System.currentTimeMillis(),
                            status = "completed"
                        )
                        userRepository.saveTransaction(transaction)
                    }
                }
            }

            is PaymentSheetResult.Canceled -> {
                paymentError = "Thanh toán đã bị hủy"
            }

            is PaymentSheetResult.Failed -> {
                paymentError = "Lỗi thanh toán: ${paymentSheetResult.error.message}"
            }
        }
    }

    val paymentSheet = rememberPaymentSheet(::onPaymentSheetResult)

    LaunchedEffect(Unit) {
        userState = userRepository.getUser(userId)
    }

    LaunchedEffect(showUpgradeBottomSheet) {
        if (showUpgradeBottomSheet && paymentIntentClientSecret == null) {
            isPaymentLoading = true
            val backendUrl = "https://stripe-backend-eia6.onrender.com/payment-sheet"
            Log.d("PaymentDebug", "Sending request to $backendUrl with body: {}")
            backendUrl.httpPost()
                .header("Content-Type" to "application/json")
                .body("{}")
                .timeout(30000) // Tăng timeout lên 30 giây để xử lý server chậm
                .responseJson { request, response, result ->
                    isPaymentLoading = false
                    Log.d(
                        "PaymentDebug",
                        "Response: ${response.responseMessage}, Body: ${response.data.decodeToString()}"
                    )
                    when (result) {
                        is Result.Success -> {
                            try {
                                val responseJson = result.get().obj()
                                paymentIntentClientSecret = responseJson.getString("paymentIntent")
                                customerConfig = PaymentSheet.CustomerConfiguration(
                                    id = responseJson.getString("customer"),
                                    ephemeralKeySecret = responseJson.getString("ephemeralKey")
                                )
                                val publishableKey = responseJson.getString("publishableKey")
                                PaymentConfiguration.init(context, publishableKey)
                                Log.d("PaymentDebug", "Payment data loaded successfully")
                            } catch (e: Exception) {
                                paymentError = "Lỗi xử lý dữ liệu thanh toán: ${e.message}"
                                Log.e("PaymentDebug", "Error parsing response: ${e.message}", e)
                            }
                        }

                        is Result.Failure -> {
                            paymentError =
                                "Lỗi kết nối với máy chủ: ${result.getException().message}"
                            Log.e(
                                "PaymentDebug",
                                "Request failed: ${result.getException().message}, Response: ${response.data.decodeToString()}",
                                result.getException()
                            )
                        }
                    }
                }
        }
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
                            Toast.makeText(context, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val email = authViewModel.getCurrentUserEmail() ?: ""
                            if (email.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Không tìm thấy email, vui lòng đăng nhập lại",
                                    Toast.LENGTH_SHORT
                                ).show()
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                authViewModel.deleteAccount(
                                    email = email,
                                    currentPassword = currentPassword,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Tài khoản đã được xóa!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
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

    val nextYearDate by remember {
        mutableStateOf(
            LocalDate.now().plusYears(1)
                .format(DateTimeFormatter.ofPattern("d 'tháng' M, yyyy"))
        )
    }

    if (showUpgradeBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUpgradeBottomSheet = false },
            containerColor = Color(0xFFF2F1EB)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .background(Color(0xFFF2F1EB)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.multiply),
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(23.dp)
                            .clickable { showUpgradeBottomSheet = false }
                    )
                    Spacer(modifier = Modifier.width(120.dp))
                    Text(
                        text = "Gói đăng ký",
                        color = Color.Black,
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .offset(x = 280.dp, y = (-10).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4448)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "-30%",
                                color = Color.White,
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                        Column {
                            Text(
                                text = "Hàng năm",
                                color = Color.Black,
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "599.000₫ ≈ $21.84",
                                    color = Color.Black,
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "/ năm",
                                    color = Color.Black,
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Huỷ gia hạn tối đa 24 giờ trước khi thời gian sử dụng kết thúc.",
                                color = Color.Black,
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Text(
                    text = "Cách hoạt động của gói đăng ký hàng năm",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(x = 40.dp),
                    color = Color.Black,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF66D3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.lock),
                            contentDescription = "Vector",
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Hôm nay: Truy cập tức thì",
                            color = Color.Black,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Bạn được tính phí cho một năm dịch vụ",
                            color = Color(0xFF6D6A6A),
                            style = TextStyle(fontSize = 13.sp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = 4.dp, y = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        color = Color.Black,
                        modifier = Modifier
                            .width(22.dp)
                            .rotate(-90f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF66D3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.restart),
                            contentDescription = "Restart",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Ngày ${nextYearDate}: Gia hạn",
                            color = Color.Black,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Gói đăngLimited time offer ký của bạn được gia hạn thêm một năm trừ khi bạn huỷ gói cước trước ngày này",
                            color = Color(0xFF6D6A6A),
                            style = TextStyle(fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }
                }
                Button(
                    onClick = {
                        if (customerConfig != null && paymentIntentClientSecret != null) {
                            paymentSheet.presentWithPaymentIntent(
                                paymentIntentClientSecret!!,
                                PaymentSheet.Configuration(
                                    merchantDisplayName = "E-App",
                                    customer = customerConfig,
                                    allowsDelayedPaymentMethods = false
                                )
                            )
                        } else {
                            paymentError = "Chưa sẵn sàng để thanh toán. Vui lòng thử lại."
                        }
                    },
                    enabled = !isPaymentLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, Color(0xFFAEAEAC)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFF74C54),
                                        Color(0xFFF9674D),
                                        Color(0xFFFEAC2F)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nâng cấp tài khoản ngay",
                            color = Color.White,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }
                if (paymentError != null) {
                    Text(
                        text = paymentError!!,
                        color = Color.Red,
                        style = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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
            if (userState?.premium == true) {
                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(64.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colorStops = arrayOf(
                                    0f to Color(0xFFF74C54),
                                    0.75f to Color(0xFFFA8246),
                                    1f to Color(0xFFFEAC2F)
                                )
                            )
                        )
                        .height(40.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Premium",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 0.83.em,
                        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                    )
                }
            } else {
                Button(
                    onClick = { showUpgradeBottomSheet = true },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF6F6F6)),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Color(0xFF929295).copy(alpha = 0.5f),
                    thickness = 1.5.dp
                )

                // Email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF6F6F6)),
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
                            text = userState?.email
                                ?: "tennguoidung${userId.takeLast(8)}@gmail.com",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Color(0xFF929295).copy(alpha = 0.5f),
                    thickness = 1.5.dp
                )

                // Mật khẩu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF6F6F6)),
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
                .background(Color(0xFFF6F6F6))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
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