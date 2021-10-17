package com.danego.voicechat.ui.screens.login

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.danego.voicechat.viewmodel.LoginScreenStatus

@ExperimentalAnimationApi
@Composable
fun LoginContent(
    loginStatus: LoginScreenStatus,
    userEmail: MutableState<String>,
    userPassword: MutableState<String>,
    changeStatus: (LoginScreenStatus) -> Unit,
    isLoginProcess: Boolean,
    login: () -> Unit,
    enterAnimation: EnterTransition,
    exitAnimation: ExitTransition
) {

    val isFormValid by derivedStateOf {
        Patterns.EMAIL_ADDRESS.matcher(userEmail.value).matches() &&
                userPassword.value.length > 7
    }
    val passwordFocusRequest = remember { FocusRequester() }

    AnimatedVisibility(
        visible = loginStatus == LoginScreenStatus.SingUp ||
                loginStatus == LoginScreenStatus.SingIn,
        modifier = Modifier.fillMaxSize(),
        enter = enterAnimation,
        exit = exitAnimation
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
        ) {
            Spacer(modifier = Modifier.weight(1f))
            EmailTextField(
                userEmail = userEmail,
                isLoginProcess = isLoginProcess,
                passwordFocusRequest = passwordFocusRequest
            )
            PasswordTextField(
                userPassword = userPassword,
                isLoginProcess = isLoginProcess,
                isFormValid = isFormValid,
                passwordFocusRequest = passwordFocusRequest,
                login = login
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth()) {
                BackButton(
                    isLoginProcess = isLoginProcess,
                    changeStatus = changeStatus
                )
                Spacer(modifier = Modifier.weight(0.3F))
                LoginButton(
                    loginStatus = loginStatus,
                    isFormValid = isFormValid,
                    isLoginProcess = isLoginProcess,
                    login = login
                )

            }
        }

    }

}

@Composable
private fun EmailTextField(
    userEmail: MutableState<String>,
    isLoginProcess: Boolean,
    passwordFocusRequest: FocusRequester
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        value = userEmail.value,
        enabled = !isLoginProcess,
        onValueChange = { userEmail.value = it },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { passwordFocusRequest.requestFocus() }
        ),
        label = { Text(text = "Email") },
        singleLine = true,
        trailingIcon = {
            if (userEmail.value.isNotEmpty()) {
                IconButton(onClick = { userEmail.value = "" }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "")
                }
            }
        }
    )
}

@Composable
private fun PasswordTextField(
    userPassword: MutableState<String>,
    isLoginProcess: Boolean,
    isFormValid: Boolean,
    passwordFocusRequest: FocusRequester,
    login: () -> Unit
) {

    var isPasswordVisible by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
            .focusRequester(passwordFocusRequest),
        value = userPassword.value,
        enabled = !isLoginProcess,
        onValueChange = { userPassword.value = it },
        label = { Text(text = "Password") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (isFormValid) {
                    login()
                }
            }
        ),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Password Toggle"
                )
            }
        }
    )
}

@Composable
fun RowScope.BackButton(
    isLoginProcess: Boolean,
    changeStatus: (LoginScreenStatus) -> Unit
) {
    Button(
        onClick = { changeStatus(LoginScreenStatus.ChoiceLoginType) },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.weight(0.35F),
        enabled = !isLoginProcess
    ) {
        Text(text = "Back")
    }
}

@Composable
fun RowScope.LoginButton(
    loginStatus: LoginScreenStatus,
    isFormValid: Boolean,
    isLoginProcess: Boolean,
    login: () -> Unit
) {
    Button(
        onClick = login,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.weight(0.35F),
        enabled = isFormValid && !isLoginProcess
    ) {
        if (isLoginProcess) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text(
                text = if (loginStatus == LoginScreenStatus.SingIn)
                    "Sing In"
                else
                    "Sing Up"
            )
        }
    }
}