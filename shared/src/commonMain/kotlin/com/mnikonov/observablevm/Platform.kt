package com.mnikonov.observablevm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform