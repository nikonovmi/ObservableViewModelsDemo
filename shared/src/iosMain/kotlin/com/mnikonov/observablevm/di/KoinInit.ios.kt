package com.mnikonov.observablevm.di

import com.mnikonov.observablevm.CaptchaViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

actual fun initDI(configuration: PlatformConfiguration) {
    val platformCoreModule = module {
        single<PlatformConfiguration> { configuration }
    }
    val captchaModule = module {
        factory<CaptchaViewModel> { CaptchaViewModel() }
    }
    startKoin {
        modules(
            platformCoreModule,
            captchaModule,
        )
    }
}
