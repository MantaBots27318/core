val pluginNamespace = "com.bylazar.alliance"
val pluginVersion = "1.0.0"

repositories{
    maven { url = uri("https://mymaven.bylazar.com/releases") }
}

dependencies {

    compileOnly("org.firstinspires.ftc:Inspection:11.1.0")
    compileOnly("org.firstinspires.ftc:Blocks:11.1.0")
    compileOnly("org.firstinspires.ftc:RobotCore:11.1.0")
    compileOnly("org.firstinspires.ftc:RobotServer:11.1.0")
    compileOnly("org.firstinspires.ftc:OnBotJava:11.1.0")
    compileOnly("org.firstinspires.ftc:Hardware:11.1.0")
    compileOnly("org.firstinspires.ftc:FtcCommon:11.1.0")
    compileOnly("org.firstinspires.ftc:Vision:11.1.0")

    compileOnly("com.bylazar:fullpanels:1.0.12")

    compileOnly(project(":Core"))
}



plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("com.bylazar.svelte-assets")
}

svelteAssets {
    webAppPath = "web"
    buildDirPath = "dist"
    assetsPath = "web/plugins/$pluginNamespace"
}

android {
    namespace = pluginNamespace

    defaultConfig {
        compileSdk = 34
        minSdk = 24
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    publishing {
        singleVariant("release") {}
    }
}

// Après le bloc android {}
afterEvaluate {
    tasks.named("assembleRelease") {
        dependsOn("copySvelteToAssetsAllianceNavlet")
    }
    tasks.named("packageReleaseAssets") {
        dependsOn("copySvelteToAssetsAllianceNavlet")
    }
    tasks.named("compileReleaseKotlin") {
        dependsOn("copySvelteToAssetsAllianceNavlet")
    }
}

tasks.register<Copy>("deployToTeamCode") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/aar"))
    include("*.aar")
    into("../../TeamCode/plugin")
    rename { "alliance-plugin.aar" }
}

tasks.register<Copy>("deployToCore") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/aar"))
    include("*.aar")
    into("../../Core/plugin")
    rename { "alliance-plugin.aar" }
}