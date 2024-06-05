import org.jetbrains.dokka.gradle.DokkaTask

buildscript {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
    }
}
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.devtools.ksp")
    id("org.jetbrains.dokka") version "1.9.20"

}

android {
    namespace = "com.example.simplewebdavmanager"
    compileSdk = 34
    buildFeatures{
        dataBinding = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.simplewebdavmanager"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


}
// Remove Inherited Members
tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            suppressInheritedMembers = true

            // Set the visibility settings
            documentedVisibilities.set(
                listOf(
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC,
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PRIVATE
                )
            )
        }

    }

}


dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.github.thegrizzlylabs:sardine-android:v0.9")
    implementation("com.github.lookfirst:sardine:sardine-5.10")
    implementation ("androidx.activity:activity-ktx:1.9.0")
    implementation ("androidx.fragment:fragment-ktx:1.7.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
