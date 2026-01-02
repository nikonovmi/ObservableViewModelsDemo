package com.mnikonov.observablevm.di

import com.mnikonov.observablevm.CaptchaViewModel
import org.koin.mp.KoinPlatform.getKoin

object CaptchaDependencies {
    fun getViewModel() = getKoin().get<CaptchaViewModel>()
}
