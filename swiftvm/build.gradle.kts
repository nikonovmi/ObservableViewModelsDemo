plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidLibrary)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()
    androidTarget()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
}

android {
    namespace = "com.mnikonov.observablevm.swiftvm"
    compileSdk = 36
}
