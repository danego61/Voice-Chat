package com.danego.voicechat.ui.screens.login

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.danego.voicechat.R
import com.danego.voicechat.utils.GlideUtils
import com.danego.voicechat.viewmodel.LoginScreenStatus
import com.danego.voicechat.viewmodel.LoginScreenViewModel

@ExperimentalAnimationApi
@Composable
fun LoginScreen(choiceUserImage: (MutableState<String>) -> Unit, loginToMain: () -> Unit) {

    val viewModel: LoginScreenViewModel = viewModel()

    if (viewModel.loginStatus.value == LoginScreenStatus.Finish)
        loginToMain()

    val enterAnimation = fadeIn(
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = 350
        )
    ) + slideInHorizontally(
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = 350
        )
    )
    val exitAnimation = fadeOut(animationSpec = tween(durationMillis = 300)) +
            slideOutHorizontally(animationSpec = tween(durationMillis = 300))

    BackHandler(
        enabled = viewModel.loginStatus.value != LoginScreenStatus.ChoiceLoginType
    ) {
        viewModel.changeStatus(LoginScreenStatus.ChoiceLoginType)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.primary
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            ShowAppImage(
                loginStatus = viewModel.loginStatus.value,
                userImage = viewModel.userImage.value
            )

            ShowCard(
                viewModel = viewModel,
                choiceUserImage = { choiceUserImage(viewModel.userImage) },
                enterAnimation = enterAnimation,
                exitAnimation = exitAnimation
            )

            ShowSnackbar(snackBarText = viewModel.snackbarMessage.value)
        }
    }

}

@ExperimentalAnimationApi
@Composable
private fun ColumnScope.ShowAppImage(loginStatus: LoginScreenStatus, userImage: String) {

    val state by derivedStateOf {
        when {
            loginStatus != LoginScreenStatus.UserDetails -> 0
            userImage == "" -> 1
            else -> 2
        }
    }

    AnimatedContent(
        targetState = state,
        transitionSpec = {
            slideInHorizontally({ height -> height }) + fadeIn() with slideOutHorizontally(
                { height -> -height }) + fadeOut() using (SizeTransform(clip = false))
        },
        modifier = Modifier.weight(0.5f)
    ) { newState ->
        when (newState) {
            0, 1 -> {
                val id = if (newState == 0)
                    R.drawable.app_image
                else
                    R.drawable.no_picture
                Image(
                    painter = painterResource(id = id),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize(0.25f)
                )
            }
            else -> {
                val image = GlideUtils.loadFromFile(
                    path = userImage,
                    diskCache = DiskCacheStrategy.NONE
                )
                image.value?.also {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "",
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(CircleShape)
                            .wrapContentSize()
                    )
                }
            }
        }
    }

}

@ExperimentalAnimationApi
@Composable
private fun ColumnScope.ShowCard(
    viewModel: LoginScreenViewModel,
    choiceUserImage: () -> Unit,
    enterAnimation: EnterTransition,
    exitAnimation: ExitTransition
) {
    Card(
        Modifier
            .weight(2f)
            .padding(8.dp),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            CardHeaderText(loginStatus = viewModel.loginStatus.value)
            Spacer(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .height(2.dp)
                    .fillMaxWidth()
            )
            Box(modifier = Modifier.fillMaxSize()) {
                ChoiceLoginTypeContent(
                    loginStatus = viewModel.loginStatus.value,
                    changeStatus = viewModel::changeStatus,
                    enterAnimation = enterAnimation,
                    exitAnimation = exitAnimation
                )
                LoginContent(
                    loginStatus = viewModel.loginStatus.value,
                    userEmail = viewModel.userEmail,
                    userPassword = viewModel.userPassword,
                    changeStatus = viewModel::changeStatus,
                    isLoginProcess = viewModel.isLoginProcess.value,
                    login = viewModel::login,
                    enterAnimation = enterAnimation,
                    exitAnimation = exitAnimation
                )
                UserDetailsContent(
                    viewModel = viewModel,
                    choiceUserImage = choiceUserImage,
                    enterAnimation = enterAnimation,
                    exitAnimation = exitAnimation
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun CardHeaderText(loginStatus: LoginScreenStatus) {
    AnimatedContent(
        targetState = loginStatus,
        transitionSpec = {
            slideInVertically({ height -> height }) + fadeIn() with slideOutVertically(
                { height -> -height }) + fadeOut() using (SizeTransform(clip = false))
        }
    ) { state ->
        Text(
            text = when (state) {
                LoginScreenStatus.ChoiceLoginType -> "Login Type"
                LoginScreenStatus.SingIn -> "Sing In"
                LoginScreenStatus.SingUp -> "Sing Up"
                LoginScreenStatus.UserDetails -> "User Details"
                LoginScreenStatus.Loading -> "Loading"
                LoginScreenStatus.Finish -> "Login"
            },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(all = 5.dp)
        )
    }
}

@ExperimentalAnimationApi
@Composable
private fun ShowSnackbar(
    snackBarText: String
) {
    AnimatedVisibility(visible = snackBarText != "") {
        Snackbar {
            Text(text = snackBarText)
        }
    }
}

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {

    val currentOnBack by rememberUpdatedState(onBack)

    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }

    SideEffect {
        backCallback.isEnabled = enabled
    }

    val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backDispatcher) {

        backDispatcher.addCallback(lifecycleOwner, backCallback)

        onDispose {
            backCallback.remove()
        }
    }
}