plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    // Читає app/google-services.json і генерує з нього ресурси Firebase.
    alias(libs.plugins.google.services)
}

// Читаємо конфігурацію з gradle.properties через providers — це сумісно
// з configuration cache, на відміну від прямого звернення до project.property().
val baseUrlDebug: String = providers.gradleProperty("eventsparser.baseUrl.debug").get()
val baseUrlRelease: String = providers.gradleProperty("eventsparser.baseUrl.release").get()
val fcmTopic: String = providers.gradleProperty("eventsparser.fcmTopic").get()
val feedPageSize: String = providers.gradleProperty("eventsparser.feedPageSize").get()
val syncPageSize: String = providers.gradleProperty("eventsparser.syncPageSize").get()

android {
    namespace = "com.KSU.EventsParser"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.KSU.EventsParser"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Спільні для всіх типів збірки значення.
        buildConfigField("String", "FCM_TOPIC", "\"$fcmTopic\"")
        buildConfigField("int", "FEED_PAGE_SIZE", feedPageSize)
        buildConfigField("int", "SYNC_PAGE_SIZE", syncPageSize)
    }

    androidResources {
        // Перелік локалей, які реально є в ресурсах. Обрізає непотрібні
        // переклади бібліотек і задає список для системного перемикача мови.
        localeFilters += listOf("uk", "en")
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$baseUrlDebug\"")
        }
        release {
            optimization {
                enable = false
            }
            buildConfigField("String", "BASE_URL", "\"$baseUrlRelease\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // Обов'язково для AGP 8+: без цього buildConfigField нічого не генерує.
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.browser)

    // Мережа
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Локальний кеш
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Календар
    implementation(libs.calendar.compose)

    // Push. Аналітику навмисно не підключаємо — вона тут не потрібна.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
