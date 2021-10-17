package com.danego.voicechat.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danego.voicechat.model.UserDetailsModel
import com.danego.voicechat.utils.FirebaseFirestoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchScreenViewModel : ViewModel() {

    val isUserNameSearch = mutableStateOf(true)
    val isTelephoneSearch = mutableStateOf(false)
    val isEmailSearch = mutableStateOf(false)
    val searchText = mutableStateOf("")
    val searchResults = mutableStateOf(listOf<Pair<String, UserDetailsModel>>())
    val isLoading = mutableStateOf(false)
    private var searchJob: Job? = null

    fun search() {

        if (searchText.value.isNotEmpty() && !isLoading.value) {

            searchJob?.cancel()
            isLoading.value = true
            val result: (List<Pair<String, UserDetailsModel>>) -> Unit = {
                searchResults.value = it
                isLoading.value = false
            }
            searchJob = viewModelScope.launch(Dispatchers.Main) {

                when {

                    isUserNameSearch.value ->
                        FirebaseFirestoreUtils.searchByUserName(searchText.value, result)

                    isTelephoneSearch.value ->
                        FirebaseFirestoreUtils.searchByTelephone(searchText.value, result)

                    isEmailSearch.value ->
                        FirebaseFirestoreUtils.searchByUserEmail(searchText.value, result)

                }

            }

        }

    }

    override fun onCleared() {
        searchJob?.cancel()
        searchJob = null
        super.onCleared()
    }

}