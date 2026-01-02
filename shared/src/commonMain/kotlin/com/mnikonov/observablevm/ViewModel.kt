package com.mnikonov.observablevm

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
    protected val coroutineScope: CoroutineScope

    fun dispose()

    protected open fun onCleared()
}
