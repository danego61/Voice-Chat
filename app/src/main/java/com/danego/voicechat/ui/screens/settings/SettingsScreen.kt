package com.danego.voicechat.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danego.voicechat.R
import com.danego.voicechat.ui.screens.message.BackButton
import com.danego.voicechat.ui.screens.message.UserNameText
import com.danego.voicechat.utils.GlideUtils
import com.danego.voicechat.viewmodel.SettingScreenViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@ExperimentalAnimationApi
@Composable
fun SettingsScreen(
    userEmail: String,
    settingsToLogin: () -> Unit,
    settingsToMessageList: () -> Unit,
    settingsToMessage: () -> Unit
) {

    val viewModel: SettingScreenViewModel = viewModel()
    val isUserDetails = remember { userEmail != "?" }

    LaunchedEffect(key1 = true) {
        viewModel.init(userEmail)
    }

    Surface {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Header(
                userEmail = viewModel.userEmail.value ?: "",
                onBack = {

                    if (isUserDetails)
                        settingsToMessage()
                    else
                        settingsToMessageList()

                }
            )

            ShowAppImage(userEmail = viewModel.userEmail.value, modifier = Modifier.weight(.5f))

            Card(
                modifier = Modifier
                    .weight(2f)
                    .padding(8.dp),
                shape = RoundedCornerShape(32.dp)
            ) {

                UserDetailsContent(
                    viewModel = viewModel,
                    isUserDetails = isUserDetails,
                    settingsToLogin = settingsToLogin
                )
            }

        }

    }

}

@Composable
private fun Header(
    userEmail: String,
    onBack: (() -> Unit)
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colors.primary, shape = RoundedCornerShape(
                    bottomEnd = 20.dp,
                    bottomStart = 20.dp
                )
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        BackButton(onClick = onBack)

        Spacer(modifier = Modifier.weight(0.5f))

        UserNameText(userEmail = userEmail)

        Spacer(modifier = Modifier.weight(0.5f))

    }

}

@ExperimentalAnimationApi
@Composable
private fun ShowAppImage(
    userEmail: String?,
    modifier: Modifier = Modifier
) {

    if (userEmail != null) {
        val imageContent = GlideUtils.getUserImage(userEmail = userEmail).value

        if (imageContent == null) {
            Image(
                painter = painterResource(id = R.drawable.no_picture),
                contentDescription = "",
                modifier = modifier
                    .fillMaxSize(0.25f)
            )
        } else {
            Image(
                bitmap = imageContent.asImageBitmap(),
                contentDescription = "",
                modifier = modifier
                    .padding(10.dp)
                    .clip(CircleShape)
                    .wrapContentSize()
            )
        }
    }

}

@ExperimentalAnimationApi
@Composable
fun UserDetailsContent(
    viewModel: SettingScreenViewModel,
    isUserDetails: Boolean,
    settingsToLogin: () -> Unit
) {
    val isFormValid = derivedStateOf {
        viewModel.userFirstName.value.isNotEmpty() &&
                viewModel.userSurname.value.isNotEmpty() &&
                viewModel.userTelephone.value.isNotEmpty() &&
                viewModel.userTelephone.value.length == 10 &&
                viewModel.userTelephone.value.toLongOrNull() != null
    }
    val isEnabled = derivedStateOf {
        !viewModel.isRegistrationProcess.value
                && !viewModel.isProcessing.value
                && !isUserDetails
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
        val telephoneFocusRequester = remember {
            FocusRequester()
        }

        Spacer(modifier = Modifier.weight(1f))

        AddTextField(
            value = viewModel.userFirstName,
            labelText = "First Name",
            isEnabled = isEnabled.value,
            nextFocus = surnameFocusRequester
        )

        AddTextField(
            value = viewModel.userSurname,
            labelText = "Surname",
            isEnabled = isEnabled.value,
            focusRequester = surnameFocusRequester,
            nextFocus = telephoneFocusRequester
        )

        AddTextField(
            value = viewModel.userName,
            labelText = "User Name",
            isEnabled = false
        )

        AddTextField(
            value = viewModel.userTelephone,
            labelText = "Telephone - 5XXXXXXXXX",
            isEnabled = isEnabled.value,
            focusRequester = telephoneFocusRequester
        ) {
            if (isFormValid.value)
                viewModel.saveChanges()
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isUserDetails) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                AddButton(
                    isEnabled = !viewModel.isRegistrationProcess.value
                            && !viewModel.isProcessing.value,
                    text = "Sign Out"
                ) {
                    Firebase.auth.signOut()
                    settingsToLogin()
                }

                AddButton(
                    isEnabled = !viewModel.isRegistrationProcess.value
                            && !viewModel.isProcessing.value
                            && isFormValid.value,
                    text = "Save Changes"
                ) { viewModel.saveChanges() }

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
            if (value.value.isNotEmpty() && isEnabled) {
                IconButton(onClick = { value.value = "" }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "")
                }
            }
        }
    )
}

@Composable
fun AddButton(
    isEnabled: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        enabled = isEnabled
    ) {
        Text(text = text)
    }
}