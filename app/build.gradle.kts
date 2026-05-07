plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.notiontasks"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.notiontasks"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Notion OAuth — set these in your local.properties or via GitHub Actions secrets
        val notionClientId = project.findProperty("NOTION_CLIENT_ID")?.toString() ?: ""
        val notionClientSecret = project.findProperty("NOTION_CLIENT_SECRET")?.toString() ?: ""
        val notionRedirectUri = "com.notiontasks://oauth/callback"

        buildConfigField("String", "NOTION_CLIENT_ID", "\"$notionClientId\"")
        buildConfigField("String", "NOTION_CLIENT_SECRET", "\"$notionClientSecret\"")
        buildConfigField("String", "NOTION_REDIRECT_URI", "\"$notionRedirectUri\"")
        buildConfigField("String", "NOTION_BASE_URL", "\"https://api.notion.com/v1/\"")
        buildConfigField("String", "NOTION_OAUTH_URL", "\"https://api.notion.com/v1/oauth/token\"")
        buildConfigField("String", "NOTION_AUTH_URL", "\"https://api.notion.com/v1/oauth/authorize\"")

        manifestPlaceholders["notionRedirectScheme"] = "com.notiontasks"
        manifestPlaceholders["notionRedirectHost"] = "oauth"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // Replace with release signing in CI
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Database
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // Preferences
    implementation(libs.datastore.preferences)

    // Widget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // OAuth WebView
    implementation(libs.accompanist.webview)

    debugImplementation(libs.androidx.ui.tooling)
}
