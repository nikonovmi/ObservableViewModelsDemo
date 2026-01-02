package com.mnikonov.observablevm.di

import org.koin.core.context.startKoin
import org.koin.dsl.module

actual fun initDI(configuration: PlatformConfiguration) {
    val platformCoreModule = module {
        single<PlatformConfiguration> { configuration }
    }
    startKoin {
        modules(
            platformCoreModule,
        )
    }
}
