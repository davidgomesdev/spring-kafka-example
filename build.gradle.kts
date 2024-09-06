import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val confluentVersion = "7.4.0"

plugins {
	kotlin("jvm") version "1.9.21"
	kotlin("plugin.spring") version "1.9.21"
	kotlin("plugin.jpa") version "1.9.21"
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
	id("com.github.imflog.kafka-schema-registry-gradle-plugin") version "2.1.1"
	id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "me.davidgomes"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

buildscript {
	repositories {
		gradlePluginPortal()
		maven("https://packages.confluent.io/maven/")
		maven("https://jitpack.io")
	}
}

repositories {
	mavenCentral()
	maven {
		url = URI.create("https://packages.confluent.io/maven")
	}
}

ext {
	set("testcontainers.version", "1.20.1")
}

val pgVersion = "42.7.2"
val awaitilityVersion = "4.2.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.apache.kafka:kafka-streams")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("io.confluent:kafka-avro-serializer:$confluentVersion")
	implementation("io.confluent:kafka-schema-registry-client:$confluentVersion")
	implementation("io.confluent:kafka-streams-avro-serde:$confluentVersion")

	runtimeOnly("org.postgresql:postgresql:$pgVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:kafka")
	testImplementation("org.awaitility:awaitility-kotlin:$awaitilityVersion")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
