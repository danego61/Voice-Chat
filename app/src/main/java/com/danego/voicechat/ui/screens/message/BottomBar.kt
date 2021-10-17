package com.danego.voicechat.ui.screens.message

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danego.voicechat.model.MediaState
import com.danego.voicechat.model.MessageSendBottomBarState

@ExperimentalAnimationApi
@Composable
fun ColumnScope.MessageSendBottomBar(
    messageSendBottomBarState: MessageSendBottomBarState,
    isSendProcess: Boolean,
    playClick: () -> Unit,
    pauseClick: () -> Unit,
    stopClick: () -> Unit,
    seekTo: (Float) -> Unit,
    deleteCLick: () -> Unit,
    sendClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(0.15f)
            .background(
                color = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        val isPauseEnabled = when (messageSendBottomBarState) {

            is MessageSendBottomBarState.Recorded ->
                messageSendBottomBarState.state.value is MediaState.Playing

            is MessageSendBottomBarState.Recording ->
                messageSendBottomBarState.state.value is MediaState.Playing

            MessageSendBottomBarState.Waiting -> false

        }

        PlayPauseButton(
            isPauseEnabled = isPauseEnabled,
            isLoading = false,
            isEnabled = !isSendProcess,
            pauseClick = pauseClick,
            playClick = playClick
        )

        ShowMessageSendStatus(
            messageSendBottomBarState = messageSendBottomBarState,
            isSendProcess = isSendProcess,
            stopClick = stopClick,
            seekTo = seekTo,
            deleteCLick = deleteCLick,
            sendClick = sendClick
        )

    }

}

@Composable
fun PlayPauseButton(
    isPauseEnabled: Boolean,
    isLoading: Boolean,
    isEnabled: Boolean,
    playClick: () -> Unit,
    pauseClick: () -> Unit
) {

    IconButton(
        onClick = { if (isPauseEnabled) pauseClick() else playClick() },
        modifier = Modifier.padding(5.dp),
        enabled = isEnabled && !isLoading
    ) {
        when {

            isLoading -> CircularProgressIndicator()

            isPauseEnabled ->
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause Button"
                )

            else ->
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Button")

        }
    }

}

@ExperimentalAnimationApi
@Composable
private fun ShowMessageSendStatus(
    messageSendBottomBarState: MessageSendBottomBarState,
    isSendProcess: Boolean,
    stopClick: () -> Unit,
    seekTo: (Float) -> Unit,
    deleteCLick: () -> Unit,
    sendClick: () -> Unit
) {

    AnimatedVisibility(
        visible = messageSendBottomBarState != MessageSendBottomBarState.Waiting,
        enter = expandHorizontally(animationSpec = tween(delayMillis = 300)),
        exit = shrinkHorizontally()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            val recordingState =
                messageSendBottomBarState as? MessageSendBottomBarState.Recording
            val recordedState =
                messageSendBottomBarState as? MessageSendBottomBarState.Recorded

            Text(
                text = recordedState?.state?.value?.duration
                    ?: recordingState?.state?.value?.duration ?: "",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(end = 5.dp)
            )

            ListeningStatus(
                mediaState = recordedState?.state?.value,
                isEnabled = !isSendProcess,
                seekTo = seekTo
            )

            StopDeleteButton(
                isStop = recordedState?.state?.value !is MediaState.Stop,
                isEnabled = !isSendProcess,
                stopClick = stopClick,
                deleteCLick = deleteCLick
            )

            SendButton(
                isEnabled = recordedState != null && !isSendProcess,
                sendClick = sendClick
            )

        }

    }

}

@ExperimentalAnimationApi
@Composable
fun RowScope.ListeningStatus(
    mediaState: MediaState?,
    isEnabled: Boolean,
    seekTo: (Float) -> Unit
) {

    AnimatedVisibility(
        visible = mediaState != null,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
        modifier = Modifier.weight(1f)
    ) {

        val slideValue = remember { mutableStateOf(-1f) }
        val sliderState by derivedStateOf {
            if (slideValue.value >= 0)
                slideValue.value
            else
                mediaState?.progress ?: 0f
        }

        Slider(
            value = sliderState,
            onValueChange = { slideValue.value = it },
            onValueChangeFinished = {
                seekTo(slideValue.value)
                slideValue.value = -1f
            },
            enabled = isEnabled
        )

    }

}

@ExperimentalAnimationApi
@Composable
private fun StopDeleteButton(
    isStop: Boolean,
    isEnabled: Boolean,
    stopClick: () -> Unit,
    deleteCLick: () -> Unit
) {
    IconButton(
        onClick = { if (isStop) stopClick() else deleteCLick() },
        enabled = isEnabled
    ) {
        AnimatedContent(
            targetState = isStop,
            transitionSpec = {
                slideInVertically({ height -> height }) + fadeIn() with slideOutVertically(
                    { height -> -height }) + fadeOut() using (SizeTransform(clip = false))
            }
        ) {
            if (isStop)
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Button"
                )
            else
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Button",
                    tint = Color.Red
                )
        }

    }
}

@Composable
private fun SendButton(
    isEnabled: Boolean,
    sendClick: () -> Unit
) {
    IconButton(
        onClick = sendClick,
        enabled = isEnabled
    ) {
        Icon(imageVector = Icons.Default.Send, contentDescription = "Stop Button")
    }
}