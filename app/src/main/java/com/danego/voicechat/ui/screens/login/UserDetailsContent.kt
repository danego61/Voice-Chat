package com.danego.voicechat.ui.screens.login

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.danego.voicechat.viewmodel.LoginScreenStatus
import com.danego.voicechat.viewmodel.LoginScreenViewModel

@ExperimentalAnimationApi
@Composable
fun UserDetailsContent(
    viewModel: LoginScreenViewModel,
    choiceUserImage: () -> Unit,
    enterAnimation: EnterTransition,
    exitAnimation: ExitTransition
) {
    AnimatedVisibility(
        visible = viewModel.loginStatus.value == LoginScreenStatus.UserDetails,
        enter = enterAnimation,
        exit = exitAnimation
    ) {

        val isFormValid = derivedStateOf {
            viewModel.userFirstName.value.isNotEmpty() &&
                    viewModel.userSurname.value.isNotEmpty() &&
                    viewModel.userName.value.isNotEmpty() &&
                    viewModel.userTelephone.value.isNotEmpty() &&
                    viewModel.userTelephone.value.length == 10 &&
                    viewModel.userTelephone.value.toLongOrNull() != null
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {

            val surnameFocusRequester = remember {
                FocusRequester()
            }
            val userNameFocusRequester = remember {
                FocusRequester()
            }
            val telephoneFocusRequester = remember {
                FocusRequester()
            }

            Spacer(modifier = Modifier.weight(1f))

            AddTextField(
                value = viewModel.userFirstName,
                labelText = "First Name",
                isEnabled = !viewModel.isRegistrationProcess.value,
                nextFocus = surnameFocusRequester
            )

            AddTextField(
                value = viewModel.userSurname,
                labelText = "Surname",
                isEnabled = !viewModel.isRegistrationProcess.value,
                focusRequester = surnameFocusRequester,
                nextFocus = userNameFocusRequester
            )

            AddTextField(
                value = viewModel.userName,
                labelText = "User Name",
                isEnabled = !viewModel.isRegistrationProcess.value,
                focusRequester = userNameFocusRequester,
                nextFocus = telephoneFocusRequester
            )

            AddTextField(
                value = viewModel.userTelephone,
                labelText = "Telephone - 5XXXXXXXXX",
                isEnabled = !viewModel.isRegistrationProcess.value,
                focusRequester = telephoneFocusRequester
            ) {
                if (isFormValid.value)
                    viewModel.completeRegistration()
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                SignOutButton(
                    isRegistrationProcess = viewModel.isRegistrationProcess.value,
                    changeStatus = viewModel::changeStatus
                )

                ChangeUserImageButton(
                    isRegistrationProcess = viewModel.isRegistrationProcess.value,
                    choiceUserImage = choiceUserImage
                )

                CompleteRegistrationButton(
                    isFormValid = isFormValid.value,
                    isRegistrationProcess = viewModel.isRegistrationProcess.value,
                    registrationComplete = viewModel::completeRegistration
                )
            }
        }

    }
}

@Composable
private fun AddTextField(
    value: MutableState<String>,
    labelText: String,
    isEnabled: Boolean,
    nextFocus: FocusRequester? = null,
    focusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    var modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth()

    if (focusRequester != null)
        modifier = modifier.focusRequester(focusRequester)

    OutlinedTextField(
        value = value.value,
        onValueChange = { value.value = it },
        label = { Text(text = labelText) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (onDone == null)
                    nextFocus?.requestFocus()
                else
                    onDone()
            }
        ),
        modifier = modifier,
        enabled = isEnabled,
        singleLine = true,
        trailingIcon = {
            if (value.value.isNotEmpty()) {
                IconButton(onClick = { value.value = "" }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "")
                }
            }
        }
    )
}

@Composable
fun SignOutButton(
    isRegistrationProcess: Boolean,
    changeStatus: (LoginScreenStatus) -> Unit
) {
    Button(
        onClick = { changeStatus(LoginScreenStatus.ChoiceLoginType) },
        shape = RoundedCornerShape(16.dp),
        enabled = !isRegistrationProcess
    ) {
        Text(text = "Sign Out")
    }
}

@Composable
fun ChangeUserImageButton(
    isRegistrationProcess: Boolean,
    choiceUserImage: () -> Unit
) {
    Button(
        onClick = choiceUserImage,
        shape = RoundedCornerShape(16.dp),
        enabled = !isRegistrationProcess
    ) {
        Text(text = "Change Image")
    }
}

@Composable
fun CompleteRegistrationButton(
    isFormValid: Boolean,
    isRegistrationProcess: Boolean,
    registrationComplete: () -> Unit
) {
    Button(
        onClick = registrationComplete,
        shape = RoundedCornerShape(16.dp),
        enabled = isFormValid && !isRegistrationProcess
    ) {
        if (isRegistrationProcess) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text(
                text = "Register"
            )
        }
    }
}