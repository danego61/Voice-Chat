package com.danego.voicechat.model

import androidx.compose.runtime.MutableState

sealed class MessageSendBottomBarState {

    data class Recording(val state: MutableState<MediaState>) : MessageSendBottomBarState()

    object Waiting : MessageSendBottomBarState()

    data class Recorded(val state: MutableState<MediaState>, val isRobotSound: Boolean = false) :
        MessageSendBottomBarState()

}
