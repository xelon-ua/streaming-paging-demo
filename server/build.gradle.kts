plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    application
}

group = "com.example.streaming_paging_demo"
version = "1.0.0"
application {
    mainClass.set("com.example.streaming_paging_demo.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.serializationJson)
    implementation(libs.ktor.server.sse)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javaTime)
    implementation(libs.h2)
    implementation(libs.exposed.pagination)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}