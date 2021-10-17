package com.danego.voicechat.ui.screens.message

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danego.voicechat.model.MediaCommands
import com.danego.voicechat.model.MediaState
import com.danego.voicechat.model.MessageModel
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalAnimationApi
@Composable
fun ShowMessages(
    isProcess: Boolean,
    messages: List<MessageModel>,
    getMessageStatus: (Int) -> MutableState<MediaState>,
    mediaCommand: (MediaCommands, Int) -> Unit,
    playerSeek: (Float, Int) -> Unit,
    modifier: Modifier = Modifier
) {

    if (messages.isEmpty()) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = "No Message!")
        }

    } else {

        LazyColumn(
            reverseLayout = true,
            modifier = modifier
        ) {

            items(messages.size) { index ->

                val message = messages[index]
                val state = getMessageStatus(index)

                DrawMessage(
                    message = message,
                    state = state.value,
                    playClick = {
                        if (state.value == MediaState.Stop)
                            mediaCommand(MediaCommands.Start, index)
                        else
                            mediaCommand(MediaCommands.Resume, index)
                    },
                    pauseClick = { mediaCommand(MediaCommands.Pause, index) },
                    seekTo = { playerSeek(it, index) }
                )

            }

        }

    }

    if (isProcess) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            CircularProgressIndicator()
        }
    }

}

@ExperimentalAnimationApi
@Composable
private fun DrawMessage(
    message: MessageModel,
    state: MediaState,
    playClick: () -> Unit,
    pauseClick: () -> Unit,
    seekTo: (Float) -> Unit
) {

    Row(modifier = Modifier.fillMaxWidth()) {

        if (message.isSend) {
            Spacer(modifier = Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.padding(10.dp),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = if (message.isSend)
                MaterialTheme.colors.secondary
            else
                MaterialTheme.colors.surface
        ) {

            Column(
                modifier = Modifier.padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val time = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss Z",
                    Locale.getDefault()
                ).let {
                    val date = it.parse(message.date)
                    it.applyPattern("dd.MM.yyyy HH:mm:ss")
                    it.format(date!!)
                }

                Text(text = time)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    PlayPauseButton(
                        isPauseEnabled = state is MediaState.Playing,
                        isLoading = state == MediaState.Loading,
                        isEnabled = true,
                        playClick = playClick,
                        pauseClick = pauseClick
                    )

                    ListeningStatus(
                        mediaState = if (state == MediaState.Stop) null else state,
                        isEnabled = true,
                        seekTo = seekTo
                    )

                }

            }

        }


    }

}