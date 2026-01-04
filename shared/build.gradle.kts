plugins {
    kotlin("multiplatform")
    alias(libs.plugins.skie)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlinKSP)
}

version = "0.0.1"

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget()
    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "SharedSDK"
            freeCompilerArgs += "-Xbinary=bundleId=com.mnikonov.observablevm.SharedSDK"

            isStatic = false
            transitiveExport = false
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.androidCompose)
            implementation(libs.androidx.lifecycle.viewmodel)
        }
        commonMain.dependencies {
            implementation(projects.swiftvm)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.mnikonov.observablevm.shared"
    compileSdk = 36
}

dependencies {
    add("kspCommonMainMetadata", projects.swiftvmKspProcessor)
}

tasks.register<Copy>("exportSwiftVmManifest") {
    group = "swiftvm"
    description = "Exports SwiftVM manifest"

    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))

    val fromDir = layout.buildDirectory.dir("generated/ksp/metadata/commonMain/resources")
    from(fromDir) {
        include("swiftvm-manifest.json")
    }

    into(rootProject.layout.buildDirectory.dir("swiftvm").get().asFile)
}

tasks.named("embedAndSignAppleFrameworkForXcode").configure {
    dependsOn(
        tasks.named("exportSwiftVmManifest")
    )
}
