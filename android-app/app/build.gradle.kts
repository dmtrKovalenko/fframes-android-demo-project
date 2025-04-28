plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.curtesmalteser.hellorust"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.curtesmalteser.hellorust"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register<Exec>("buildRust") {
    group = "rust"
    val cargoPath = "${System.getProperty("user.home")}/.cargo/bin/cargo"
    workingDir("$projectDir/../../hello_rust_lib")
    commandLine(
        cargoPath, "ndk",
        "-t", "arm64-v8a",
        "-t", "armeabi-v7a",
        "-t", "x86",
        "-t", "x86_64",
        "-o", "$projectDir/../../hello_rust_lib/jniLibs",
        "build", "--release"
    )
}

tasks.register<Copy>("copyRustLibs") {
    group = "rust"
    dependsOn("buildRust")
    from("$projectDir/../../hello_rust_lib/jniLibs")
    into("$projectDir/src/main/jniLibs")
    include("**/*.so")
}

tasks.named("preBuild") {
    dependsOn("copyRustLibs")
}

tasks.named("preBuild") {
    dependsOn("clean")
}

