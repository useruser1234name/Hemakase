package com.example.hemakase.ui.theme

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.hemakase.R
import com.example.hemakase.viewmodel.RegisterViewModel
import java.io.File
import java.io.FileOutputStream


@Preview(showBackground = true)
@Composable
fun thirdScreenPreview() {
    CameraScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNextClick: () -> Unit = {},
    registerViewModel: RegisterViewModel = viewModel()
) {

    val context = LocalContext.current

    // 다이얼로그 표시 여부 상태
    var showDialog by remember { mutableStateOf(false) }

    // 이미지 URI 상태
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리에서 이미지 선택 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        registerViewModel.tempProfileUri = uri
    }

    // 카메라 촬영 후 비트맵을 URI로 저장
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveBitmapToCache(context, bitmap)
            imageUri = uri
            registerViewModel.tempProfileUri = uri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(
                context,
                "카메라 권한이 거부되었습니다. 사진 촬영을 위해 권한을 허용해주세요.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        TopBarWithBackArrow()

        Spacer(modifier = Modifier.height(32.dp))

        // 상단 단계 진행 표시 줄
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val centerY = size.height / 2
                        val startX = 24.dp.toPx()
                        val endX = size.width - 24.dp.toPx()
                        drawLine(
                            color = Color.Gray,
                            start = Offset(startX, centerY),
                            end = Offset(endX, centerY),
                            strokeWidth = strokeWidth
                        )
                    },
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    R.drawable.first,
                    R.drawable.choicesecond,
                    R.drawable.third,
                    R.drawable.fourth,
                    R.drawable.fifth
                ).forEachIndexed { index, resId ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF4F4F4))
                            .border(
                                width = if (index == 1) 1.dp else 0.dp,
                                color = if (index == 1) Color.Black else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Step ${index + 1}",
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 사진등록 타이틀
        Text(
            text = "사진등록",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(33.dp))

        // 선택 영역: 점선 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(5.dp))
                .clickable { showDialog = true }
                .drawBehind {
                    val dashStyle = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawRoundRect(
                        color = Color.LightGray,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = dashStyle
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.picture_plus),
                    contentDescription = "Plus icon",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Choose a file",
                    color = Color.Black
                )
            }
        }

        // 선택된 이미지가 있을 경우 미리보기 표시
        imageUri?.let {
            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = rememberAsyncImagePainter(model = it),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next 버튼
        Button(
            onClick = { onNextClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(60.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(
                text = "Next",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // 사진 등록 방식 선택 Bottom Sheet 다이얼로그
    if (showDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = Color(0xFF1C1C1E), // 시크한 어두운 배경
            modifier = Modifier.heightIn(min = 200.dp, max = 400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    "사진 등록 방식 선택",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Divider(color = Color.Gray)

                Text(
                    "갤러리에서 선택",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            galleryLauncher.launch("image/*")
                            showDialog = false
                        }
                )

                Text(
                    "카메라로 촬영",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                )
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                            showDialog = false
                        }
                )

            }
        }
    }
}

// 비트맵을 임시 파일로 저장하고 URI 반환
fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val filename = "${System.currentTimeMillis()}.jpg"
    val file = File(context.cacheDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
