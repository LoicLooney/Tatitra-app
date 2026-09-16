import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/**
 * Lit un réglage, dans l'ordre : option de ligne de commande (-P) ou gradle.properties,
 * puis local.properties.
 *
 * Gradle n'expose pas local.properties comme propriété de projet — le plugin Android n'y
 * lit que sdk.dir — il faut donc l'ouvrir explicitement. C'est pourtant le bon endroit
 * pour ces valeurs : le fichier est ignoré par Git, contrairement à gradle.properties.
 */
val proprietesLocales = Properties().apply {
    val fichier = rootProject.file("local.properties")
    if (fichier.exists()) fichier.inputStream().use { load(it) }
}

fun reglage(cle: String, parDefaut: String = ""): String =
    (project.findProperty(cle) as String?)
        ?: proprietesLocales.getProperty(cle)
        ?: parDefaut

val apiBaseUrl: String = reglage("tatitra.apiBaseUrl", "http://10.0.2.2:3000/")

// Authentification Supabase. Les deux valeurs se placent dans local.properties ou
// gradle.properties (tous deux ignorés par Git) — jamais en dur dans le code.
//
// La clé attendue est la clé PUBLIABLE (« sb_publishable_… », anciennement « anon ») :
// elle est conçue pour être embarquée dans une application cliente. La clé secrète
// « sb_secret_… » du backend ne doit jamais se retrouver ici.
//
// Laissées vides, l'application démarre quand même : l'écran de connexion explique
// que l'authentification n'est pas configurée et le mode démonstration reste ouvert.
val supabaseUrl: String = reglage("tatitra.supabaseUrl")
val supabaseAnonKey: String = reglage("tatitra.supabaseAnonKey")

android {
    namespace = "mg.itu.tatitra_app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "mg.itu.tatitra_app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            // android.util.Log est un stub sur la JVM : sans ceci, tout appel à Log lève
            // une exception au lieu de ne rien faire.
            isReturnDefaultValues = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Schémas Room exportés : utile pour relire la structure de la base et préparer les migrations.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation + MVVM
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Offline-first : Room + synchronisation différée
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)

    // Réseau
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Photo + GPS
    implementation(libs.coil.compose)
    implementation(libs.androidx.exifinterface)
    implementation(libs.play.services.location)

    // Préférences persistantes (dernière sync)
    implementation(libs.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}