package com.mnikonov.observablevm.di

import com.mnikonov.observablevm.CaptchaViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual fun initDI(configuration: PlatformConfiguration) {
    val platformCoreModule = module {
        single<PlatformConfiguration> { configuration }
    }
    val captchaModule = module {
        viewModel { CaptchaViewModel() }
    }

    startKoin {
        androidContext(configuration.androidContext)
        androidLogger()

        modules(
            platformCoreModule,
            captchaModule,
        )
    }
}
