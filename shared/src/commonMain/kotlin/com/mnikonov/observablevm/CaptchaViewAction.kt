package com.mnikonov.observablevm

import com.mnikonov.observablevm.domain.CaptchaImage

sealed interface CaptchaViewAction {
    data object Submit : CaptchaViewAction
    data object Retry : CaptchaViewAction
    data class ToggleImage(val image: CaptchaImage) : CaptchaViewAction
}
