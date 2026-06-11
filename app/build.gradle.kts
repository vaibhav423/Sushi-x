plugins {
    id("com.android.application")
}

android {
    namespace = "fire.sushi.ui"
    compileSdk = 34

    defaultConfig {
        applicationId = "fire.sushi.ui"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:101.0.1")
    implementation(files("libs/bsh-2.1.1.jar"))
}
