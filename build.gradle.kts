plugins {
    kotlin("jvm") version "1.9.24"
    `java-library`
}

group = (findProperty("GROUP") as String)
version = (findProperty("VERSION_NAME") as String)

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    named("main") {
        java.setSrcDirs(
            listOf(
                "shared/src/main/java",
                "order/src/main/java",
                "checkout/src/main/java",
                "credit/src/main/java",
                "card/src/main/java",
                "wallet/src/main/java",
            ),
        )
        resources.setSrcDirs(listOf("shared/src/main/resources"))
    }
    named("test") {
        java.setSrcDirs(listOf("shared/src/test/java", "order/src/test/java", "checkout/src/test/java", "credit/src/test/java", "card/src/test/java", "wallet/src/test/java"))
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    testImplementation(kotlin("test"))
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
}
