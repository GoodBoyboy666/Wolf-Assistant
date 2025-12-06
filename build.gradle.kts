// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.3" apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    id("com.mikepenz.aboutlibraries.plugin") version "13.1.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
}
