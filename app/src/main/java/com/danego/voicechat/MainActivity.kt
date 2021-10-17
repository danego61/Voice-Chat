package com.danego.voicechat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.danego.voicechat.navigation.NavGraph
import com.danego.voicechat.navigation.NavRoutes
import com.danego.voicechat.navigation.destinations.*
import com.danego.voicechat.services.NotificationService
import com.danego.voicechat.ui.theme.VoiceChatTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.soundcloud.android.crop.Crop
import java.io.File

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {

    private var choiceImage: MutableState<String>? = null
    private val choiceImageResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                performCrop(uri)
            }
        }
    }
    private val cropImageResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.extras?.getParcelable("output")
            uri?.path?.also {
                choiceImage?.value = it
            }
        }
    }
    private val permissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkRecordPermission()
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkRecordPermission()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkRecordPermission()
        createService()
        val startDestination = intent?.getStringExtra("email")
        if (startDestination != null)
            intent.removeExtra("email")
        setContent {
            VoiceChatTheme {
                val navController = rememberAnimatedNavController()
                SetupNavGraph(navController = navController, startDestination)
            }
        }
    }

    private fun createService() {
        val serviceIntent = Intent(this, NotificationService::class.java)
        serviceIntent.putExtra("userEmail", Firebase.auth.currentUser?.email)
        startService(serviceIntent)
    }

    @Composable
    private fun SetupNavGraph(navController: NavHostController, startDestination: String?) {

        val routes = remember(navController) {
            NavRoutes(navController = navController)
        }

        AnimatedNavHost(
            navController = navController,
            startDestination = NavGraph.SplashScreen.route
        ) {

            splashComposable(
                splashToLogin = routes.splashToLogin,
                splashToMain = {
                    routes.splashToMain()
                    if (startDestination != null)
                        routes.messageListToMessage(startDestination)
                }
            )

            loginComposable(
                choiceUserImage = ::choiceUserImage,
                loginToMain = routes.loginToMain
            )

            messageListComposable(
                messageListToMessage = routes.messageListToMessage,
                messageListToSearch = routes.messageListToSearch,
                messageListToSettings = routes.messageListToSettings
            )

            messageComposable(
                messageToMessageList = routes.messageToMessageList,
                messageToSettings = routes.messageToSettings
            )

            searchComposable(
                searchToMessage = routes.searchToMessage,
                searchToMessageList = routes.searchToMessageList
            )

            settingsComposable(
                settingsToLogin = routes.settingsToLogin,
                settingsToMessageList = routes.settingsToMessageList,
                settingsToMessage = routes.settingsToMessage
            )

        }
    }

    private fun choiceUserImage(imageState: MutableState<String>) {
        choiceImage = imageState
        openChoiceImage()
    }

    private fun performCrop(picUri: Uri) {
        val tempFile = File(cacheDir.path + "/CI")
        cropImageResultLauncher.launch(
            Crop.of(picUri, Uri.fromFile(tempFile)).asSquare().getIntent(this)
        )
    }

    private fun openChoiceImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        choiceImageResultLauncher.launch(intent)
    }

    private fun checkRecordPermission() {
        val state = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        if (state == PackageManager.PERMISSION_DENIED) {
            val never = shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
            if (never) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                permissionResultLauncher.launch(intent)
                Toast.makeText(
                    this,
                    "Please allow for microphone recording",
                    Toast.LENGTH_LONG
                ).show()
            } else
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        }

    }

}