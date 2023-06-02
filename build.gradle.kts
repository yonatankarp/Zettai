import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.8.21"
}

repositories {
    mavenCentral()
}

dependencies {
    val http4kVersion = project.properties["http4kVersion"].toString()
    val junitVersion = project.properties["junitVersion"].toString()
    val junitLauncherVersion = project.properties["junitLauncherVersion"].toString()

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("org.http4k:http4k-core:${http4kVersion}")
    implementation("org.http4k:http4k-server-jetty:${http4kVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${junitLauncherVersion}")
    testImplementation("org.http4k:http4k-client-jetty:${http4kVersion}")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }

        //if true show println in test console
        testLogging.showStandardStreams = false

        // start tests every time, even when code not changed
        outputs.upToDateWhen { false }

    }
}