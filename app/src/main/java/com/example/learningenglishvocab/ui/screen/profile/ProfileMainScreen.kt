package com.example.learningenglishvocab.ui.screen.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.learningenglishvocab.R
import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileMainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val userRepository = UserRepository()
    val coroutineScope = rememberCoroutineScope()

    // State để quản lý dữ liệu người dùng và trạng thái tải
    var userState by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var isEditingUsername by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf(userState?.username ?: "E-App User") }

    var showUpgradeBottomSheet by remember { mutableStateOf(false) }

    // Load dữ liệu người dùng khi Composable được tạo
    LaunchedEffect(Unit) {
        val userId = authViewModel.getCurrentUserId() ?: return@LaunchedEffect
        isLoading = true
        userState = userRepository.getUser(userId)
        editedUsername = userState?.username ?: "E-App User"
        isLoading = false
    }

    // Chuyển đổi avatar thành Bitmap để hiển thị
    val avatarBitmap = userState?.avatar?.let { base64String ->
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    // Trình chọn ảnh
    val context = LocalContext.current
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                coroutineScope.launch {
                    try {
                        // Chuyển ảnh thành Base64
                        val inputStream = context.contentResolver.openInputStream(it)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        val base64String =
                            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                        // Cập nhật avatar trong Firestore
                        val userId = authViewModel.getCurrentUserId() ?: return@launch
                        userState?.let { currentUser ->
                            val updatedUser = currentUser.copy(avatar = base64String)
                            val success = userRepository.updateUser(updatedUser)
                            if (success) {
                                userState = updatedUser // Cập nhật giao diện
                            }
                        }
                    } catch (e: Exception) {
                        // Xử lý lỗi (hiển thị Toast hoặc Snackbar nếu cần)
                        Log.e("ProfileMainScreen", "Error updating avatar: ${e.message}", e)
                    }
                }
            }
        }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        showBottomSheet = false
                        showImageDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xem ảnh", color = Color.Black)
                }
                TextButton(
                    onClick = {
                        showBottomSheet = false
                        pickImageLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chọn ảnh mới", color = Color.Black)
                }
            }
        }
    }

    // Dialog xem ảnh
    if (showImageDialog) {
        if (avatarBitmap == null) {
            showImageDialog = false
        } else {
            Dialog(
                onDismissRequest = { showImageDialog = false },
                properties = DialogProperties(
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(
                            onClick = { showImageDialog = false },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = avatarBitmap,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit // Giữ tỷ lệ ảnh
                    )
                }
            }
        }
    }

    val nextYearDate by remember {
        mutableStateOf(
            LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("d 'tháng' M, yyyy"))
        )
    }

