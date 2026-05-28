package com.example.faiz_gear

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.faiz_gear.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Faiz_GearTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepBlack
                ) {
                    FaizPhoneApp()
                }
            }
        }
    }
}

@Composable
fun FaizPhoneApp(viewModel: FaizViewModel = viewModel()) {
    val context = LocalContext.current
    
    // Initialize high-performance sound engine once
    LaunchedEffect(Unit) {
        viewModel.initSounds(context)
    }
    
    FaizPhoneContent(viewModel)
}

@Composable
fun FaizPhoneContent(viewModel: FaizViewModel) {
    val context = LocalContext.current

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .drawWithContent {
                drawContent()
                // Subtle Grid Pattern
                val gridSize = 32.dp.toPx()
                for (x in 0..size.width.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.02f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..size.height.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.02f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        FaizDeviceShell(viewModel, vibrator)
    }
}

@Composable
fun FaizDeviceShell(viewModel: FaizViewModel, vibrator: Vibrator) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clip(FaizDeviceShape())
            .background(DarkMetal)
            .border(2.dp, TitaniumSilver, FaizDeviceShape())
            .padding(8.dp)
    ) {
        // Side Lights
        SideLights()

        // Inner Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(FaizDeviceShape())
                .background(DeepBlack)
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            StatusBar()
            Spacer(modifier = Modifier.height(8.dp))
            IndicatorRow()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display Section
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DisplayPanel(viewModel)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Keypad Section
            TacticalKeypad(viewModel, vibrator)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Action Section
            EnterButton(viewModel, vibrator)
        }
    }
}

@Composable
fun SideLights() {
    val infiniteTransition = rememberInfiniteTransition(label = "SideLights")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Left Light Strip
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(2.dp)
                .fillMaxHeight(0.6f)
                .background(NeonRed.copy(alpha = alpha))
                .shadow(elevation = 8.dp, color = NeonRed)
        )
        // Right Light Strip
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(2.dp)
                .fillMaxHeight(0.6f)
                .background(NeonRed.copy(alpha = alpha))
                .shadow(elevation = 8.dp, color = NeonRed)
        )
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatusItem("TAILSCALE", true)
        StatusItem("HEADSCALE", true)
        StatusItem("API", true)
    }
}

@Composable
fun StatusItem(label: String, active: Boolean) {
    Column {
        Text(
            text = label,
            color = if (active) TextMain else TextMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 3.dp)
                        .background(if (active && i < 3) CrimsonRed else Color.DarkGray)
                )
            }
        }
    }
}

@Composable
fun IndicatorRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IndicatorIcon(true) // Connection
        Spacer(modifier = Modifier.width(12.dp))
        IndicatorIcon(true) // Lock
        Spacer(modifier = Modifier.width(12.dp))
        IndicatorIcon(true) // Power
    }
}

@Composable
fun IndicatorIcon(active: Boolean) {
    Canvas(modifier = Modifier.size(14.dp)) {
        drawCircle(
            color = if (active) NeonRed else Color.Gray,
            radius = size.minDimension / 2f,
            style = Stroke(width = 2f)
        )
        if (active) {
            drawCircle(
                color = NeonRed.copy(alpha = 0.3f),
                radius = size.minDimension / 1.5f,
            )
        }
    }
}

@Composable
fun DisplayPanel(viewModel: FaizViewModel) {
    val uiState = getFaizUiState(viewModel)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(GlassBlack)
            .border(1.dp, CrimsonRed.copy(alpha = 0.3f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            FaizUiState.IDLE -> IdleDisplay()
            FaizUiState.INPUT -> InputDisplay(viewModel.inputCode)
            FaizUiState.VERIFYING -> VerifyingDisplay(viewModel.verificationProgress)
            FaizUiState.SUCCESS -> SuccessDisplay()
            FaizUiState.ERROR -> ErrorDisplay(viewModel.statusMessage)
        }
    }
}

