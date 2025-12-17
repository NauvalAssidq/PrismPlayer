val keystorePath = providers.gradleProperty("KEYSTORE_PATH").orNull
val keystorePass = providers.gradleProperty("KEYSTORE_PASSWORD").orNull
val keyAliasVal  = providers.gradleProperty("KEY_ALIAS").orNull
val keyPass      = providers.gradleProperty("KEY_PASSWORD").orNull

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.3.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
}

android {
    namespace = "org.android.prismplayer"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.android.prismplayer"
        minSdk = 24
        targetSdk = 36
        versionCode = 1002
        versionName = "1.0.0-beta.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (!keystorePath.isNullOrBlank()
            && !keystorePass.isNullOrBlank()
            && !keyAliasVal.isNullOrBlank()
            && !keyPass.isNullOrBlank()
        ) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePass
                keyAlias = keyAliasVal
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
    }

    tasks.register("renameReleaseApk") {
        dependsOn("assembleRelease")
        doLast {
            val versionName = android.defaultConfig.versionName ?: "0.0.0"
            val appName = "PrismPlayer"
            val apkDir = file("$buildDir/outputs/apk/release")
            val unsigned = File(apkDir, "app-release-unsigned.apk")
            val signed = File(apkDir, "app-release.apk")
            val input = when {
                signed.exists() -> signed
                unsigned.exists() -> unsigned
                else -> error("No release APK found in: $apkDir")
            }
            val output = File(apkDir, "$appName-$versionName.apk")
            input.copyTo(output, overwrite = true)
            println("Created: ${output.absolutePath}")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.play.services.appsearch)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.coil.compose)
    implementation(libs.androidx.palette.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.androidx.room.compiler) {
        exclude(group = "com.intellij", module = "annotations")
    }

    implementation(libs.accompanist.permissions)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.coil.compose.v260)
    implementation(libs.reorderable)
    implementation(libs.accompanist.permissions.v0340)
    implementation("net.jthink:jaudiotagger:3.0.1") {
        exclude(group = "com.intellij", module = "annotations")
    }}