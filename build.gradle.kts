buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // AGP 9's built-in Kotlin ships with KGP 2.2.10, whose compiler can only
        // read Kotlin metadata up to 2.3.0. The project's dependencies (androidx,
        // dotsindicator) pull kotlin-stdlib 2.4.0, so we bump the built-in Kotlin
        // Gradle Plugin up to 2.4.10 to match. KSP is bumped in lockstep.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.11")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}
