package com.danego.voicechat.utils

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File

class FirebaseStorageUtils {

    companion object {

        fun uploadUserImage(result: (String) -> Unit) {

            val email = Firebase.auth.currentUser?.email ?: result.let {
                result("Error!")
                return
            }
            val context = Firebase.storage.app.applicationContext
            val metadata = storageMetadata {
                contentType = "image/jpg"
            }

            Firebase.storage.reference.child("/user_images/$email").putFile(
                Uri.fromFile(File(context.cacheDir.path + "/CI")),
                metadata
            ).addOnSuccessListener {
                result("OK!")
            }.addOnFailureListener {
                result(it.localizedMessage ?: it.message ?: "Error!")
            }
        }

        fun uploadSound(soundId: String, filePath: String, result: (String) -> Unit) {

            Firebase.storage.reference.child("/messages/$soundId").putFile(
                Uri.fromFile(File(filePath))
            ).addOnSuccessListener {
                result("OK!")
            }.addOnFailureListener {
                result(it.localizedMessage ?: it.message ?: "Error!")
            }

        }

        fun getSoundUri(soundId: String, result: (Uri?) -> Unit) {

            Firebase.storage.reference.child("/messages/$soundId")
                .downloadUrl.addOnSuccessListener {
                    result(it)
                }.addOnFailureListener {
                    result(null)
                }

        }

    }

}