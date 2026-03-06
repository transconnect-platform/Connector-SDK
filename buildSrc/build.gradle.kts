plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.spotlessPlugin)
    implementation(libs.lombokPlugin)
    implementation(libs.vanniktechMavenPublishPlugin)
}