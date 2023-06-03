import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    kotlin("jvm") version "1.8.21"
}

repositories {
    mavenCentral()
}

dependencies {
    val http4kVersion = project.properties["http4kVersion"].toString()
    val kotestVersion = project.properties["kotestVersion"].toString()

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-jetty:$http4kVersion")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("org.http4k:http4k-client-jetty:$http4kVersion")
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