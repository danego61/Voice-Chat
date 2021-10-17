package com.danego.voicechat.utils

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAuthUtils {

    companion object {

        fun checkIsLogged(): Boolean {
            return Firebase.auth.currentUser != null
        }

        fun getUserEmail(): String {
            return Firebase.auth.currentUser?.email ?: ""
        }

    }

}