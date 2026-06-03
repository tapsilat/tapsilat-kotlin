plugins {
    kotlin("jvm") version "2.4.0"
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
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.2")

    testImplementation(kotlin("test"))
    testImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
}
