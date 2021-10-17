package com.danego.voicechat.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.danego.voicechat.model.UserDetailsModel
import com.danego.voicechat.utils.FirebaseFirestoreUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingScreenViewModel : ViewModel() {

    val userTelephone = mutableStateOf("")
    val userFirstName = mutableStateOf("")
    val userSurname = mutableStateOf("")
    val isRegistrationProcess = mutableStateOf(false)
    val userName = mutableStateOf("")
    private var userDetails: UserDetailsModel? = null
    val isProcessing = mutableStateOf(false)
    val userEmail: MutableState<String?> = mutableStateOf(null)

    fun init(userEmail: String) {

        isProcessing.value = true
        this.userEmail.value =
            if (userEmail == "?")
                Firebase.auth.currentUser?.email
            else
                userEmail

        FirebaseFirestoreUtils.getUserDetails(this.userEmail.value) {

            userDetails = it
            userTelephone.value = it.userTelephone
            userFirstName.value = it.userFirstName
            userSurname.value = it.userSurname
            userName.value = it.userName
            isProcessing.value = false

        }

    }

    fun saveChanges() {
        isRegistrationProcess.value = true
        FirebaseFirestoreUtils.updateUserDetails(
            userEmail.value,
            UserDetailsModel(
                userName = userName.value,
                userTelephone = userTelephone.value,
                userFirstName = userFirstName.value,
                userSurname = userSurname.value
            )
        ) {
            isRegistrationProcess.value = false
        }
    }

}
