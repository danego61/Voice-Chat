package com.danego.voicechat.ui.screens.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danego.voicechat.viewmodel.SearchScreenViewModel

@ExperimentalAnimationApi
@Composable
fun SearchScreen(
    searchToMessage: (String) -> Unit,
    searchToMessageList: () -> Unit
) {

    val viewModel: SearchScreenViewModel = viewModel()

    Surface(modifier = Modifier.fillMaxSize()) {

        Column {

            Header(
                searchToMessageList = searchToMessageList,
                searchText = viewModel.searchText,
                emailSearch = viewModel.isEmailSearch,
                telephoneSearch = viewModel.isTelephoneSearch,
                userNameSearch = viewModel.isUserNameSearch,
                isLoading = viewModel.isLoading,
                search = viewModel::search
            )

            SearchResults(
                searchResults = viewModel.searchResults.value,
                loading = viewModel.isLoading.value,
                searchToMessage = searchToMessage
            )

        }

    }

}

@Composable
private fun Header(
    searchToMessageList: () -> Unit,
    searchText: MutableState<String>,
    emailSearch: MutableState<Boolean>,
    telephoneSearch: MutableState<Boolean>,
    userNameSearch: MutableState<Boolean>,
    isLoading: MutableState<Boolean>,
    search: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colors.primary,
                shape = RoundedCornerShape(
                    bottomEnd = 20.dp,
                    bottomStart = 20.dp
                )
            )
            .padding(10.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            BackButton(onClick = searchToMessageList)

            SearchBar(
                searchText = searchText,
                search = search,
                isLoading = isLoading.value
            )

        }

        SearchFields(
            emailSearch = emailSearch,
            telephoneSearch = telephoneSearch,
            userNameSearch = userNameSearch,
            isLoading = isLoading.value,
            search = search
        )

    }

}

@Composable
private fun SearchBar(
    searchText: MutableState<String>,
    search: () -> Unit,
    isLoading: Boolean
) {

    OutlinedTextField(
        value = searchText.value,
        onValueChange = { searchText.value = it },
        enabled = !isLoading,
        placeholder = {
            Text(text = "Search Text")
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { search() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 25.dp)
    )

}

@Composable
private fun SearchFields(
    emailSearch: MutableState<Boolean>,
    telephoneSearch: MutableState<Boolean>,
    userNameSearch: MutableState<Boolean>,
    isLoading: Boolean,
    search: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        ShowRadioButton(
            value = emailSearch,
            isEnabled = !isLoading,
            text = "Email"
        ) {
            emailSearch.value = true
            telephoneSearch.value = false
            userNameSearch.value = false
            search()
        }

        ShowRadioButton(
            value = telephoneSearch,
            isEnabled = !isLoading,
            text = "Telephone"
        ) {
            emailSearch.value = false
            telephoneSearch.value = true
            userNameSearch.value = false
            search()
        }

        ShowRadioButton(
            value = userNameSearch,
            isEnabled = !isLoading,
            text = "User Name"
        ) {
            emailSearch.value = false
            telephoneSearch.value = false
            userNameSearch.value = true
            search()
        }

    }

}

@Composable
private fun ShowRadioButton(
    value: MutableState<Boolean>,
    text: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {

    Row {

        RadioButton(
            selected = value.value,
            enabled = isEnabled,
            onClick = {
                if (!value.value)
                    onClick()
            }
        )

        Text(
            text = text,
            modifier = Modifier.padding(start = 5.dp)
        )
    }


}

@Composable
private fun BackButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.padding(5.dp)) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back"
        )
    }
}