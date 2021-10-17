package com.danego.voicechat.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.danego.voicechat.model.UserDetailsModel
import com.danego.voicechat.utils.FirebaseAuthUtils
import com.danego.voicechat.utils.FirebaseFirestoreUtils

class MessageListViewModel : ViewModel() {

    private val userDetailsLoaded = mutableStateOf(false)
    private val messagesLoaded = mutableStateOf(false)
    val userDetails = mutableStateOf(UserDetailsModel())
    val userEmail = mutableStateOf(FirebaseAuthUtils.getUserEmail())
    val users = mutableStateOf(listOf<String>())
    val isLoading = derivedStateOf { !userDetailsLoaded.value && !messagesLoaded.value }

    init {

        FirebaseFirestoreUtils.getUserDetails {
            userDetails.value = it
            userDetailsLoaded.value = true
        }

       updateMessages()

    }

    fun updateMessages() {

        FirebaseFirestoreUtils.getMessageUsers {
            users.value = it
            messagesLoaded.value = true
        }

    }

}