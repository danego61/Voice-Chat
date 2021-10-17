package com.danego.voicechat.utils

import com.danego.voicechat.model.MessageModel
import com.danego.voicechat.model.UserDetailsModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class FirebaseFirestoreUtils {

    companion object {

        fun isUserRegistrationComplete(result: (Boolean) -> Unit) {
            Firebase.auth.currentUser?.also { user ->
                Firebase.firestore
                    .collection("users")
                    .document(user.email ?: "")
                    .get()
                    .addOnSuccessListener {
                        result(it.exists())
                    }.addOnFailureListener {
                        result(false)
                    }

            }
        }

        fun registrationComplete(userDetails: UserDetailsModel, result: (String) -> Unit) {

            val user = Firebase.auth.currentUser ?: result.let {
                result("User Not Logged!")
                return
            }

            Firebase.firestore.collection("users").whereEqualTo("userName", userDetails.userName)
                .get().addOnSuccessListener {
                    if (it.size() == 0) {
                        Firebase.firestore
                            .collection("users")
                            .document(user.email ?: "")
                            .set(userDetails)
                            .addOnSuccessListener {
                                result("OK!")
                            }.addOnFailureListener { ex ->
                                result(ex.localizedMessage ?: "Error!")
                            }
                    } else {
                        result("User Name Already Exists!")
                    }
                }.addOnFailureListener {
                    result(it.localizedMessage ?: "Error!")
                }

        }

        fun getUserDetails(
            email: String? = null,
            result: (UserDetailsModel) -> Unit
        ) {

            val userEmail = email ?: Firebase.auth.currentUser?.email ?: return

            Firebase.firestore
                .collection("users")
                .document(userEmail)
                .get()
                .addOnSuccessListener {
                    val model: UserDetailsModel? = it.toObject()
                    if (model != null)
                        result(model)
                }

        }

        fun getMessageUsers(result: (List<String>) -> Unit) {

            val user = Firebase.auth.currentUser?.email ?: return

            Firebase.firestore
                .collection(user)
                .get()
                .addOnSuccessListener {
                    val userEmails = arrayListOf<String>()
                    val users = it.documents
                    for (userEmail in users) {
                        userEmails.add(userEmail.id)
                    }
                    result(userEmails.toList())
                }
                .addOnFailureListener {
                    result(listOf())
                }

        }

        fun getMessageForUser(userEmail: String, result: (List<MessageModel>) -> Unit) {

            val user = Firebase.auth.currentUser?.email ?: return

            Firebase.firestore
                .collection(user)
                .document(userEmail)
                .get()
                .addOnSuccessListener {

                    val list = arrayListOf<MessageModel>()
                    it.data?.also { dataList ->

                        for (data in dataList.values) {

                            list.add(
                                Gson().fromJson(
                                    data.toString(),
                                    MessageModel::class.java
                                )
                            )

                        }
                        val formatter = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss Z",
                            Locale.getDefault()
                        )
                        list.sortByDescending { sort ->
                            formatter.parse(sort.date)?.time
                        }
                    }
                    result(list.toList())

                }.addOnFailureListener {
                    result(listOf())
                }

        }

        fun sendMessage(userEmail: String, soundId: String, result: (String) -> Unit) {

            val user = Firebase.auth.currentUser?.email ?: return
            val messageModel = MessageModel(
                soundId = soundId,
                isSend = true,
                date = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss Z",
                    Locale.getDefault()
                ).format(Date())
            )

            sendUserMessage(userEmail, user, messageModel) {

                if (it == "OK!") {

                    sendUserMessage(
                        user,
                        userEmail,
                        messageModel.copy(isSend = false, isNotified = false)
                    ) { second ->

                        result(second)

                    }

                } else
                    result(it)

            }

        }

        private fun sendUserMessage(
            sendUserEmail: String,
            fromUserEmail: String,
            messageModel: MessageModel,
            result: (String) -> Unit
        ) {

            val firestore = Firebase.firestore
            val data = hashMapOf(
                firestore.collection(sendUserEmail).document().id to Gson().toJson(messageModel)
            )

            firestore
                .collection(fromUserEmail)
                .document(sendUserEmail)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    result("OK!")
                }
                .addOnFailureListener {
                    result(it.localizedMessage ?: it.message ?: "Error!")
                }
        }

        fun searchByUserName(
            userName: String,
            result: (List<Pair<String, UserDetailsModel>>) -> Unit
        ) {

            val user = Firebase.auth.currentUser?.email ?: return

            Firebase.firestore
                .collection("users")
                .get()
                .addOnSuccessListener {

                    val models = arrayListOf<Pair<String, UserDetailsModel>>()

                    for (document in it.documents) {

                        if (document.id != user) {

                            val model: UserDetailsModel? = document.toObject()
                            if (model != null && model.userName.contains(userName, true))
                                models.add(Pair(document.id, model))

                        }

                    }
                    result(models)

                }
                .addOnFailureListener {

                    result(listOf())

                }


        }

        fun searchByUserEmail(
            userEmail: String,
            result: (List<Pair<String, UserDetailsModel>>) -> Unit
        ) {

            val user = Firebase.auth.currentUser?.email ?: return
            Firebase.firestore
                .collection("users")
                .get()
                .addOnSuccessListener {

                    val models = arrayListOf<Pair<String, UserDetailsModel>>()

                    for (document in it.documents) {

                        if (document.id != user && document.id.contains(userEmail, true)) {

                            val model: UserDetailsModel? = document.toObject()
                            if (model != null)
                                models.add(Pair(document.id, model))

                        }

                    }
                    result(models)

                }
                .addOnFailureListener {

                    result(listOf())

                }

        }

        fun searchByTelephone(
            telephone: String,
            result: (List<Pair<String, UserDetailsModel>>) -> Unit
        ) {

            val user = Firebase.auth.currentUser?.email ?: return
            Firebase.firestore
                .collection("users")
                .whereGreaterThanOrEqualTo("userTelephone", telephone)
                .get()
                .addOnSuccessListener {

                    val models = arrayListOf<Pair<String, UserDetailsModel>>()

                    for (document in it.documents) {

                        if (document.id != user) {

                            val model: UserDetailsModel? = document.toObject()
                            if (model != null)
                                models.add(Pair(document.id, model))

                        }

                    }
                    result(models)

                }
                .addOnFailureListener {

                    result(listOf())

                }

        }

        fun updateUserDetails(
            email: String? = null,
            userDetails: UserDetailsModel,
            result: (String) -> Unit
        ) {

            val userEmail = email ?: Firebase.auth.currentUser?.email ?: return

            Firebase.firestore
                .collection("users")
                .document(userEmail)
                .set(userDetails)
                .addOnSuccessListener {
                    result("OK!")
                }
                .addOnFailureListener {
                    result("Error!")
                }

        }

    }

}