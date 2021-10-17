package com.danego.voicechat.ui.screens.messagelist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danego.voicechat.R
import com.danego.voicechat.utils.GlideUtils
import com.danego.voicechat.viewmodel.MessageListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@ExperimentalAnimationApi
@Composable
fun MessageListScreen(
    messageListToMessage: (String) -> Unit,
    messageListToSearch: () -> Unit,
    messageListToSettings: () -> Unit
) {

    val viewModel: MessageListViewModel = viewModel()

    LaunchedEffect(key1 = true) {

        while (isActive) {
            delay(3000)
            viewModel.updateMessages()
        }

    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Header(
                userName = viewModel.userDetails.value.userName,
                isLoaded = !viewModel.isLoading.value,
                messageListToSettings = messageListToSettings
            )

            ShowMessageUsers(
                messages = viewModel.users.value,
                isLoaded = !viewModel.isLoading.value,
                messageListToMessage = messageListToMessage
            )

        }

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.padding(10.dp)
        ) {
            FloatingActionButton(onClick = messageListToSearch) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = ""
                )
            }
        }

    }

}

@Composable
private fun Header(
    userName: String,
    isLoaded: Boolean,
    messageListToSettings: () -> Unit
) {

    Box(
        modifier = Modifier.background(
            color = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.h5,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(.9f)
            )
            Spacer(modifier = Modifier.weight(.1f))
            IconButton(onClick = messageListToSettings, enabled = isLoaded) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "")
            }
        }
    }

}

@ExperimentalAnimationApi
@Composable
private fun ShowMessageUsers(
    messages: List<String>,
    isLoaded: Boolean,
    messageListToMessage: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoaded) {
            if (messages.isEmpty()) {
                Text(text = "No Message")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(count = messages.size) { messageId ->

                        val state = remember {
                            MutableTransitionState(false).apply {
                                targetState = true
                            }
                        }

                        AnimatedVisibility(
                            visibleState = state,
                            enter = expandVertically(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = 300
                                )
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(
                                    durationMillis = 300
                                )
                            )
                        ) {
                            MessageUserCard(
                                userName = messages[messageId],
                                messageListToMessage = messageListToMessage
                            )
                        }


                    }
                }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun MessageUserCard(
    userName: String,
    messageListToMessage: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { messageListToMessage(userName) },
    ) {

        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ShowUserImage(
                userName = userName,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(65.dp),
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = userName,
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.weight(0.5f))

        }

    }
}

@Composable
private fun ShowUserImage(
    userName: String,
    modifier: Modifier = Modifier
) {

    val userImage by GlideUtils.getUserImage(userEmail = userName)

    if (userImage == null) {
        Image(
            painter = painterResource(id = R.drawable.no_picture),
            contentDescription = "User Image",
            modifier = modifier
        )
    } else {
        Image(
            bitmap = userImage!!.asImageBitmap(),
            contentDescription = "User Image",
            modifier = modifier
                .clip(CircleShape)
        )
    }

}