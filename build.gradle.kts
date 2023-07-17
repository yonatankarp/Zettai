import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.diffplug.spotless") version "6.20.0"
}

repositories {
    mavenCentral()
}

dependencies {
    val http4kVersion = project.properties["http4kVersion"].toString()
    val jsoupVersion = project.properties["jsoupVersion"].toString()
    val junitLauncherVersion = project.properties["junitLauncherVersion"].toString()
    val junitVersion = project.properties["junitVersion"].toString()
    val kotlinLoggingVersion = project.properties["kotlinLoggingVersion"].toString()
    val pesticideVersion = project.properties["pesticideVersion"].toString()
    val striktVersion = project.properties["striktVersion"].toString()

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Web server
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-jetty:$http4kVersion")

    // Logging
    runtimeOnly("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitLauncherVersion")
    testImplementation("com.ubertob.pesticide:pesticide-core:$pesticideVersion")
    testImplementation("io.strikt:strikt-core:$striktVersion")
    testImplementation("org.http4k:http4k-client-jetty:$http4kVersion")
    testImplementation("org.jsoup:jsoup:$jsoupVersion")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events = setOf(PASSED, SKIPPED, FAILED)
        }

        // if true show println in test console
        testLogging.showStandardStreams = false

        // start tests every time, even when code not changed
        outputs.upToDateWhen { false }
    }
}

configure<SpotlessExtension> {
    kotlin {
        // see https://github.com/shyiko/ktlint#standard-rules
        ktlint()
    }
    kotlinGradle {
        trimTrailingWhitespace()
        ktlint()
    }
}