//    Bottom sheet nâng cấp
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
                // Tiêu đề và nút đóng
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

                // Phần giá tiền và giảm giá
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
                        // Box giảm giá -30%
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
                        // Nội dung giá tiền
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
                                    text = "599.000₫ ≈ $23",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "/ năm",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
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

                // Phần "Cách hoạt động"
                Text(
                    text = "Cách hoạt động của gói đăng ký hàng năm",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(x = 40.dp),
                    color = Color.Black,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                )

                // Hôm nay: Truy cập tức thì
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

                // Dòng phân cách
                Row(
                    modifier = Modifier.fillMaxWidth().offset(x = 4.dp, y = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Divider(
                        color = Color.Black,
                        modifier = Modifier
                            .width(22.dp)
                            .rotate(-90f)
                    )
                }

                // Ngày gia hạn
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
                            text = "Gói đăng ký của bạn được gia hạn thêm một năm trừ khi bạn huỷ gói cước trước ngày này",
                            color = Color(0xFF6D6A6A),
                            style = TextStyle(fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }
                }

                // Nút nâng cấp
                Button(
                    onClick = { showUpgradeBottomSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
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
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xfff2f1eb))
            .padding(start = 23.dp, end = 23.dp)
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
                modifier = Modifier.size(80.dp)
            )
        }
        Button(
            onClick = { showUpgradeBottomSheet = true },
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .offset(x = 130.dp, y = 25.dp)
                .clip(shape = RoundedCornerShape(64.dp))
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

        // Profile
        Box(modifier = modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                userState == null -> {
                    Text(
                        text = "Không thể tải thông tin người dùng",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Vùng chứa avatar
                        Box(
                            modifier = Modifier
                                .offset(y = 80.dp)
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable { showBottomSheet = true }
                        ) {
                            Image(
                                bitmap = avatarBitmap
                                    ?: ImageBitmap.imageResource(R.drawable.default_avt),
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        // Vùng chứa username
                        Text(
                            text = userState?.username ?: "E-App User",
                            color = Color.Black,
                            lineHeight = 1.18.em,
                            fontWeight = FontWeight.SemiBold,
                            style = TextStyle(fontSize = 17.sp),
                            modifier = Modifier.offset(y = 100.dp)
                        )
                    }
                }
            }
        }

        // Manage Account
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .offset(y = 270.dp)
                .clip(RoundedCornerShape(15.dp))
                .border(
                    border = BorderStroke(1.dp, Color(0xff929295).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(15.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xfff6f6f6),
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { navController.navigate("accountManagement") },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_setting),
                        contentDescription = "setting",
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "Quản lý tài tài khoản",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 1.43.em
                        )
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Next",
                    alpha = 0.5f,
                    modifier = Modifier
                        .size(15.dp)
                        .rotate(180f)
                )
            }
        }

        // Streak
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 350.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thành tựu",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Xem thêm",
                fontSize = 14.sp,
                color = Color(0xFF555555),
                modifier = Modifier
                    .clickable { navController.navigate("yearlyOverview") }
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF6F6F6))
                .border(
                    border = BorderStroke(1.dp, Color(0xFF929295).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakSection()
            CalendarSection()
        }
    }
}

@Composable
fun StreakSection(modifier: Modifier = Modifier) {
    val studyLogRepository = StudyLogRepository()
    val authViewModel = AuthViewModel()
    val userId = authViewModel.getCurrentUserId() ?: return
    var streak by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        streak = studyLogRepository.calculateStreak(userId)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "\uD83D\uDD25",
                    fontSize = 28.sp
                )
                Text(
                    text = streak.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8C00)
                )
            }
            Text(
                text = "Tiếp tục học để giữ chuỗi của bạn!",
                fontSize = 15.sp,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarSection(modifier: Modifier = Modifier) {
    val currentDate = java.time.LocalDate.now()
    val currentYear = currentDate.year
    val currentMonth = currentDate.monthValue

    // Tạo LocalDate để tính toán lịch
    val localDate = java.time.LocalDate.of(currentYear, currentMonth, 1)
    val daysInMonth = localDate.lengthOfMonth() // Số ngày trong tháng (30 cho tháng 4)
    val firstDayOfMonth =
        localDate.dayOfWeek.value % 7 // Thứ của ngày 1 (0 = CN, 1 = T2, ..., 6 = T7)

    // Danh sách các ngày (1 đến daysInMonth)
    val days = (1..daysInMonth).toList()

    val authViewModel = AuthViewModel()
    val studyLogRepository = StudyLogRepository()
    val userId = authViewModel.getCurrentUserId() ?: return
    var studiedDays by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Lấy danh sách ngày đã học
        studiedDays = studyLogRepository.getStudiedDays(userId)
    }

    DisposableEffect(Unit) {
        onDispose {
            studiedDays = emptyList() // Giải phóng bộ nhớ
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hàng tiêu đề: CN, T2, ..., T7
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Lưới hiển thị các ngày
        LazyVerticalGrid(
            columns = GridCells.Fixed(7), // 7 cột cho 7 ngày trong tuần
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Thêm các ô trống trước ngày 1 (dựa trên firstDayOfMonth)
            items(firstDayOfMonth) {
                Box(modifier = Modifier.size(40.dp))
            }

            // Hiển thị các ngày
            items(days) { day ->
                val formattedDay = "$currentYear-${currentMonth.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                Log.e("study date", formattedDay)
                val isStudied = formattedDay in studiedDays
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isStudied) Color(0xFFFF8C00) else Color(0xFFF6F6F6)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontSize = 15.sp,
                        color = if (isStudied) Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
