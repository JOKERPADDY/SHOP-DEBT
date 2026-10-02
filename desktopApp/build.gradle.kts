plugins {
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
    sourceSets {
        val main by getting {
            kotlin.srcDirs("src/jvmMain/kotlin")
            resources.srcDirs("src/jvmMain/resources")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    
    // Explicitly add skiko to ensure version alignment with Compose
    implementation("org.jetbrains.skiko:skiko-awt:0.8.9")
}

compose.desktop {
    application {
        mainClass = "com.example.debt.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "DebtSystem"
            packageVersion = "1.0.0"
            
            // Removing manual module list to let Compose include everything it needs
            // This is safer for preventing "Failed to launch JVM" errors
            
            windows {
                menu = true
                shortcut = true
                upgradeUuid = "5801d5a0-1ba1-4106-9b45-d90ee7e750ed"
                console = true
            }
        }
    }
}
