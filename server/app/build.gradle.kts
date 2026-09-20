plugins {
    id("com.organizer.java-conventions")
    alias(libs.plugins.spring.boot)
}

group = "com.organizer"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":application"))
    implementation(project(":adapter-graphql"))
    implementation(project(":adapter-persistence"))
    implementation(project(":adapter-security"))

    implementation(libs.spring.boot.starter.graphql)
    implementation(libs.spring.boot.starter.webmvc)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.boot:spring-boot-starter-graphql-test")
    testImplementation("org.springframework:spring-webflux")
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> { useJUnitPlatform() }
