package com.mnikonov.observablevm.domain

data class CaptchaChallenge(
    val images: List<CaptchaImage>,
    val type: CaptchaType,
)

fun randomChallenge(): CaptchaChallenge {
    return CaptchaChallenge(
        images = CaptchaImage.entries.shuffled().take(9),
        type = CaptchaType.entries.random(),
    )
}
