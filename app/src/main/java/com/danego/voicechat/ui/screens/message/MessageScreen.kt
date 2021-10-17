package com.danego.voicechat.ui.screens.message

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danego.voicechat.R
import com.danego.voicechat.model.MediaCommands
import com.danego.voicechat.model.MediaState
import com.danego.voicechat.model.MessageSendBottomBarState
import com.danego.voicechat.utils.GlideUtils
import com.danego.voicechat.viewmodel.MessageScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@ExperimentalAnimationApi
@Composable
fun MessageScreen(
    userEmail: String,
    messageToMessageList: () -> Unit,
    messageToSettings: (String) -> Unit
) {

    val viewModel: MessageScreenViewModel = viewModel()

    LaunchedEffect(key1 = true) {

        viewModel.init(userEmail)

        while (isActive) {
            delay(3000)
            viewModel.updateMessages(false)
        }

    }

    Surface(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            Header(
                userEmail = userEmail,
                onClick = messageToMessageList,
                messageToSettings = messageToSettings
            )

            ShowMessages(
                isProcess = viewModel.isLoading.value,
                messages = viewModel.messages.value,
                getMessageStatus = viewModel::getMessageStatus,
                mediaCommand = viewModel::playerCommand,
                playerSeek = viewModel::playerSeek,
                modifier = Modifier.weight(0.85f)
            )

            MessageSendBottomBar(
                messageSendBottomBarState = viewModel.messageSendBottomBarState.value,
                isSendProcess = viewModel.isSendProcessing.value,
                pauseClick = {
                    if (viewModel.messageSendBottomBarState.value is MessageSendBottomBarState.Recorded)
                        viewModel.playerCommand(MediaCommands.Pause)
                    else if (viewModel.messageSendBottomBarState.value is MessageSendBottomBarState.Recording)
                        viewModel.recorderCommand(MediaCommands.Pause)
                },
                playClick = {
                    when (val state = viewModel.messageSendBottomBarState.value) {
                        is MessageSendBottomBarState.Recorded -> {
                            if (state.state.value == MediaState.Stop)
                                viewModel.playerCommand(MediaCommands.Start)
                            else
                                viewModel.playerCommand(MediaCommands.Resume)
                        }
                        is MessageSendBottomBarState.Recording -> {
                            if (state.state.value == MediaState.Stop)
                                viewModel.recorderCommand(MediaCommands.Start)
                            else
                                viewModel.recorderCommand(MediaCommands.Resume)
                        }
                        MessageSendBottomBarState.Waiting ->
                            viewModel.recorderCommand(MediaCommands.Start)
                    }
                },
                stopClick = {
                    if (viewModel.messageSendBottomBarState.value is MessageSendBottomBarState.Recorded)
                        viewModel.playerCommand(MediaCommands.Stop)
                    else if (viewModel.messageSendBottomBarState.value is MessageSendBottomBarState.Recording)
                        viewModel.recorderCommand(MediaCommands.Stop)
                },
                seekTo = { viewModel.playerSeek(it) },
                deleteCLick = {
                    if (viewModel.messageSendBottomBarState.value is MessageSendBottomBarState.Recorded)
                        viewModel.messageSendBottomBarState.value =
                            MessageSendBottomBarState.Waiting
                },
                sendClick = viewModel::sendMessage
            )

        }

    }

}

@Composable
private fun Header(
    userEmail: String,
    onClick: () -> Unit,
    messageToSettings: (String) -> Unit
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
            .padding(10.dp)
            .clickable { messageToSettings(userEmail) },
        verticalAlignment = Alignment.CenterVertically
    ) {

        BackButton(onClick = onClick)

        ShowUserImage(userEmail = userEmail)

        Spacer(modifier = Modifier.weight(0.5f))

        UserNameText(userEmail = userEmail)

        Spacer(modifier = Modifier.weight(0.5f))

    }

}

@Composable
fun BackButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.padding(5.dp)) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back"
        )
    }
}

@Composable
private fun ShowUserImage(userEmail: String) {

    val userImage by GlideUtils.getUserImage(userEmail = userEmail)

    if (userImage == null) {
        Image(
            painter = painterResource(id = R.drawable.no_picture),
            contentDescription = "User Image",
            modifier = Modifier
                .padding(start = 5.dp)
                .size(50.dp)
        )
    } else {
        Image(
            bitmap = userImage!!.asImageBitmap(),
            contentDescription = "User Image",
            modifier = Modifier
                .padding(start = 5.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
    }

}

@Composable
fun UserNameText(userEmail: String) {

    Text(text = userEmail, style = MaterialTheme.typography.h6)

}