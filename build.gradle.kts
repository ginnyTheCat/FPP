plugins {
    application
    kotlin("jvm") version "2.0.21"
}

application {
    mainClass = "MainKt"
}

repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDir("src")
}

dependencies {
    implementation("org.apache.commons:commons-email2-javax:2.0.0-M1")
}

tasks.getByName("run", JavaExec::class) {
    standardInput = System.`in`
}