@Composable
fun IdleDisplay() {
    val infiniteTransition = rememberInfiniteTransition(label = "Idle")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Logo Image
        Image(
            painter = painterResource(id = R.drawable.faiz_logo),
            contentDescription = "Faiz Logo",
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer(alpha = glowAlpha),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "READY",
            color = NeonRed.copy(alpha = glowAlpha),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "SYSTEM STANDBY",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InputDisplay(code: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "ENTER ACCESS CODE",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i ->
                val char = code.getOrNull(i)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .size(width = 45.dp, height = 60.dp)
                        .background(PanelBlack)
                        .border(1.dp, if (char.isNotEmpty()) NeonRed else Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        color = if (char == "5") CrimsonRed else TextMain,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "INPUT 3–6 DIGITS",
            color = TextMuted,
            fontSize = 8.sp
        )
    }
}

@Composable
fun VerifyingDisplay(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "Verifying")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "VERIFYING...",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "STANDING\nBY",
            color = CrimsonRed.copy(alpha = pulse),
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            modifier = Modifier.graphicsLayer {
                shadowElevation = 8.dp.toPx()
                spotShadowColor = CrimsonRed
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        // Segmented Progress Bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            val totalSegments = 12
            val activeSegments = (progress * totalSegments).toInt()
            repeat(totalSegments) { i ->
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 4.dp)
                        .background(if (i < activeSegments) CrimsonRed else Color.DarkGray.copy(alpha = 0.5f))
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "PLEASE WAIT",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SuccessDisplay() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "CONNECTION ESTABLISHED",
            color = CrimsonRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "COMPLETE",
            color = CrimsonRed,
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.shadow(12.dp, CrimsonRed)
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        // Decorative Circuit Pattern (Bottom)
        Canvas(modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(20.dp)) {
            val strokeWidth = 2.dp.toPx()
            val centerY = size.height / 2
            
            val path = Path().apply {
                moveTo(0f, centerY)
                lineTo(size.width * 0.4f, centerY)
                lineTo(size.width * 0.45f, centerY + 8.dp.toPx())
                lineTo(size.width * 0.55f, centerY - 8.dp.toPx())
                lineTo(size.width * 0.6f, centerY)
                lineTo(size.width, centerY)
            }
            
            drawPath(
                path = path,
                color = CrimsonRed,
                style = Stroke(width = strokeWidth)
            )
            
            drawCircle(CrimsonRed, radius = 3.dp.toPx(), center = Offset(0f, centerY))
            drawCircle(CrimsonRed, radius = 3.dp.toPx(), center = Offset(size.width, centerY))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Wake-on-LAN signal sent\nto Workstation",
            color = TextMain,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun ErrorDisplay(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "ACCESS DENIED",
            color = CrimsonRed,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "INVALID CODE",
            color = AmberYellow,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = TextMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TacticalKeypad(viewModel: FaizViewModel, vibrator: Vibrator) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
    ) {
        items(keys) { key ->
            KeypadButton(
                text = key,
                isLocked = viewModel.isKeypadLocked,
                isActive = key == "5" && viewModel.inputCode.contains("5"),
                onClick = {
                    triggerHapticFeedback(vibrator)
                    viewModel.onKeyPress(key)
                }
            )
        }
    }
}

@Composable
fun KeypadButton(text: String, isLocked: Boolean, isActive: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) DarkRed else MatteBlack)
            .border(
                1.dp,
                if (isActive) CrimsonRed else Color.Gray,
                RoundedCornerShape(4.dp)
            )
            .clickable(
                enabled = !isLocked,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) Color.White else TextMain,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EnterButton(viewModel: FaizViewModel, vibrator: Vibrator) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 8.dp)
            .clip(GenericShape { size, _ ->
                moveTo(0f, 12f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height - 12f)
                lineTo(0f, size.height)
                close()
            })
            .background(if (viewModel.isKeypadLocked) DarkRed.copy(alpha = 0.5f) else CrimsonRed)
            .clickable(enabled = !viewModel.isKeypadLocked) {
                triggerHapticFeedback(vibrator)
                viewModel.onEnter()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ENTER",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
    }
}

enum class FaizUiState {
    IDLE, INPUT, VERIFYING, SUCCESS, ERROR
}

@Composable
fun getFaizUiState(viewModel: FaizViewModel): FaizUiState {
    return when {
        viewModel.statusMessage == "COMPLETE" -> FaizUiState.SUCCESS
        viewModel.statusMessage == "STANDING BY..." -> FaizUiState.VERIFYING
        viewModel.statusMessage == "ERROR" || viewModel.statusMessage == "CONNECTION FAILED" -> FaizUiState.ERROR
        viewModel.inputCode.isNotEmpty() -> FaizUiState.INPUT
        else -> FaizUiState.IDLE
    }
}

fun FaizDeviceShape() = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 32.dp,
    bottomEnd = 32.dp
)

fun triggerHapticFeedback(vibrator: Vibrator) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08080A)
@Composable
fun FaizPhonePreview() {
    Faiz_GearTheme(darkTheme = true, dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF08080A)),
            contentAlignment = Alignment.Center
        ) {
            Text("FAIZPHONE UI PREVIEW", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
        }
    }
}

fun Modifier.shadow(
    elevation: androidx.compose.ui.unit.Dp,
    color: Color
) = this.drawWithContent {
    drawContent()
    drawRect(
        color = color.copy(alpha = 0.2f),
        topLeft = Offset(0f, 0f),
        size = size,
        style = Stroke(width = elevation.toPx())
    )
}
