package com.danego.voicechat.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.danego.voicechat.R
import com.danego.voicechat.model.UserDetailsModel
import com.danego.voicechat.utils.GlideUtils

@ExperimentalAnimationApi
@Composable
fun SearchResults(
    searchResults: List<Pair<String, UserDetailsModel>>,
    loading: Boolean,
    searchToMessage: (String) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (!loading) {

            if (searchResults.isEmpty()) {

                Text(text = "Not Found!")

            } else {

                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    items(count = searchResults.size) { resultId ->

                        val result = searchResults[resultId]
                        val state = remember {
                            MutableTransitionState(false).apply {
                                targetState = true
                            }
                        }

                        AnimatedVisibility(
                            visibleState = state,
                            enter = expandVertically(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = 300
                                )
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(
                                    durationMillis = 300
                                )
                            )
                        ) {
                            UserCard(
                                userEmail = result.first,
                                userDetails = result.second,
                                searchToMessage = searchToMessage
                            )
                        }


                    }
                }

            }

        } else {

            CircularProgressIndicator()

        }
    }

}

@Composable
private fun UserCard(
    userEmail: String,
    userDetails: UserDetailsModel,
    searchToMessage: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { searchToMessage(userEmail) },
    ) {

        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ShowUserImage(
                userName = userEmail,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(65.dp),
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.body1
                )

                Text(
                    text = userDetails.userName,
                    style = MaterialTheme.typography.body1
                )

                Text(
                    text = userDetails.userTelephone,
                    style = MaterialTheme.typography.body1
                )

                Text(
                    text = "${userDetails.userFirstName} ${userDetails.userSurname}",
                    style = MaterialTheme.typography.body1
                )

            }

            Spacer(modifier = Modifier.weight(0.5f))

        }

    }
}

@Composable
private fun ShowUserImage(
    userName: String,
    modifier: Modifier = Modifier
) {

    val userImage by GlideUtils.getUserImage(userEmail = userName)

    if (userImage == null) {
        Image(
            painter = painterResource(id = R.drawable.no_picture),
            contentDescription = "User Image",
            modifier = modifier
        )
    } else {
        Image(
            bitmap = userImage!!.asImageBitmap(),
            contentDescription = "User Image",
            modifier = modifier
                .clip(CircleShape)
        )
    }

}