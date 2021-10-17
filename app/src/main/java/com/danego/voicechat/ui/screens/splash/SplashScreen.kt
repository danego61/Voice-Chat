package com.danego.voicechat.ui.screens.splash

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danego.voicechat.R
import com.danego.voicechat.utils.FirebaseAuthUtils
import com.danego.voicechat.utils.FirebaseFirestoreUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    splashToLogin: () -> Unit,
    splashToMain: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var startAnimation by remember { mutableStateOf(false) }
        val offsetState by animateDpAsState(
            targetValue = if (startAnimation) 0.dp else 100.dp,
            animationSpec = tween(
                durationMillis = 1000
            )
        )
        val alphaState by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(
                durationMillis = 1500
            )
        )

        LaunchedEffect(key1 = true) {
            startAnimation = true
            val isLogged = async {
                FirebaseAuthUtils.checkIsLogged()
            }
            delay(3000)
            if (isLogged.await()) {
                FirebaseFirestoreUtils.isUserRegistrationComplete {
                    if (it)
                        splashToMain()
                    else
                        splashToLogin()
                }
            } else
                splashToLogin()
        }

        SplashImage(
            offset = offsetState,
            alpha = alphaState
        )
    }

}

@Composable
private fun SplashImage(offset: Dp, alpha: Float) {
    Image(
        modifier = Modifier
            .offset(y = offset)
            .alpha(alpha)
            .size(200.dp),
        painter = painterResource(id = R.drawable.app_image),
        contentDescription = "Splash Image"
    )
}