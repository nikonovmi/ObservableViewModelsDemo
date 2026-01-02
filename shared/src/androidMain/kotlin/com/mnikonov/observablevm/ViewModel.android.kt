package com.mnikonov.observablevm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import androidx.lifecycle.ViewModel as AndroidXViewModel

actual abstract class ViewModel actual constructor() : AndroidXViewModel() {
    actual val coroutineScope = viewModelScope

    actual fun dispose() {
        coroutineScope.cancel()
        onCleared()
    }

    actual override fun onCleared() {
        super.onCleared()
    }
}
