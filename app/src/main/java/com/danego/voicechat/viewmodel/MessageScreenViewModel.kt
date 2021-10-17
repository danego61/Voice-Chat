package com.danego.voicechat.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.danego.voicechat.model.MediaCommands
import com.danego.voicechat.model.MediaState
import com.danego.voicechat.model.MessageModel
import com.danego.voicechat.model.MessageSendBottomBarState
import com.danego.voicechat.utils.FirebaseFirestoreUtils
import com.danego.voicechat.utils.FirebaseStorageUtils
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MessageScreenViewModel(app: Application) : AndroidViewModel(app) {

    private var processingMedia: Pair<Int, MutableState<MediaState>>? = null
    private var userEmail = ""
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val time = Calendar.getInstance()
    private var durationJob: Job? = null
    private val messagesStatus = hashMapOf<Int, MutableState<MediaState>>()
    val messageSendBottomBarState: MutableState<MessageSendBottomBarState> =
        mutableStateOf(MessageSendBottomBarState.Waiting)
    val messages = mutableStateOf(listOf<MessageModel>())
    val isLoading = mutableStateOf(false)
    val isSendProcessing = mutableStateOf(false)

    fun init(userEmail: String) {

        this.userEmail = userEmail
        updateMessages(true)

    }

    fun updateMessages(withLoading: Boolean) {

        if (!isLoading.value) {

            if (withLoading)
                isLoading.value = true
            FirebaseFirestoreUtils.getMessageForUser(userEmail) {
                messages.value = it
                if (withLoading)
                    isLoading.value = false
            }

        }

    }

    fun sendMessage() {

        val state = messageSendBottomBarState.value as? MessageSendBottomBarState.Recorded

        if (state != null) {

            isSendProcessing.value = true
            val soundId = UUID.randomUUID().toString()
            val fileName = if (state.isRobotSound)
                "recordRobot"
            else
                "record"
            val path = "${getApplication<Application>().cacheDir.path}/$fileName"

            if (state.state.value != MediaState.Stop)
                playerCommand(MediaCommands.Stop)

            FirebaseStorageUtils.uploadSound(soundId, path) {

                if (it == "OK!") {

                    FirebaseFirestoreUtils.sendMessage(userEmail, soundId) { send ->

                        if (send == "OK!") {
                            isSendProcessing.value = false
                            messageSendBottomBarState.value = MessageSendBottomBarState.Waiting
                            updateMessages(true)
                        }

                    }

                }

            }

        }

    }

    fun recorderCommand(mediaCommand: MediaCommands) {

        val state: MutableState<MediaState> =
            (messageSendBottomBarState.value as? MessageSendBottomBarState.Recording)?.state
                ?: mutableStateOf(MediaState.Loading)

        if (checkListenPause(state, -2))
            setTime(state.value.duration)

        processRecorderCommand(mediaCommand, state)

    }

    fun playerCommand(mediaCommand: MediaCommands, id: Int = -1, seek: Float = -1f) {

        val mediaState = if (id == -1)
            (messageSendBottomBarState.value as? MessageSendBottomBarState.Recorded)?.state
        else
            getMessageStatus(id)

        if (mediaState == null)
            return

        if (checkListenPause(mediaState, id)) {

            val waitingStatus = mediaState.value as? MediaState.Waiting

            if (waitingStatus != null) {

                val seekVal = if (seek == -1f)
                    waitingStatus.progress
                else
                    seek
                processPlayerCommand(MediaCommands.Start, mediaState, seekVal)

                if (mediaCommand != MediaCommands.Resume)
                    processPlayerCommand(mediaCommand, mediaState)
                return

            }

        }

        processPlayerCommand(mediaCommand, mediaState, seek)

    }

    fun getMessageStatus(id: Int): MutableState<MediaState> {
        return messagesStatus[id] ?: mutableStateOf<MediaState>(MediaState.Stop).also {
            messagesStatus[id] = it
        }
    }

    fun playerSeek(seek: Float, id: Int = -1) {

        playerCommand(MediaCommands.Start, id, seek)

    }

    private fun startDurationJob(
        durationUpdate: () -> Unit,
        updateTime: Boolean = false,
        delay: Int = 161
    ) {

        durationJob?.cancel()
        durationJob = viewModelScope.launch(Dispatchers.Main) {

            while (isActive) {
                delay(delay.toLong())
                if (updateTime)
                    time.add(Calendar.MILLISECOND, delay)
                durationUpdate()
            }

        }

    }

    private fun stopDurationJob() {
        durationJob?.cancel()
    }

    private fun processRecorderCommand(
        mediaCommand: MediaCommands,
        mediaState: MutableState<MediaState>?
    ) {

        var updateState = mediaState
        val updateDuration: () -> Unit = {
            updateState?.value = MediaState.Playing(getTime())
        }

        when (mediaCommand) {
            MediaCommands.Start -> {
                if (messageSendBottomBarState.value == MessageSendBottomBarState.Waiting) {
                    recorder = initMediaRecorder()
                    recorder?.start()
                    setTime()
                    updateState = mutableStateOf(MediaState.Playing(getTime()))
                    messageSendBottomBarState.value =
                        MessageSendBottomBarState.Recording(updateState)
                    checkListenPause(updateState, -2)
                    startDurationJob(updateDuration, updateTime = true)
                }
            }
            MediaCommands.Stop -> {
                val state = messageSendBottomBarState.value as? MessageSendBottomBarState.Recording
                if (state != null && mediaState?.value != MediaState.Stop) {
                    stopDurationJob()
                    recorder?.stop()
                    recorder?.release()
                    recorder = null
                    messageSendBottomBarState.value =
                        MessageSendBottomBarState.Recorded(mutableStateOf(MediaState.Stop))
                    checkListenPause()
                }
            }
            MediaCommands.Pause -> {
                val state = messageSendBottomBarState.value as? MessageSendBottomBarState.Recording
                if (state != null && mediaState?.value is MediaState.Playing) {
                    stopDurationJob()
                    recorder?.pause()
                    mediaState.value = MediaState.Waiting(getTime())
                }
            }
            MediaCommands.Resume -> {
                val state = messageSendBottomBarState.value as? MessageSendBottomBarState.Recording
                if (state != null && mediaState?.value is MediaState.Waiting) {
                    recorder?.resume()
                    mediaState.value = MediaState.Playing(getTime())
                    startDurationJob(updateDuration, updateTime = true)
                }
            }
        }

    }

    private fun processPlayerCommand(
        mediaCommand: MediaCommands,
        state: MutableState<MediaState>,
        seek: Float = -1f
    ) {

        val updateDuration: () -> Unit = {

            player?.let {

                if (it.currentPosition == it.duration)
                    processPlayerCommand(MediaCommands.Stop, state)
                else {
                    setTime("0:0:${it.currentPosition}")
                    state.value =
                        MediaState.Playing(
                            duration = getTime(),
                            progress = it.currentPosition / it.duration.toFloat()
                        )
                }

            }

        }

        when (mediaCommand) {

            MediaCommands.Start -> {
                if (state.value is MediaState.Stop || state.value is MediaState.Waiting) {

                    state.value = MediaState.Loading
                    getUri {

                        if (it == null) {

                            state.value = MediaState.Stop

                        } else if (state.value == MediaState.Loading) {

                            val mediaPlayer = initMediaPlayer(it)
                            mediaPlayer.start()
                            if (seek != -1f)
                                mediaPlayer.seekTo((mediaPlayer.duration * seek).toInt())
                            player = mediaPlayer
                            state.value = MediaState.Playing("0:00:000")
                            startDurationJob(updateDuration)

                        }

                    }

                } else if (seek != -1f) {
                    player?.let { mediaPlayer ->
                        mediaPlayer.seekTo((mediaPlayer.duration * seek).toInt())
                    }
                }
            }
            MediaCommands.Stop -> {

                if (state.value != MediaState.Stop) {

                    stopDurationJob()
                    player?.also {
                        if (it.isPlaying)
                            it.stop()
                    }
                    player?.release()
                    player = null
                    state.value = MediaState.Stop
                    checkListenPause()

                }

            }
            MediaCommands.Pause -> {

                if (state.value is MediaState.Playing) {

                    stopDurationJob()
                    player?.also {
                        if (it.isPlaying)
                            it.pause()
                    }
                    state.value = MediaState.Waiting(state.value.duration, state.value.progress)

                }

            }
            MediaCommands.Resume -> {

                if (state.value is MediaState.Waiting) {

                    if (player == null) {
                        processPlayerCommand(
                            MediaCommands.Start,
                            state,
                            if (seek == -1f) state.value.progress else seek
                        )
                    }
                    player?.also {
                        if (!it.isPlaying)
                            it.start()
                    }
                    state.value = MediaState.Playing(state.value.duration, state.value.progress)
                    startDurationJob(updateDuration)

                }

            }

        }

    }

    private fun getUri(result: (Uri?) -> Unit) {

        val media = processingMedia

        if (media == null) {

            result(null)

        } else {

            when (media.first) {

                -1 -> {

                    val recordedState =
                        messageSendBottomBarState.value as? MessageSendBottomBarState.Recorded
                    if (recordedState != null) {

                        val fileName = if (recordedState.isRobotSound)
                            "recordRobot"
                        else
                            "record"

                        val file = File("${getApplication<Application>().cacheDir.path}/$fileName")
                        result(Uri.fromFile(file))
                    } else
                        result(null)

                }

                in 0..Int.MAX_VALUE -> {

                    val soundId = messages.value[media.first].soundId
                    FirebaseStorageUtils.getSoundUri(soundId, result)

                }

            }

        }

    }

    private fun getTime(): String {
        val format = SimpleDateFormat("m:ss:SSS", Locale.getDefault())
        return format.format(time.time)
    }

    private fun setTime(newTime: String? = null) {

        val split = newTime?.split(':')?.map { it.toInt() }

        time.set(Calendar.MINUTE, split?.get(0) ?: 0)
        time.set(Calendar.SECOND, split?.get(1) ?: 0)
        time.set(Calendar.MILLISECOND, split?.get(2) ?: 0)

    }

    @Synchronized
    private fun checkListenPause(
        mediaState: MutableState<MediaState>? = null,
        id: Int = -3
    ): Boolean {

        if (mediaState == null) {
            processingMedia = null
            return false
        }

        if (processingMedia == null) {
            processingMedia = Pair(id, mediaState)
            return false
        }

        processingMedia?.also { media ->

            if (id == media.first) {
                processingMedia = Pair(id, mediaState)
                return false
            }

            if (media.first == -2)
                processRecorderCommand(MediaCommands.Pause, media.second)
            else
                processPlayerCommand(MediaCommands.Pause, media.second)

            processingMedia = Pair(id, mediaState)
            return true

        }


        return false
    }

    private fun initMediaRecorder(): MediaRecorder {
        return MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(getApplication<Application>().cacheDir.path + "/record")
            prepare()
        }
    }

    private fun initMediaPlayer(uri: Uri): MediaPlayer {
        return MediaPlayer.create(getApplication(), uri)
    }

    override fun onCleared() {
        recorder?.release()
        player?.release()
        recorder = null
        player = null
        durationJob?.cancel()
        durationJob = null
        super.onCleared()
    }

}