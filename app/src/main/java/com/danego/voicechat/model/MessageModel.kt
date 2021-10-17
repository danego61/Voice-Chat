package com.danego.voicechat.model

data class MessageModel(
    val soundId: String,
    val isSend: Boolean,
    val date: String,
    val isNotified: Boolean? = null
)
