package com.bac.homelink.core.ui
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data:T) : UiState<T>()
    data class Error(val message:String, val throwable:Throwable?=null) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}
val <T> UiState<T>.isLoading get() = this is UiState.Loading
fun <T> UiState<T>.dataOrNull():T? = (this as? UiState.Success)?.data
