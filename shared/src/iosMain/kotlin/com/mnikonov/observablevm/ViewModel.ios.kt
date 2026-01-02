package com.mnikonov.observablevm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

actual abstract class ViewModel actual constructor() {
    actual val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    actual fun dispose() {
        onCleared()
        coroutineScope.cancel()
    }

    protected actual open fun onCleared() {

    }
}
