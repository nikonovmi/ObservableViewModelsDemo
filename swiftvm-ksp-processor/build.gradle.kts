plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.swiftvm)
}
