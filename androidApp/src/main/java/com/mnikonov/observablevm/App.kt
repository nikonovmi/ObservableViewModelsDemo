package com.mnikonov.observablevm

import android.app.Application
import com.mnikonov.observablevm.di.PlatformConfiguration
import com.mnikonov.observablevm.di.initDI

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        initDI(PlatformConfiguration(androidContext = applicationContext))
    }
}
