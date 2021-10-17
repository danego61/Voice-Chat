package com.danego.voicechat.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danego.voicechat.model.UserDetailsModel
import com.danego.voicechat.utils.FirebaseAuthUtils
import com.danego.voicechat.utils.FirebaseFirestoreUtils
import com.danego.voicechat.utils.FirebaseStorageUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginScreenViewModel : ViewModel() {

    private val _loginStatus: MutableState<LoginScreenStatus> =
        mutableStateOf(LoginScreenStatus.Loading)
    private var snackbarJob: Job? = null

    val loginStatus: State<LoginScreenStatus> = _loginStatus
    val userEmail = mutableStateOf("")
    val userPassword = mutableStateOf("")
    val isLoginProcess = mutableStateOf(false)
    val isRegistrationProcess = mutableStateOf(false)
    val snackbarMessage = mutableStateOf("")
    val userName = mutableStateOf("")
    val userTelephone = mutableStateOf("")
    val userFirstName = mutableStateOf("")
    val userSurname = mutableStateOf("")
    val userImage = mutableStateOf("")

    init {
        if (FirebaseAuthUtils.checkIsLogged()) {
            checkRegistrationComplete()
        } else {
            changeStatus(LoginScreenStatus.ChoiceLoginType)
        }
    }

    fun login() {
        isLoginProcess.value = true
        if (loginStatus.value is LoginScreenStatus.SingIn) {
            Firebase.auth.signInWithEmailAndPassword(userEmail.value, userPassword.value)
                .addOnSuccessListener {
                    checkRegistrationComplete()
                }.addOnFailureListener {
                    showSnackBar(it.localizedMessage ?: it.message ?: "")
                    isLoginProcess.value = false
                }
        } else {
            Firebase.auth.createUserWithEmailAndPassword(userEmail.value, userPassword.value)
                .addOnSuccessListener {
                    changeStatus(LoginScreenStatus.UserDetails)
                }.addOnFailureListener {
                    showSnackBar(it.localizedMessage ?: it.message ?: "")
                    isLoginProcess.value = false
                }
        }
    }

    fun completeRegistration() {
        isRegistrationProcess.value = true
        FirebaseFirestoreUtils.registrationComplete(
            UserDetailsModel(
                userName = userName.value,
                userTelephone = userTelephone.value,
                userFirstName = userFirstName.value,
                userSurname = userSurname.value
            )
        ) {
            if (it == "OK!") {
                if (userImage.value != "") {
                    FirebaseStorageUtils.uploadUserImage { result ->
                        if (result == "OK!") {
                            changeStatus(LoginScreenStatus.Finish)
                        } else {
                            showSnackBar(result)
                        }
                    }
                } else {
                    changeStatus(LoginScreenStatus.Finish)
                }
            } else {
                isRegistrationProcess.value = false
                showSnackBar(it)
            }
        }
    }

    fun changeStatus(newStatus: LoginScreenStatus) {

        if (newStatus == LoginScreenStatus.ChoiceLoginType)
            if (Firebase.auth.currentUser != null)
                Firebase.auth.signOut()

        if (
            newStatus == LoginScreenStatus.UserDetails ||
            newStatus == LoginScreenStatus.ChoiceLoginType
        ) {
            userEmail.value = ""
            userPassword.value = ""
            isLoginProcess.value = false
            userName.value = ""
            userTelephone.value = ""
            userFirstName.value = ""
            userSurname.value = ""
            userImage.value = ""
        }

        _loginStatus.value = newStatus
    }

    private fun showSnackBar(message: String) {
        snackbarMessage.value = message
        snackbarJob?.cancel()
        snackbarJob = viewModelScope.launch {
            delay(4000)
            snackbarMessage.value = ""
        }
    }

    private fun checkRegistrationComplete() {
        FirebaseFirestoreUtils.isUserRegistrationComplete {
            if (it) {
                changeStatus(LoginScreenStatus.Finish)
            } else {
                changeStatus(LoginScreenStatus.UserDetails)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        snackbarJob?.cancel()
        snackbarJob = null
    }

}

sealed class LoginScreenStatus {

    object Loading : LoginScreenStatus()

    object ChoiceLoginType : LoginScreenStatus()

    object SingIn : LoginScreenStatus()

    object SingUp : LoginScreenStatus()

    object UserDetails : LoginScreenStatus()

    object Finish : LoginScreenStatus()

}