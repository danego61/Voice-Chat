package com.danego.voicechat.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class GlideUtils {

    companion object {

        @Composable
        fun loadFromFile(
            path: String,
            diskCache: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC
        ): MutableState<Bitmap?> {

            val bitmapState: MutableState<Bitmap?> = remember {
                mutableStateOf(null)
            }

            Glide.with(LocalContext.current)
                .asBitmap()
                .load(File(path))
                .diskCacheStrategy(diskCache)
                .skipMemoryCache(diskCache == DiskCacheStrategy.NONE)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bitmapState.value = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })

            return bitmapState
        }

        @Composable
        fun getUserImage(userEmail: String): MutableState<Bitmap?> {

            val bitmapState: MutableState<Bitmap?> = remember {
                mutableStateOf(null)
            }
            val reference = Firebase.storage.getReference("user_images").child(userEmail)
            val context = LocalContext.current

            reference.downloadUrl.addOnSuccessListener {
                Glide.with(context)
                    .asBitmap()
                    .load(it)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bitmapState.value = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }

            return bitmapState
        }

    }

}