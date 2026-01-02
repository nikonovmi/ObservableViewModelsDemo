package com.mnikonov.observablevm

import com.mnikonov.observablevm.domain.CaptchaImage

sealed interface CaptchaViewState {
    data object Loading : CaptchaViewState

    data class Active(
        val images: List<CaptchaImageUiModel>,
        val promptText: String,
        val isVerifying: Boolean,
    ) : CaptchaViewState

    data object Failed : CaptchaViewState

    data object Success : CaptchaViewState
}

data class CaptchaImageUiModel(
    val image: CaptchaImage,
    val isSelected: Boolean,
)
