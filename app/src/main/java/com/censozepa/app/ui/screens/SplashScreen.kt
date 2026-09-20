package com.censozepa.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreenContent(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    var countdownText by remember { mutableStateOf("3") }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 600)
        )
        
        delay(400)
        countdownText = "2"
        delay(400)
        countdownText = "1"
        delay(400)
        countdownText = "¡Vamos allá!"
        delay(500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E7D32),
                        Color(0xFF1B4D3E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            Text(
                text = "ZEPA",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Censo de Aves · Red Natura 2000",
                color = Color(0xFFC8E6C9),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = countdownText,
                color = Color(0xFF81C784),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
