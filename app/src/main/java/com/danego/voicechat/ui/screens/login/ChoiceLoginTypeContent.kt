package com.danego.voicechat.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danego.voicechat.viewmodel.LoginScreenStatus

@ExperimentalAnimationApi
@Composable
fun ChoiceLoginTypeContent(
    loginStatus: LoginScreenStatus,
    changeStatus: (LoginScreenStatus) -> Unit,
    enterAnimation: EnterTransition,
    exitAnimation: ExitTransition
) {

    val buttonEnabled = loginStatus == LoginScreenStatus.ChoiceLoginType

    AnimatedVisibility(
        visible = loginStatus == LoginScreenStatus.ChoiceLoginType ||
                loginStatus == LoginScreenStatus.Loading,
        enter = enterAnimation,
        exit = exitAnimation
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = { changeStatus(LoginScreenStatus.SingIn) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                enabled = buttonEnabled
            ) {
                Text(text = "Sing In", style = MaterialTheme.typography.body1)
            }

            Button(
                onClick = { changeStatus(LoginScreenStatus.SingUp) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = buttonEnabled
            ) {
                Text(text = "Sing Up", style = MaterialTheme.typography.body1)
            }
        }
    }

}