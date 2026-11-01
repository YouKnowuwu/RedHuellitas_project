plugins {
    alias(libs.plugins.android.application) apply false
    // Asegura que la versión de Kotlin coincida con la que requiere tu proyecto (ej. 2.4.0)
    kotlin("android") version "2.4.0" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    alias(libs.plugins.kotlin.compose) apply false
}