package com.danego.voicechat.model

sealed class MediaState(val duration: String, val progress: Float = 0f) {

    class Playing(duration: String, progress: Float = 0f) : MediaState(duration, progress)

    class Waiting(duration: String, progress: Float = 0f) : MediaState(duration, progress)

    object Stop : MediaState("0:00:000", 0f)

    object Loading : MediaState("0:00:000", 0f)

}
